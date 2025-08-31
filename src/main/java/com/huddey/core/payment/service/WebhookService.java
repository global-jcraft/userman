package com.huddey.core.payment.service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.huddey.core.payment.data.entity.UserSubscription;
import com.huddey.core.payment.data.enums.SubscriptionPlan;
import com.huddey.core.payment.data.enums.SubscriptionStatus;
import com.huddey.core.payment.repository.UserSubscriptionRepository;
import com.huddey.core.payment.utils.StripeUtils;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WebhookService {

  private final UserSubscriptionRepository subscriptionRepository;

  public WebhookService(UserSubscriptionRepository subscriptionRepository) {
    this.subscriptionRepository = subscriptionRepository;
  }

  /**
   * Handles the checkout session completed event from Stripe.
   *
   * @param event The Stripe event object
   */
  public void handleCheckoutSessionCompleted(Event event) {
    EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
    Session session = (Session) dataObjectDeserializer.getObject().orElse(null);

    if (session != null && session.getMode().equals("subscription")) {
      String userId = session.getMetadata().get("userId");
      String planName = session.getMetadata().get("plan");

      if (userId != null && planName != null) {
        try {
          SubscriptionPlan plan = SubscriptionPlan.valueOf(planName);
          Long userIdLong = Long.valueOf(userId);

          Optional<UserSubscription> existingSub = subscriptionRepository.findByUserId(userIdLong);

          // Fetch subscription details from Stripe to get period dates
          Subscription subscription = Subscription.retrieve(session.getSubscription());

          UserSubscription userSub;
          if (existingSub.isPresent()) {
            userSub = existingSub.get();
            userSub.setStripeCustomerId(session.getCustomer());
            userSub.setStripeSubscriptionId(session.getSubscription());
            userSub.setPlan(plan);
            userSub.setStatus(SubscriptionStatus.ACTIVE);
            userSub.setUpdatedAt(OffsetDateTime.now());
          } else {
            userSub =
                new UserSubscription(
                    userIdLong,
                    session.getCustomer(),
                    session.getSubscription(),
                    plan,
                    SubscriptionStatus.ACTIVE);
          }

          userSub.setCurrentPeriodStart(
              Instant.ofEpochSecond(
                      subscription.getItems().getData().getFirst().getCurrentPeriodStart())
                  .atZone(ZoneId.systemDefault())
                  .toOffsetDateTime());
          userSub.setCurrentPeriodEnd(
              Instant.ofEpochSecond(
                      subscription.getItems().getData().getFirst().getCurrentPeriodEnd())
                  .atZone(ZoneId.systemDefault())
                  .toOffsetDateTime());

          subscriptionRepository.save(userSub);
        } catch (StripeException e) {
          log.error("Failed to retrieve subscription from Stripe: {}", e.getMessage());
        } catch (Exception e) {
          log.error("Error processing checkout session: {}", e.getMessage());
        }
      }
    }
  }

  public void handleSubscriptionCreated(Event event) {
    EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
    Subscription subscription = (Subscription) dataObjectDeserializer.getObject().orElse(null);

    if (subscription != null) {
      updateSubscriptionInDatabase(subscription);
    }
  }

  /**
   * Handles the subscription update event from Stripe.
   *
   * @param event The Stripe event object
   */
  public void handleSubscriptionUpdated(Event event) {
    EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
    Subscription subscription = (Subscription) dataObjectDeserializer.getObject().orElse(null);

    if (subscription != null) {
      updateSubscriptionInDatabase(subscription);
    }
  }

  public void handleSubscriptionDeleted(Event event) {
    EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
    Subscription subscription = (Subscription) dataObjectDeserializer.getObject().orElse(null);

    if (subscription != null) {
      Optional<UserSubscription> userSub =
          subscriptionRepository.findByStripeSubscriptionId(subscription.getId());

      if (userSub.isPresent()) {
        UserSubscription sub = userSub.get();
        sub.setStatus(SubscriptionStatus.CANCELED);
        sub.setUpdatedAt(OffsetDateTime.now());
        subscriptionRepository.save(sub);
      }
    }
  }

  public void handleInvoicePaymentSucceeded(Event event) {
    StripeUtils.logEventStart("invoice payment succeeded", event.getId());

    Optional<Invoice> invoiceOpt = StripeUtils.extractInvoice(event);
    if (invoiceOpt.isEmpty()) {
      log.debug("Invoice output is empty");
      return;
    }

    Invoice invoice = invoiceOpt.get();

    // Try to get subscription from invoice lines first
    Optional<String> subscriptionIdOpt = StripeUtils.getSubscriptionIdFromInvoice(invoice);
    Optional<UserSubscription> userSubOpt = Optional.empty();

    if (subscriptionIdOpt.isPresent()) {
      userSubOpt =
          StripeUtils.findUserSubscription(subscriptionIdOpt.get(), subscriptionRepository);
    } else {
      // Fallback: try to find subscription by customer ID
      String customerId = invoice.getCustomer();
      if (customerId != null) {
        userSubOpt =
            StripeUtils.findUserSubscriptionByCustomerId(customerId, subscriptionRepository);
      }
    }

    if (userSubOpt.isEmpty()) {
      return;
    }

    try {
      UserSubscription userSub = userSubOpt.get();

      // Ensure subscription is active after successful payment
      if (userSub.getStatus() != SubscriptionStatus.ACTIVE) {
        userSub.setStatus(SubscriptionStatus.ACTIVE);
        userSub.setUpdatedAt(OffsetDateTime.now());
        subscriptionRepository.save(userSub);
        log.info(
            "Reactivated subscription {} after successful payment",
            userSub.getStripeSubscriptionId());
      }

      // Log successful payment
      log.info(
          "Payment succeeded for subscription: {}, user: {}, amount: {}",
          userSub.getStripeSubscriptionId(),
          userSub.getUserId(),
          StripeUtils.formatAmount(invoice.getAmountPaid()));

    } catch (Exception e) {
      StripeUtils.logEventError("invoice payment succeeded", event.getId(), e);
    }
  }

  public void handleInvoicePaymentFailed(Event event) {
    StripeUtils.logEventStart("invoice payment failed", event.getId());

    Optional<Invoice> invoiceOpt = StripeUtils.extractInvoice(event);
    if (invoiceOpt.isEmpty()) {
      return;
    }

    Invoice invoice = invoiceOpt.get();

    // Try to get subscription from invoice lines first
    Optional<String> subscriptionIdOpt = StripeUtils.getSubscriptionIdFromInvoice(invoice);
    Optional<UserSubscription> userSubOpt = Optional.empty();

    if (subscriptionIdOpt.isPresent()) {
      userSubOpt =
          StripeUtils.findUserSubscription(subscriptionIdOpt.get(), subscriptionRepository);
    } else {
      // Fallback: try to find subscription by customer ID
      String customerId = invoice.getCustomer();
      if (customerId != null) {
        userSubOpt =
            StripeUtils.findUserSubscriptionByCustomerId(customerId, subscriptionRepository);
      }
    }

    if (userSubOpt.isEmpty()) {
      return;
    }

    try {
      UserSubscription userSub = userSubOpt.get();

      // Update subscription status to indicate payment issues
      // Note: Don't immediately cancel - Stripe has retry logic
      if (userSub.getStatus() == SubscriptionStatus.ACTIVE) {
        userSub.setStatus(SubscriptionStatus.PAST_DUE);
        userSub.setUpdatedAt(OffsetDateTime.now());
        subscriptionRepository.save(userSub);
        log.warn(
            "Updated subscription {} to PAST_DUE after payment failure",
            userSub.getStripeSubscriptionId());
      }

      // Log payment failure details for monitoring/alerting
      log.error(
          "Payment failed for subscription: {}, user: {}, attempt: {}, amount: {}",
          userSub.getStripeSubscriptionId(),
          userSub.getUserId(),
          invoice.getAttemptCount(),
          StripeUtils.formatAmount(invoice.getAmountDue()));

      // Here you could add additional logic such as:
      // - Send notification to user about payment failure
      // - Trigger dunning management process
      // - Log to external monitoring system

    } catch (Exception e) {
      StripeUtils.logEventError("invoice payment failed", event.getId(), e);
    }
  }

  public void handleInvoiceCreated(Event event) {
    try {
      StripeUtils.logEventStart("invoice created", event.getId());
      // Invoice created - typically no action needed
    } catch (Exception e) {
      StripeUtils.logEventError("invoice created", event.getId(), e);
    }
  }

  public void handleInvoicePaid(Event event) {
    try {
      StripeUtils.logEventStart("invoice paid", event.getId());
      // Similar to payment succeeded but for one-time payments
      handleInvoicePaymentSucceeded(event);
    } catch (Exception e) {
      StripeUtils.logEventError("invoice paid", event.getId(), e);
    }
  }

  public void handleInvoiceFinalized(Event event) {
    try {
      StripeUtils.logEventStart("invoice finalized", event.getId());
      // Invoice finalized - typically no action needed
    } catch (Exception e) {
      StripeUtils.logEventError("invoice finalized", event.getId(), e);
    }
  }

  public void handleChargeSucceeded(Event event) {
    try {
      StripeUtils.logEventStart("charge succeeded", event.getId());
      // Charge succeeded - typically handled by invoice events
    } catch (Exception e) {
      StripeUtils.logEventError("charge succeeded", event.getId(), e);
    }
  }

  public void handlePaymentMethodAttached(Event event) {
    try {
      StripeUtils.logEventStart("payment method attached", event.getId());
      // Payment method attached - typically no action needed
    } catch (Exception e) {
      StripeUtils.logEventError("payment method attached", event.getId(), e);
    }
  }

  public void handlePaymentIntentSucceeded(Event event) {
    try {
      StripeUtils.logEventStart("payment intent succeeded", event.getId());
      // Payment intent succeeded - typically handled by invoice events
    } catch (Exception e) {
      StripeUtils.logEventError("payment intent succeeded", event.getId(), e);
    }
  }

  public void handlePaymentIntentCreated(Event event) {
    try {
      StripeUtils.logEventStart("payment intent created", event.getId());
      // Payment intent created - typically no action needed
    } catch (Exception e) {
      StripeUtils.logEventError("payment intent created", event.getId(), e);
    }
  }

  /**
   * Updates the subscription in the database with the latest information from Stripe.
   *
   * @param subscription The subscription object from Stripe
   */
  public void updateSubscriptionInDatabase(Subscription subscription) {
    Optional<UserSubscription> userSubOpt =
        subscriptionRepository.findByStripeSubscriptionId(subscription.getId());

    if (userSubOpt.isPresent()) {
      UserSubscription userSub = userSubOpt.get();

      if (subscription.getStatus() != null) {
        userSub.setStatus(SubscriptionStatus.valueOf(subscription.getStatus().toUpperCase()));
      }
      userSub.setCurrentPeriodStart(
          Instant.ofEpochSecond(
                  subscription.getItems().getData().getFirst().getCurrentPeriodStart())
              .atZone(ZoneId.systemDefault())
              .toOffsetDateTime());
      userSub.setCurrentPeriodEnd(
          Instant.ofEpochSecond(subscription.getItems().getData().getFirst().getCurrentPeriodEnd())
              .atZone(ZoneId.systemDefault())
              .toOffsetDateTime());
      userSub.setCancelAtPeriodEnd(subscription.getCancelAtPeriodEnd());
      userSub.setUpdatedAt(OffsetDateTime.now());

      // Update plan if changed
      if (subscription.getItems() != null && !subscription.getItems().getData().isEmpty()) {
        String priceId = subscription.getItems().getData().getFirst().getPrice().getId();
        SubscriptionPlan plan = SubscriptionPlan.fromStripePriceId(priceId);
        userSub.setPlan(plan);
      }

      subscriptionRepository.save(userSub);
    }
  }
}
