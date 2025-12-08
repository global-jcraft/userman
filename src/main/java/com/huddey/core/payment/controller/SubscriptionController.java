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
import com.huddey.core.payment.config.PlanKey;
import com.huddey.core.payment.data.dto.*;
import com.huddey.core.payment.data.entity.UserSubscription;
import com.huddey.core.payment.exception.StripeServiceException;
import com.huddey.core.payment.service.BillingService;
import com.huddey.core.payment.service.ProductCatalogService;
import com.huddey.core.payment.service.SubscriptionService;
import com.huddey.core.payment.utils.StripeUtils;
import com.huddey.core.userman.data.SecurityUser;
import com.huddey.core.userman.exception.UserNotFoundException;
import com.stripe.exception.StripeException;
import com.stripe.param.SubscriptionUpdateParams;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/subscription")
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

  /**
   * CHECKOUT: lookup-key pricing, seat-based quantity, multi-currency
   *
   * @param authentication
   * @param request
   * @return
   */
  @PostMapping("/create-checkout-session")
  public ResponseEntity<ApiResponse<CreateCheckoutSessionResponse>> createCheckoutSession(
      Authentication authentication, @Valid @RequestBody CreateCheckoutSessionRequest request) {

    SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
    try {
      log.debug("Creating checkout session for plan: {}", request);

      Long userId = securityUser.getUser().getId();
      String email = request.getCustomerEmail();

      if (!SecurityUtils.emailEquals(email, securityUser.getUsername())) {
        throw new UserNotFoundException("User does not exist.");
      }

      // Normalize planKey using enum
      request.setPlanKey(normalizePlanKey(request.getPlanKey()));

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

  /**
   * PLAN SWITCH: switch price by lookup key, preserve quantity
   *
   * @param authentication
   * @param request
   * @return
   */
  @PutMapping("/plan/switch")
  public ResponseEntity<ApiResponse<Map<String, String>>> switchPlan(
      Authentication authentication, @Valid @RequestBody SwitchPlanRequest request) {

    try {
      SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
      Long userId = securityUser.getUser().getId();

      log.debug(
          "Switch plan: userId={}, newPlanKey={}, interval={}, currency={}",
          userId,
          request.getPlanKey(),
          request.getInterval(),
          request.getCurrency());

      subscriptionService.switchPlan(
          userId, request.getPlanKey(), request.getInterval(), request.getCurrency());

      Map<String, String> resp = new HashMap<>();
      resp.put("status", "success");
      return ResponseEntity.ok(ApiResponse.success(resp));

    } catch (StripeServiceException | StripeException e) {
      log.error("Stripe error switching plan", e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error("Failed to switch plan: " + e.getMessage(), "STRIPE_ERROR"));
    } catch (Exception e) {
      log.error("Unexpected error switching plan", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("An unexpected error occurred", "INTERNAL_ERROR"));
    }
  }

  @PutMapping("/seats/update")
  public ResponseEntity<ApiResponse<Map<String, Object>>> updateSeatCount(
      Authentication authentication, @Valid @RequestBody UpdateSeatCountRequest request) {

    try {
      SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
      Long userId = securityUser.getUser().getId();

      SubscriptionUpdateParams.ProrationBehavior behavior =
          parseProrationBehavior(request.getProrationBehavior());

      log.debug(
          "Change seats: userId={}, newSeatCount={}, prorationBehavior={}",
          userId,
          request.getNewSeatCount(),
          behavior);

      subscriptionService.changeSeatCount(userId, request.getNewSeatCount(), behavior);

      Map<String, Object> resp = new HashMap<>();
      resp.put("status", "success");
      resp.put("newSeatCount", request.getNewSeatCount());
      resp.put("prorationBehavior", behavior.name());
      return ResponseEntity.ok(ApiResponse.success(resp));

    } catch (StripeServiceException | StripeException e) {
      log.error("Stripe error updating seat count", e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponse.error("Failed to update seats: " + e.getMessage(), "STRIPE_ERROR"));
    } catch (Exception e) {
      log.error("Unexpected error updating seat count", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("An unexpected error occurred", "INTERNAL_ERROR"));
    }
  }

  private SubscriptionUpdateParams.ProrationBehavior parseProrationBehavior(String v) {
    if (v == null) return SubscriptionUpdateParams.ProrationBehavior.CREATE_PRORATIONS;
    try {
      return SubscriptionUpdateParams.ProrationBehavior.valueOf(v.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      return SubscriptionUpdateParams.ProrationBehavior.CREATE_PRORATIONS;
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
      log.error("Stripe error cancelling subscription", e);
      var errorResponse = new CancelSubscriptionResponse(false, "Failed to cancel subscription");
      return ResponseEntity.badRequest().body(ApiResponse.error(errorResponse));
    }
  }

  @GetMapping("/status/{userId}")
  public ResponseEntity<SubscriptionStatusResponse> getSubscriptionStatus(
      @PathVariable Long userId) {
    Optional<UserSubscription> subscription = subscriptionService.getUserSubscription(userId);
    return subscription
        .map(SubscriptionStatusResponse::fromUserSubscription)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Backward-compatible endpoint; now proxies product catalog instead of enum
   *
   * @return
   */
  @GetMapping("/plans")
  public ResponseEntity<ApiResponse<List<ProductResponse>>> getAvailablePlans() {
    try {
      log.debug("Retrieving available plans (proxying product catalog)");
      return ResponseEntity.ok(
          ApiResponse.success(
              LocaleUtils.getMessage(STRIPE_PRODUCT_LIST_SUCCESS),
              productCatalogService.getAllProducts()));
    } catch (StripeException e) {
      log.error("Stripe error retrieving plans", e);
      return ResponseEntity.badRequest()
          .body(ApiResponse.error(LocaleUtils.getMessage(STRIPE_PRODUCT_LIST_ERROR)));
    }
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

  private String normalizePlanKey(String planKey) {
    if (planKey == null) throw new IllegalArgumentException("Plan key is required");

    // Try exact match first
    for (PlanKey pk : PlanKey.values()) {
      if (pk.getKey().equalsIgnoreCase(planKey)) return pk.getKey();
    }

    // Try with huddey_ prefix
    String withPrefix = planKey.startsWith("huddey_") ? planKey : "huddey_" + planKey;
    for (PlanKey pk : PlanKey.values()) {
      if (pk.getKey().equalsIgnoreCase(withPrefix)) return pk.getKey();
    }

    throw new IllegalArgumentException("Invalid plan key: " + planKey);
  }
}
