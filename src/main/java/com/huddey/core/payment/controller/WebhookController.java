package com.huddey.core.payment.controller;

import static com.huddey.core.payment.data.enums.StripeConstants.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.huddey.core.payment.service.WebhookIdempotencyService;
import com.huddey.core.payment.service.WebhookService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhook")
public class WebhookController {

  @Value("${stripe.webhook.secret}")
  private String webhookSecret;

  private final WebhookService webhookService;
  private final WebhookIdempotencyService idempotencyService;

  public WebhookController(
      WebhookService webhookService, WebhookIdempotencyService idempotencyService) {
    this.webhookService = webhookService;
    this.idempotencyService = idempotencyService;
  }

  @PostMapping("/stripe")
  public ResponseEntity<String> handleStripeWebhook(
      @RequestBody String payload,
      @RequestHeader(value = "Stripe-Signature", required = true) String sigHeader) {

    log.debug(
        "Received Stripe webhook with payload length: {}", payload != null ? payload.length() : 0);

    // Validate required parameters
    if (payload == null || payload.trim().isEmpty()) {
      log.warn("Webhook rejected: Empty payload");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Empty payload");
    }

    if (sigHeader == null || sigHeader.trim().isEmpty()) {
      log.warn("Webhook rejected: Missing Stripe-Signature header");
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing signature");
    }

    if (webhookSecret == null || webhookSecret.trim().isEmpty()) {
      log.error("Webhook configuration error: Missing webhook secret");
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Configuration error");
    }

    Event event;
    try {
      event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
      log.debug("Webhook signature verified successfully for event: {}", event.getId());
    } catch (SignatureVerificationException e) {
      log.warn("Webhook signature verification failed: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
    } catch (Exception e) {
      log.error("Webhook processing error during signature verification: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook processing error");
    }

    // Atomic idempotency check and mark as processing
    if (!idempotencyService.markAsProcessing(event.getId(), event.getType())) {
      log.debug("Webhook event {} already processed successfully", event.getId());
      return ResponseEntity.ok("Already processed");
    }

    // Handle the event
    try {
      switch (event.getType()) {
        case CHECKOUT_COMPLETED:
          webhookService.handleCheckoutSessionCompleted(event);
          break;
        case SUBSCRIPTION_CREATED:
          webhookService.handleSubscriptionCreated(event);
          break;
        case SUBSCRIPTION_UPDATED:
          webhookService.handleSubscriptionUpdated(event);
          break;
        case SUBSCRIPTION_DELETED:
          webhookService.handleSubscriptionDeleted(event);
          break;
        case INVOICE_PAYMENT_OK:
          webhookService.handleInvoicePaymentSucceeded(event);
          break;
        case INVOICE_PAYMENT_KO:
          webhookService.handleInvoicePaymentFailed(event);
          break;
        case INVOICE_CREATED:
          webhookService.handleInvoiceCreated(event);
          break;
        case INVOICE_PAID:
          webhookService.handleInvoicePaid(event);
          break;
        case INVOICE_FINALIZED:
          webhookService.handleInvoiceFinalized(event);
          break;
        case CHARGE_SUCCEEDED:
          webhookService.handleChargeSucceeded(event);
          break;
        case PAYMENT_METHOD_ATTACHED:
          webhookService.handlePaymentMethodAttached(event);
          break;
        case PAYMENT_INTENT_SUCCEEDED:
          webhookService.handlePaymentIntentSucceeded(event);
          break;
        case PAYMENT_INTENT_CREATED:
          webhookService.handlePaymentIntentCreated(event);
          break;
        case SETUP_INTENT_CREATED:
          webhookService.handleSetupIntentCreated(event);
          break;
        case SETUP_INTENT_SUCCEEDED:
          webhookService.handleSetupIntentSucceeded(event);
          break;
        default:
          log.error("Unhandled event type: {}", event.getType());
      }
      // Mark as successfully processed
      idempotencyService.markAsSuccess(event.getId());
    } catch (Exception e) {
      log.error("Error processing webhook event {}: {}", event.getType(), e.getMessage(), e);
      // Mark as failed
      idempotencyService.markAsFailed(event.getId(), e.getMessage());
      // Return 500 to signal Stripe to retry the webhook
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Webhook processing failed");
    }

    log.debug("Webhook event {} processed successfully", event.getId());
    return ResponseEntity.ok("Success");
  }
}
