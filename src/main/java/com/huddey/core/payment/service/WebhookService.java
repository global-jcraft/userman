package com.huddey.core.payment.service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.huddey.core.payment.data.entity.UserSubscription;
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
  private final PaymentMethodService paymentMethodService;
  private final RefundDisputeService refundDisputeService;

  public WebhookService(
      UserSubscriptionRepository subscriptionRepository,
      PaymentMethodService paymentMethodService,
      RefundDisputeService refundDisputeService) {
    this.subscriptionRepository = subscriptionRepository;
    this.paymentMethodService = paymentMethodService;
    this.refundDisputeService = refundDisputeService;
  }

  /**
   * Handles the checkout session completed event from Stripe.
   *
   * @param event The Stripe event object
   */
  public void handleCheckoutSessionCompleted(Event event) {
    EventDataObjectDeserializer d = event.getDataObjectDeserializer();
    Session session = (Session) d.getObject().orElse(null);

    if (session != null && "subscription".equals(session.getMode())) {
      String userIdStr = session.getMetadata().get("userId");
      String planKey = session.getMetadata().get("planKey");
      String interval = session.getMetadata().get("interval");
      String currency = session.getMetadata().get("currency");
      String seatsStr = session.getMetadata().get("seats");

      if (userIdStr != null) {
        try {
          Long userId = Long.valueOf(userIdStr);
          Subscription subscription = Subscription.retrieve(session.getSubscription());

          var item = subscription.getItems().getData().getFirst();
          var quantity = item.getQuantity() == null ? 1L : item.getQuantity();
          if (seatsStr != null) {
            try {
              quantity = Math.max(quantity, Long.parseLong(seatsStr));
            } catch (NumberFormatException ignored) {
            }
          }

          Optional<UserSubscription> existingSub = subscriptionRepository.findByUserId(userId);
          UserSubscription userSub = existingSub.orElseGet(UserSubscription::new);

          userSub.setUserId(userId);
          userSub.setStripeCustomerId(session.getCustomer());
          userSub.setStripeSubscriptionId(subscription.getId());
          userSub.setStatus(SubscriptionStatus.ACTIVE);
          userSub.setCancelAtPeriodEnd(false);

          if (planKey != null) userSub.setPlanKey(planKey);
          if (interval != null) userSub.setBillingInterval(interval.toLowerCase());
          if (currency != null) userSub.setCurrency(currency.toUpperCase());
          userSub.setSeatCount(quantity);

          userSub.setCurrentPeriodStart(
              Instant.ofEpochSecond(item.getCurrentPeriodStart())
                  .atZone(ZoneId.systemDefault())
                  .toOffsetDateTime());
          userSub.setCurrentPeriodEnd(
              Instant.ofEpochSecond(item.getCurrentPeriodEnd())
                  .atZone(ZoneId.systemDefault())
                  .toOffsetDateTime());
          userSub.setUpdatedAt(OffsetDateTime.now());

          subscriptionRepository.save(userSub);
        } catch (StripeException e) {
          log.error("Failed to retrieve subscription from Stripe: {}", e.getMessage(), e);
        } catch (Exception e) {
          log.error("Error processing checkout session: {}", e.getMessage(), e);
        }
      }
    }
  }

  public void handleSubscriptionCreated(Event event) {
    var data = event.getDataObjectDeserializer();
    Subscription subscription = (Subscription) data.getObject().orElse(null);
    if (subscription != null) {
      updateSubscriptionInDatabase(subscription); // <— unchanged call, now uses new logic above
    }
  }

  /**
   * Handles the subscription update event from Stripe.
   *
   * @param event The Stripe event object
   */
  public void handleSubscriptionUpdated(Event event) {
    var data = event.getDataObjectDeserializer();
    Subscription subscription = (Subscription) data.getObject().orElse(null);
    if (subscription != null) {
      updateSubscriptionInDatabase(subscription); // <— unchanged call, now uses new logic above
    }
  }

  public void handleSubscriptionDeleted(Event event) {
    EventDataObjectDeserializer d = event.getDataObjectDeserializer();
    Subscription subscription = (Subscription) d.getObject().orElse(null);

    if (subscription != null) {
      Optional<UserSubscription> userSubOpt =
          subscriptionRepository.findByStripeSubscriptionId(subscription.getId());
      if (userSubOpt.isEmpty()) {
        userSubOpt = subscriptionRepository.findByStripeCustomerId(subscription.getCustomer());
      }
      if (userSubOpt.isPresent()) {
        UserSubscription sub = userSubOpt.get();

        // Downgrade to local Free (no Stripe sub)
        String currency = sub.getCurrency() != null ? sub.getCurrency() : "EUR";
        sub.setStripeSubscriptionId(null);
        sub.setPlanKey("huddey_free");
        sub.setBillingInterval("month");
        sub.setCurrency(currency);
        sub.setSeatCount(1L);
        sub.setStatus(SubscriptionStatus.ACTIVE); // Free is active locally
        sub.setCancelAtPeriodEnd(false);
        sub.setCurrentPeriodStart(OffsetDateTime.now());
        sub.setCurrentPeriodEnd(OffsetDateTime.now().plusYears(100));
        sub.setUpdatedAt(OffsetDateTime.now());

        subscriptionRepository.save(sub);
        log.info(
            "Downgraded customer {} to Free after subscription deletion",
            subscription.getCustomer());
      } else {
        log.warn("No user subscription found for deleted subscription {}", subscription.getId());
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

      EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
      com.stripe.model.PaymentMethod paymentMethod =
          (com.stripe.model.PaymentMethod) dataObjectDeserializer.getObject().orElse(null);

      if (paymentMethod != null && paymentMethod.getCustomer() != null) {
        Optional<UserSubscription> userSub =
            subscriptionRepository.findByStripeCustomerId(paymentMethod.getCustomer());

        if (userSub.isPresent()) {
          Long userId = userSub.get().getUserId();
          paymentMethodService.syncUserPaymentMethodsWithStripe(userId, 0, 100);
          log.info("Synced payment methods for user: {} after payment method attached", userId);
        } else {
          log.warn(
              "No user subscription found for customer: {} in payment_method.attached event",
              paymentMethod.getCustomer());
        }
      }
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

  public void handleSetupIntentCreated(Event event) {
    try {
      StripeUtils.logEventStart("setup intent created", event.getId());
      // Setup intent created - typically no action needed
    } catch (Exception e) {
      StripeUtils.logEventError("setup intent created", event.getId(), e);
    }
  }

  public void handleSetupIntentSucceeded(Event event) {
    try {
      StripeUtils.logEventStart("setup intent succeeded", event.getId());

      EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
      com.stripe.model.SetupIntent setupIntent =
          (com.stripe.model.SetupIntent) dataObjectDeserializer.getObject().orElse(null);

      if (setupIntent != null && setupIntent.getPaymentMethod() != null) {
        // Find user by customer ID
        Optional<UserSubscription> userSub =
            subscriptionRepository.findByStripeCustomerId(setupIntent.getCustomer());
        // Payment method is now attached and ready to use
        userSub.ifPresent(
            userSubscription ->
                log.info(
                    "SetupIntent succeeded for user: {}, payment method: {}",
                    userSubscription.getUserId(),
                    setupIntent.getPaymentMethod()));
      }
    } catch (Exception e) {
      StripeUtils.logEventError("setup intent succeeded", event.getId(), e);
    }
  }

  public void handleFinancialConnectionsAccountCreated(Event event) {
    try {
      StripeUtils.logEventStart("financial connections account created", event.getId());
      // Financial connections account created - typically no action needed
    } catch (Exception e) {
      StripeUtils.logEventError("financial connections account created", event.getId(), e);
    }
  }

  public void handleChargeRefunded(Event event) {
    try {
      StripeUtils.logEventStart("charge refunded", event.getId());
      var dataObjectDeserializer = event.getDataObjectDeserializer();
      var charge = (com.stripe.model.Charge) dataObjectDeserializer.getObject().orElse(null);

      if (charge != null && charge.getRefunded() && !charge.getRefunds().getData().isEmpty()) {
        for (com.stripe.model.Refund refund : charge.getRefunds().getData()) {
          refundDisputeService.handleRefund(refund);
        }
      }
    } catch (Exception e) {
      StripeUtils.logEventError("charge refunded", event.getId(), e);
    }
  }

  public void handleDisputeCreated(Event event) {
    try {
      StripeUtils.logEventStart("dispute created", event.getId());
      var dataObjectDeserializer = event.getDataObjectDeserializer();
      var dispute = (com.stripe.model.Dispute) dataObjectDeserializer.getObject().orElse(null);

      if (dispute != null) {
        refundDisputeService.handleDisputeCreated(dispute);
      }
    } catch (Exception e) {
      StripeUtils.logEventError("dispute created", event.getId(), e);
    }
  }

  public void handleDisputeUpdated(Event event) {
    try {
      StripeUtils.logEventStart("dispute updated", event.getId());
      var dataObjectDeserializer = event.getDataObjectDeserializer();
      var dispute = (com.stripe.model.Dispute) dataObjectDeserializer.getObject().orElse(null);

      if (dispute != null) {
        refundDisputeService.handleDisputeUpdated(dispute);
      }
    } catch (Exception e) {
      StripeUtils.logEventError("dispute updated", event.getId(), e);
    }
  }

  public void handleDisputeClosed(Event event) {
    try {
      StripeUtils.logEventStart("dispute closed", event.getId());
      var dataObjectDeserializer = event.getDataObjectDeserializer();
      var dispute = (com.stripe.model.Dispute) dataObjectDeserializer.getObject().orElse(null);

      if (dispute != null) {
        refundDisputeService.handleDisputeClosed(dispute);
      }
    } catch (Exception e) {
      StripeUtils.logEventError("dispute closed", event.getId(), e);
    }
  }

  public void handleDisputeFundsWithdrawn(Event event) {
    try {
      StripeUtils.logEventStart("dispute funds withdrawn", event.getId());
      // Funds withdrawn - typically handled by dispute updated
    } catch (Exception e) {
      StripeUtils.logEventError("dispute funds withdrawn", event.getId(), e);
    }
  }

  public void handleDisputeFundsReinstated(Event event) {
    try {
      StripeUtils.logEventStart("dispute funds reinstated", event.getId());
      // Funds reinstated - typically handled by dispute closed
    } catch (Exception e) {
      StripeUtils.logEventError("dispute funds reinstated", event.getId(), e);
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

    // If not found by subscription ID, try by customer ID (for subscription updates)
    if (userSubOpt.isEmpty()) {
      userSubOpt = subscriptionRepository.findByStripeCustomerId(subscription.getCustomer());
    }

    if (userSubOpt.isPresent()) {
      UserSubscription userSub = userSubOpt.get();

      // Update subscription ID in case it changed
      userSub.setStripeSubscriptionId(subscription.getId());

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

      // Handle plan changes when period transitions
      if (subscription.getItems() != null && !subscription.getItems().getData().isEmpty()) {
        var item = subscription.getItems().getData().getFirst();

        // Periods / cancel flag
        userSub.setCurrentPeriodStart(
            Instant.ofEpochSecond(item.getCurrentPeriodStart())
                .atZone(ZoneId.systemDefault())
                .toOffsetDateTime());
        userSub.setCurrentPeriodEnd(
            Instant.ofEpochSecond(item.getCurrentPeriodEnd())
                .atZone(ZoneId.systemDefault())
                .toOffsetDateTime());
        userSub.setCancelAtPeriodEnd(subscription.getCancelAtPeriodEnd());

        // Derive planKey:interval:CURRENCY
        String lookupKey = item.getPrice() != null ? item.getPrice().getLookupKey() : null;

        // Fallback if lookup_key is missing: build from product.metadata.key + interval + currency
        if ((lookupKey == null || lookupKey.isBlank()) && item.getPrice() != null) {
          try {
            var price = item.getPrice();
            var recurring = price.getRecurring();
            String interval = recurring != null ? recurring.getInterval().toLowerCase() : "month";
            String currency =
                price.getCurrency() != null ? price.getCurrency().toUpperCase() : "EUR";

            String productKey = null;
            if (price.getProductObject() != null
                && price.getProductObject().getMetadata() != null) {
              productKey = price.getProductObject().getMetadata().get("key");
            }
            if (productKey != null) {
              lookupKey = productKey + ":" + interval + ":" + currency;
            }
          } catch (Exception ignored) {
          }
        }

        if (lookupKey != null && !lookupKey.isBlank()) {
          String[] parts = lookupKey.split(":");
          if (parts.length == 3) {
            userSub.setPlanKey(parts[0]);
            userSub.setBillingInterval(parts[1].toLowerCase());
            userSub.setCurrency(parts[2].toUpperCase());
          } else {
            log.warn("Unexpected lookup_key format: {}", lookupKey);
          }
        } else {
          log.warn("Missing lookup_key and fallback for subscription {}", subscription.getId());
        }

        // Seats / quantity
        if (item.getQuantity() != null) {
          userSub.setSeatCount(item.getQuantity());
        }

        userSub.setUpdatedAt(OffsetDateTime.now());
        subscriptionRepository.save(userSub);
      }
    } else {
      log.warn(
          "No user subscription found for subscription ID: {} or customer ID: {}",
          subscription.getId(),
          subscription.getCustomer());
    }
  }
}
