package com.huddey.core.payment.utils;

import com.huddey.core.payment.data.entity.UserSubscription;
import com.huddey.core.payment.repository.UserSubscriptionRepository;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class StripeUtils {

    private StripeUtils(){}

  /** Extract session ID from Stripe Checkout URL */
  public static String extractSessionId(String checkoutUrl) {
    try {
      // Extract session ID from URL like: https://checkout.stripe.com/c/pay/cs_test_xxx#fidkdWxO...
      String[] parts = checkoutUrl.split("/");
      for (String part : parts) {
        if (part.startsWith("cs_")) {
          return part.split("#")[0]; // Remove any fragment
        }
      }
      return null;
    } catch (Exception e) {
      return null;
    }
  }

    /**
     * Extracts and validates a Subscription object from a Stripe event
     */
    public static Optional<Subscription> extractSubscription(Event event) {
        try {
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            Subscription subscription = (Subscription) dataObjectDeserializer.getObject().orElse(null);

            if (subscription == null) {
                log.debug("No subscription data found in event: {}", event.getId());
                return Optional.empty();
            }

            return Optional.of(subscription);
        } catch (Exception e) {
            log.error("Error extracting subscription from event: {}", event.getId(), e);
            return Optional.empty();
        }
    }

    /**
     * Extracts and validates an Invoice object from a Stripe event
     */
    public static Optional<Invoice> extractInvoice(Event event) {
        try {
            EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
            Invoice invoice = (Invoice) dataObjectDeserializer.getObject().orElse(null);

            if (invoice == null) {
                log.debug("No invoice data found in event: {}", event.getId());
                return Optional.empty();
            }

            return Optional.of(invoice);
        } catch (Exception e) {
            log.error("Error extracting invoice from event: {}", event.getId(), e);
            return Optional.empty();
        }
    }

    /**
     * Finds a UserSubscription by Stripe subscription ID with proper error handling
     */
    public static Optional<UserSubscription> findUserSubscription(
            String stripeSubscriptionId,
            UserSubscriptionRepository repository) {

        if (stripeSubscriptionId == null || stripeSubscriptionId.trim().isEmpty()) {
            log.debug("Stripe subscription ID is null or empty");
            return Optional.empty();
        }

        try {
            Optional<UserSubscription> userSub = repository.findByStripeSubscriptionId(stripeSubscriptionId);

            if (userSub.isEmpty()) {
                log.debug("No subscription found in database for Stripe subscription ID: {}", stripeSubscriptionId);
            }

            return userSub;
        } catch (Exception e) {
            log.error("Error finding subscription by ID: {}", stripeSubscriptionId, e);
            return Optional.empty();
        }
    }

    /**
     * Safe method to get subscription ID from invoice with null checks
     */
    public static Optional<String> getSubscriptionIdFromInvoice(Invoice invoice) {
        if (invoice == null) {
            return Optional.empty();
        }

        // Try to get subscription from invoice lines
        if (invoice.getLines() != null && !invoice.getLines().getData().isEmpty()) {
            for (var lineItem : invoice.getLines().getData()) {
                if (lineItem.getSubscription() != null && !lineItem.getSubscription().trim().isEmpty()) {
                    return Optional.of(lineItem.getSubscription());
                }
            }
        }

        log.debug("Invoice {} has no associated subscription", invoice.getId());
        return Optional.empty();
    }


    /**
     * Formats currency amount from cents to readable format
     */
    public static String formatAmount(Long amountInCents) {
        if (amountInCents == null) {
            return "unknown";
        }
        return String.format("%.2f", amountInCents / 100.0);
    }

    /**
     * Alternative method to find subscription by customer ID if invoice doesn't contain subscription directly
     */
    public static Optional<UserSubscription> findUserSubscriptionByCustomerId(
            String customerId,
            UserSubscriptionRepository repository) {

        if (customerId == null || customerId.trim().isEmpty()) {
            log.debug("Customer ID is null or empty");
            return Optional.empty();
        }

        try {
            Optional<UserSubscription> userSub = repository.findByStripeCustomerId(customerId);

            if (userSub.isEmpty()) {
                log.debug("No subscription found in database for customer ID: {}", customerId);
            }

            return userSub;
        } catch (Exception e) {
            log.error("Error finding subscription by customer ID: {}", customerId, e);
            return Optional.empty();
        }
    }


    /**
     * Logs webhook event processing start with consistent format
     */
    public static void logEventStart(String eventType, String eventId) {
        log.info("Processing {} event: {}", eventType, eventId);
    }

    /**
     * Logs webhook event processing error with consistent format
     */
    public static void logEventError(String eventType, String eventId, Exception e) {
        log.error("Error processing {} event: {}", eventType, eventId, e);
    }

}
