package com.huddey.core.payment.controller;

import static com.huddey.core.userman.constants.Message.*;

import java.util.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.huddey.core.common.api.ApiResponse;
import com.huddey.core.common.utils.LocaleUtils;
import com.huddey.core.notification.utils.SecurityUtils;
import com.huddey.core.payment.data.dto.*;
import com.huddey.core.payment.data.entity.UserSubscription;
import com.huddey.core.payment.data.enums.SubscriptionPlan;
import com.huddey.core.payment.service.BillingService;
import com.huddey.core.payment.service.ProductCatalogService;
import com.huddey.core.payment.service.SubscriptionService;
import com.huddey.core.payment.utils.StripeUtils;
import com.huddey.core.userman.data.SecurityUser;
import com.huddey.core.userman.exception.UserNotFoundException;
import com.stripe.exception.StripeException;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/subscription")
@CrossOrigin(origins = "*")
public class SubscriptionController {

  private final SubscriptionService subscriptionService;
  private final ProductCatalogService productCatalogService;
  private final BillingService billingService;

  public SubscriptionController(
      SubscriptionService subscriptionService,
      ProductCatalogService productCatalogService,
      BillingService billingService) {
    this.subscriptionService = subscriptionService;
    this.productCatalogService = productCatalogService;
    this.billingService = billingService;
  }

  @PostMapping("/create-checkout-session")
  public ResponseEntity<ApiResponse<CreateCheckoutSessionResponse>> createCheckoutSession(
      Authentication authentication, @Valid @RequestBody CreateCheckoutSessionRequest request) {

    SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
    try {
      log.debug("Creating checkout session for plan: {}", request.getPlan());

      Long userId = securityUser.getUser().getId();
      String email = request.getCustomerEmail();

      if (!SecurityUtils.emailEquals(email, securityUser.getUsername())) {
        throw new UserNotFoundException("User does not exist.");
      }

      String checkoutUrl = subscriptionService.createCheckoutSession(userId, email, request);
      log.debug("Successfully created checkout session for user: {}", userId);

      return ResponseEntity.ok(
          ApiResponse.success(
              LocaleUtils.getMessage(SIMPLE_AUTH_REG_SUCCESS),
              CreateCheckoutSessionResponse.success(
                  checkoutUrl, StripeUtils.extractSessionId(checkoutUrl))));

    } catch (StripeException e) {
      log.error("Stripe error creating checkout session", e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              ApiResponse.error(
                  "Failed to create checkout session: " + e.getMessage(), "STRIPE_ERROR"));

    } catch (Exception e) {
      log.error("Unexpected error creating checkout session", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("An unexpected error occurred", "INTERNAL_ERROR"));
    }
  }

  @PutMapping("/update")
  public ResponseEntity<Map<String, String>> updateSubscription(
      @RequestBody Map<String, Object> request) {

    try {
      Long userId = Long.valueOf(request.get("userId").toString());
      String planName = request.get("plan").toString();

      SubscriptionPlan plan = SubscriptionPlan.valueOf(planName);

      subscriptionService.updateSubscription(userId, plan);

      Map<String, String> response = new HashMap<>();
      response.put("status", "success");

      return ResponseEntity.ok(response);

    } catch (StripeException e) {
      Map<String, String> error = new HashMap<>();
      error.put("error", e.getMessage());
      return ResponseEntity.badRequest().body(error);
    }
  }

  @PostMapping("/cancel/{id}")
  public ResponseEntity<ApiResponse<CancelSubscriptionResponse>> cancelSubscription(
      Authentication authentication,
      @PathVariable Long id,
      @Valid @RequestBody CancelSubscriptionRequest request) {

    try {
      SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
      if (!Objects.equals(securityUser.getUser().getId(), id)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }

      return ResponseEntity.ok(
          ApiResponse.success(
              subscriptionService.cancelSubscription(id, request.isCancelAtPeriodEnd())));

    } catch (StripeException e) {
      var errorResponse =
          new CancelSubscriptionResponse(true, "Subscription cancelled successfully");
      return ResponseEntity.badRequest().body(ApiResponse.error(errorResponse));
    }
  }

  @GetMapping("/status/{userId}")
  public ResponseEntity<UserSubscription> getSubscriptionStatus(@PathVariable Long userId) {
    Optional<UserSubscription> subscription = subscriptionService.getUserSubscription(userId);

    return subscription.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/plans")
  public ResponseEntity<Map<String, Object>> getAvailablePlans() {
    Map<String, Object> plans = new HashMap<>();

    for (SubscriptionPlan plan : SubscriptionPlan.values()) {
      Map<String, Object> planInfo = new HashMap<>();
      planInfo.put("name", plan.name());
      planInfo.put("displayName", plan.getDisplayName());
      planInfo.put("price", plan.getPrice());
      planInfo.put("interval", plan.getInterval());
      planInfo.put("stripePriceId", plan.getInternalPriceId());

      plans.put(plan.name(), planInfo);
    }

    return ResponseEntity.ok(plans);
  }

  @GetMapping("/products")
  public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProductsWithPrices() {
    try {
      log.debug("Retrieving all products with prices");
      return ResponseEntity.ok(
          ApiResponse.success(
              LocaleUtils.getMessage(STRIPE_PRODUCT_LIST_SUCCESS),
              productCatalogService.getAllProducts()));
    } catch (StripeException e) {
      log.error("Stripe error syncing products", e);
      return ResponseEntity.badRequest()
          .body(ApiResponse.error(LocaleUtils.getMessage(STRIPE_PRODUCT_LIST_ERROR)));
    }
  }

  @GetMapping("/billing/history")
  public ResponseEntity<ApiResponse<BillingHistoryResponse>> getBillingHistory(
      Authentication authentication,
      @RequestParam(defaultValue = "10") Integer limit,
      @RequestParam(required = false) String startingAfter) {

    try {
      SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
      Long userId = securityUser.getUser().getId();

      log.debug(
          "Processing billing history request for user: {}, limit: {}, startingAfter: {}",
          userId,
          limit,
          startingAfter);

      BillingHistoryResponse history =
          billingService.getBillingHistory(userId, limit, startingAfter);

      log.debug(
          "Successfully retrieved billing history for user: {} with {} invoices",
          userId,
          history.getInvoices().size());

      return ResponseEntity.ok(
          ApiResponse.success(LocaleUtils.getMessage(BILLING_HISTORY_SUCCESS), history));

    } catch (StripeException e) {
      log.error("Stripe error retrieving billing history", e);
      return ResponseEntity.badRequest()
          .body(ApiResponse.error(LocaleUtils.getMessage(BILLING_HISTORY_ERROR)));
    } catch (Exception e) {
      log.error("Error retrieving billing history", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(LocaleUtils.getMessage(GLOBAL_INTERNAL_UNEXPECTED_ERROR)));
    }
  }

  @GetMapping("/billing/invoice/{invoiceId}")
  public ResponseEntity<ApiResponse<InvoiceDto>> getInvoice(
      Authentication authentication, @PathVariable String invoiceId) {

    try {
      SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
      Long userId = securityUser.getUser().getId();

      log.debug(
          "Processing invoice retrieval request for user: {}, invoiceId: {}", userId, invoiceId);

      InvoiceDto invoice = billingService.getInvoice(userId, invoiceId);

      log.debug(
          "Successfully retrieved invoice: {} for user: {}, status: {}",
          invoiceId,
          userId,
          invoice.getStatus());

      return ResponseEntity.ok(
          ApiResponse.success(LocaleUtils.getMessage(INVOICE_RETRIEVE_SUCCESS), invoice));

    } catch (StripeException e) {
      log.error("Stripe error retrieving invoice", e);
      return ResponseEntity.badRequest()
          .body(ApiResponse.error(LocaleUtils.getMessage(INVOICE_RETRIEVE_ERROR)));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ApiResponse.error(LocaleUtils.getMessage(GLOBAL_AUTH_ERROR)));
    } catch (Exception e) {
      log.error("Error retrieving invoice", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(LocaleUtils.getMessage(GLOBAL_INTERNAL_UNEXPECTED_ERROR)));
    }
  }

  @GetMapping("/billing/invoice/{invoiceId}/pdf")
  public ResponseEntity<?> downloadInvoicePdf(
      Authentication authentication, @PathVariable String invoiceId) {

    try {
      SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
      Long userId = securityUser.getUser().getId();

      log.debug("Processing PDF download request for user: {}, invoiceId: {}", userId, invoiceId);

      String pdfUrl = billingService.getInvoicePdfUrl(userId, invoiceId);

      if (pdfUrl == null) {
        log.debug("No PDF URL available for invoice: {} and user: {}", invoiceId, userId);
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(LocaleUtils.getMessage(INVOICE_PDF_ERROR)));
      }

      log.debug("Redirecting user: {} to PDF URL for invoice: {}", userId, invoiceId);

      // Return redirect to Stripe's PDF URL
      return ResponseEntity.status(HttpStatus.FOUND).header("Location", pdfUrl).build();

    } catch (StripeException e) {
      log.error("Stripe error retrieving invoice PDF", e);
      return ResponseEntity.badRequest()
          .body(ApiResponse.error(LocaleUtils.getMessage(INVOICE_PDF_ERROR)));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ApiResponse.error(LocaleUtils.getMessage(GLOBAL_AUTH_ERROR)));
    } catch (Exception e) {
      log.error("Error retrieving invoice PDF", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(LocaleUtils.getMessage(GLOBAL_INTERNAL_UNEXPECTED_ERROR)));
    }
  }
}
