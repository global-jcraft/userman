package com.huddey.core.payment.controller;

import static com.huddey.core.userman.constants.Message.SIMPLE_AUTH_REG_SUCCESS;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.huddey.core.common.api.ApiResponse;
import com.huddey.core.common.utils.LocaleUtils;
import com.huddey.core.notification.utils.SecurityUtils;
import com.huddey.core.payment.data.dto.CancelSubscriptionRequest;
import com.huddey.core.payment.data.dto.CancelSubscriptionResponse;
import com.huddey.core.payment.data.dto.CreateCheckoutSessionRequest;
import com.huddey.core.payment.data.dto.CreateCheckoutSessionResponse;
import com.huddey.core.payment.data.entity.UserSubscription;
import com.huddey.core.payment.data.enums.SubscriptionPlan;
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

  public SubscriptionController(SubscriptionService subscriptionService) {
    this.subscriptionService = subscriptionService;
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

  /*@PostMapping
  public ResponseEntity<ApiResponse<Map<String, String>>> createSubscription(
      Authentication authentication, @Valid @RequestBody CreateDirectSubscriptionRequest request) {

    try {
      SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
      Long userId = securityUser.getUser().getId();
      String email = request.getCustomerEmail();

      if (!SecurityUtils.emailEquals(email, securityUser.getUsername())) {
        throw new UserNotFoundException("User does not exist.");
      }

      subscriptionService.createSubscription(userId, email, request);

      Map<String, String> response = new HashMap<>();
      response.put("status", "success");

      return ResponseEntity.ok(ApiResponse.success(response));

    } catch (StripeException e) {
      Map<String, String> error = new HashMap<>();
      error.put("error", e.getMessage());
      return ResponseEntity.badRequest().body(ApiResponse.error(error));
    }
  }*/

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
      planInfo.put("stripePriceId", plan.getStripePriceId());

      plans.put(plan.name(), planInfo);
    }

    return ResponseEntity.ok(plans);
  }
}
