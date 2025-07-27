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
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
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

          UserSubscription userSub =
              new UserSubscription(
                  Long.valueOf(userId),
                  session.getCustomer(),
                  session.getSubscription(),
                  plan,
                  SubscriptionStatus.ACTIVE);

          subscriptionRepository.save(userSub);
        } catch (Exception e) {
          log.error("");
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
    // Handle successful payment - subscription remains active
    System.out.println("Payment succeeded for subscription");
  }

  public void handleInvoicePaymentFailed(Event event) {
    // Handle failed payment - could trigger dunning management
    System.out.println("Payment failed for subscription");
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

      userSub.setStatus(SubscriptionStatus.valueOf(subscription.getStatus().toUpperCase()));
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
        try {
          SubscriptionPlan plan = SubscriptionPlan.fromStripePriceId(priceId);
          userSub.setPlan(plan);
        } catch (IllegalArgumentException e) {
          System.err.println("Unknown price ID: " + priceId);
        }
      }

      subscriptionRepository.save(userSub);
    }
  }
}
