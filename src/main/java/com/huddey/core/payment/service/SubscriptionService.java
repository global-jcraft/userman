package com.huddey.core.payment.service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.huddey.core.payment.data.dto.CancelSubscriptionResponse;
import com.huddey.core.payment.data.dto.CreateCheckoutSessionRequest;
import com.huddey.core.payment.data.dto.CreateDirectSubscriptionRequest;
import com.huddey.core.payment.data.entity.UserSubscription;
import com.huddey.core.payment.data.enums.SubscriptionPlan;
import com.huddey.core.payment.data.enums.SubscriptionStatus;
import com.huddey.core.payment.exception.StripeServiceException;
import com.huddey.core.payment.exception.SubscriptionNotFoundException;
import com.huddey.core.payment.repository.UserSubscriptionRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import com.stripe.param.SubscriptionUpdateParams;
import com.stripe.param.checkout.SessionCreateParams;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SubscriptionService {

  private final UserSubscriptionRepository subscriptionRepository;

  public SubscriptionService(UserSubscriptionRepository subscriptionRepository) {
    this.subscriptionRepository = subscriptionRepository;
  }

  public String createCheckoutSession(
      Long userId, String email, CreateCheckoutSessionRequest request) throws StripeException {

    log.debug("Creating checkout session for user {} with plan {}", userId, request.getPlan());

    Customer customer = getOrCreateCustomer(userId, email);

    var lineItem =
        SessionCreateParams.LineItem.builder()
            .setPrice(request.getPriceId())
            .setQuantity(1L)
            .build();

    SessionCreateParams params =
        SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
            .setCustomer(customer.getId())
            .addLineItem(lineItem)
            .setSuccessUrl(request.getSuccessUrl())
            .setCancelUrl(request.getCancelUrl())
            .putMetadata("userId", userId.toString())
            .putMetadata("plan", request.getPlan().name())
            .build();

    Session session = Session.create(params);
    log.debug("Created checkout session {} for user {}", session.getId(), userId);

    return session.getUrl();
  }

  public void createSubscription(Long userId, String email, CreateDirectSubscriptionRequest request)
      throws StripeException {

    // Create or get customer
    Customer customer = getOrCreateCustomer(userId, email);

    // Create subscription
    SubscriptionCreateParams params =
        SubscriptionCreateParams.builder()
            .setCustomer(customer.getId())
            .setDefaultPaymentMethod(request.getPaymentMethodId())
            .addItem(
                SubscriptionCreateParams.Item.builder()
                    .setPrice(request.getPriceId())
                    .setQuantity(1L)
                    .build())
            .setPaymentBehavior(SubscriptionCreateParams.PaymentBehavior.DEFAULT_INCOMPLETE)
            // .setExpandListValue("latest_invoice.payment_intent")
            .putMetadata("userId", userId.toString())
            .putMetadata("plan", request.getPlan().name())
            .build();

    Subscription subscription = Subscription.create(params);

    // Save a subscription to a database
    saveSubscriptionToDatabase(userId, customer.getId(), subscription, request.getPlan());
  }

  public void updateSubscription(Long userId, SubscriptionPlan newPlan) throws StripeException {
    log.debug("Updating subscription for user {} to plan {}", userId, newPlan);
    UserSubscription userSub =
        subscriptionRepository
            .findByUserId(userId)
            .orElseThrow(
                () ->
                    new SubscriptionNotFoundException(
                        "Subscription not found for user: " + userId));

    if (userSub.getStatus() != SubscriptionStatus.ACTIVE) {
      throw new StripeServiceException("Cannot update inactive subscription", null);
    }

    Subscription subscription = Subscription.retrieve(userSub.getStripeSubscriptionId());

    SubscriptionUpdateParams params =
        SubscriptionUpdateParams.builder()
            .addItem(
                SubscriptionUpdateParams.Item.builder()
                    .setId(subscription.getItems().getData().getFirst().getId())
                    .setPrice(newPlan.getStripePriceId())
                    .build())
            .setProrationBehavior(SubscriptionUpdateParams.ProrationBehavior.CREATE_PRORATIONS)
            .putMetadata("plan", newPlan.name())
            .build();

    subscription.update(params);

    // Update database
    userSub.setPlan(newPlan);
    userSub.setUpdatedAt(OffsetDateTime.now());
    subscriptionRepository.save(userSub);

    log.debug("Successfully updated subscription for user {} to plan {}", userId, newPlan);
  }

  @Transactional
  public CancelSubscriptionResponse cancelSubscription(Long userId, boolean cancelAtPeriodEnd)
      throws StripeException {
    log.debug("Cancelling subscription for user {}, at period end: {}", userId, cancelAtPeriodEnd);

    UserSubscription userSub =
        subscriptionRepository
            .findByUserId(userId)
            .orElseThrow(
                () ->
                    new SubscriptionNotFoundException(
                        "Subscription not found for user: " + userId));

    Subscription subscription = Subscription.retrieve(userSub.getStripeSubscriptionId());

    if (cancelAtPeriodEnd) {
      SubscriptionUpdateParams params =
          SubscriptionUpdateParams.builder().setCancelAtPeriodEnd(true).build();
      subscription.update(params);

      userSub.setCancelAtPeriodEnd(true);
    } else {
      subscription.cancel();
      userSub.setStatus(SubscriptionStatus.CANCELED);
    }

    subscriptionRepository.save(userSub);
    log.debug("Successfully cancelled subscription for user {}", userId);
    return new CancelSubscriptionResponse(true, "Subscription cancelled successfully");
  }

  public Optional<UserSubscription> getUserSubscription(Long userId) {
    return subscriptionRepository.findByUserId(userId);
  }

  public void handleWebhookEvent(String payload, String sigHeader) throws StripeException {
    // Webhook handling will be implemented in the webhook controller
    // This method can be used for additional webhook processing logic
  }

  private Customer getOrCreateCustomer(Long userId, String email) throws StripeException {
    Optional<UserSubscription> existingSub = subscriptionRepository.findByUserId(userId);

    if (existingSub.isPresent() && existingSub.get().getStripeCustomerId() != null) {
      return Customer.retrieve(existingSub.get().getStripeCustomerId());
    }

    CustomerCreateParams params =
        CustomerCreateParams.builder()
            .setEmail(email)
            .putMetadata("userId", userId.toString())
            .build();

    Customer customer = Customer.create(params);
    log.debug("Created Stripe customer {} for user {}", customer.getId(), userId);

    UserSubscription userSub = existingSub.orElse(new UserSubscription());
    userSub.setUserId(userId);
    userSub.setStripeCustomerId(customer.getId());
    subscriptionRepository.save(userSub);

    return customer;
  }

  private void saveSubscriptionToDatabase(
      Long userId, String customerId, Subscription subscription, SubscriptionPlan plan) {
    UserSubscription userSub =
        new UserSubscription(
            userId,
            customerId,
            subscription.getId(),
            plan,
            SubscriptionStatus.valueOf(subscription.getStatus().toUpperCase()));

    userSub.setCurrentPeriodStart(
        Instant.ofEpochSecond(subscription.getItems().getData().getFirst().getCurrentPeriodStart())
            .atZone(ZoneId.systemDefault())
            .toOffsetDateTime());
    userSub.setCurrentPeriodEnd(
        Instant.ofEpochSecond(subscription.getItems().getData().getFirst().getCurrentPeriodEnd())
            .atZone(ZoneId.systemDefault())
            .toOffsetDateTime());

    userSub.setCancelAtPeriodEnd(subscription.getCancelAtPeriodEnd());

    subscriptionRepository.save(userSub);
  }

  public void saveSubscriptionFromWebhook(
      String stripeSubscriptionId,
      String stripeCustomerId,
      SubscriptionPlan plan,
      SubscriptionStatus status,
      Long currentPeriodStart,
      Long currentPeriodEnd,
      boolean cancelAtPeriodEnd) {

    // Find user by customer ID
    Optional<UserSubscription> existingSub =
        subscriptionRepository.findByStripeCustomerId(stripeCustomerId);

    UserSubscription userSub;
    if (existingSub.isPresent()) {
      userSub = existingSub.get();
    } else {
      // This shouldn't happen normally, but handle gracefully
      log.warn("No existing subscription found for customer {}", stripeCustomerId);
      return;
    }

    userSub.setStripeSubscriptionId(stripeSubscriptionId);
    userSub.setPlan(plan);
    userSub.setStatus(status);
    userSub.setCurrentPeriodStart(
        OffsetDateTime.ofInstant(
            Instant.ofEpochSecond(currentPeriodStart), ZoneId.systemDefault()));
    userSub.setCurrentPeriodEnd(
        OffsetDateTime.ofInstant(Instant.ofEpochSecond(currentPeriodEnd), ZoneId.systemDefault()));
    userSub.setCancelAtPeriodEnd(cancelAtPeriodEnd);
    userSub.setUpdatedAt(OffsetDateTime.now());

    subscriptionRepository.save(userSub);
    log.debug("Updated subscription from webhook for customer {}", stripeCustomerId);
  }
}
