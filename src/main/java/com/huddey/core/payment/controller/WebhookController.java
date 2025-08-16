package com.huddey.core.payment.controller;

import static com.huddey.core.payment.data.enums.StripeConstants.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

  public WebhookController(WebhookService webhookService) {
    this.webhookService = webhookService;
  }

  @PostMapping("/stripe")
  public ResponseEntity<String> handleStripeWebhook(
      @RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {

    log.debug("Entering WebhookController.handleStripeWebhook() method");
    Event event;

    try {
      event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
    } catch (SignatureVerificationException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
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
        default:
          log.error("Unhandled event type: {}", event.getType());
      }
    } catch (Exception e) {
      log.error("Error processing webhook event {}: {}", event.getType(), e.getMessage(), e);
    }

    return ResponseEntity.ok("Success");
  }
}
