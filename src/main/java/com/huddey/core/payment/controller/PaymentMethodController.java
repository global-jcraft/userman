package com.huddey.core.payment.controller;

import static com.huddey.core.userman.constants.Message.*;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.huddey.core.common.api.ApiResponse;
import com.huddey.core.common.utils.LocaleUtils;
import com.huddey.core.payment.data.dto.AddPaymentMethodRequest;
import com.huddey.core.payment.data.dto.PaymentMethodDto;
import com.huddey.core.payment.data.dto.UpdatePaymentMethodRequest;
import com.huddey.core.payment.data.enums.PaymentMethodType;
import com.huddey.core.payment.exception.PaymentMethodNotFoundException;
import com.huddey.core.payment.service.PaymentMethodService;
import com.huddey.core.payment.service.StripeSetupIntentService;
import com.huddey.core.userman.data.SecurityUser;
import com.stripe.exception.StripeException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {

  private final PaymentMethodService paymentMethodService;
  private final StripeSetupIntentService stripeSetupIntentService;

  @GetMapping
  public ResponseEntity<ApiResponse<Page<PaymentMethodDto>>> getPaymentMethods(
      Authentication authentication,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {

    try {
      SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
      Long userId = securityUser.getUser().getId();

      log.debug("Retrieving payment methods for user: {}, page: {}, size: {}", userId, page, size);

      Page<PaymentMethodDto> paymentMethods =
          paymentMethodService.getUserPaymentMethods(userId, page, size);

      log.debug(
          "Retrieved {} payment methods for user: {}", paymentMethods.getTotalElements(), userId);

      return ResponseEntity.ok(
          ApiResponse.success(LocaleUtils.getMessage(PAYMENT_METHOD_LIST_SUCCESS), paymentMethods));
    } catch (StripeException e) {
      log.error("Stripe error fetching payment methods", e);
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to fetch payment methods: " + e.getMessage()));
    }
  }

  @PostMapping
  public ResponseEntity<ApiResponse<PaymentMethodDto>> addPaymentMethod(
      Authentication authentication, @Valid @RequestBody AddPaymentMethodRequest request) {
    PaymentMethodDto paymentMethod = null;
    Long userId = null;
    try {
      SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
      userId = securityUser.getUser().getId();

      log.debug(
          "Adding payment method for user: {}, type: {}, setAsDefault: {}",
          userId,
          request.getType(),
          request.isSetAsDefault());

      paymentMethod = paymentMethodService.addPaymentMethod(userId, request);

    } catch (StripeException e) {
      log.error("Stripe error adding payment method", e);
      return ResponseEntity.badRequest()
          .body(
              ApiResponse.error(
                  LocaleUtils.getMessage(PAYMENT_METHOD_ADD_ERROR) + ": " + e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(LocaleUtils.getMessage(PAYMENT_SERVICE_ERROR)));
    }

    log.debug(
        "Successfully created SetupIntent {} for user: {}",
        paymentMethod.getSetupIntentId(),
        userId);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(LocaleUtils.getMessage(PAYMENT_METHOD_ADD_SUCCESS), paymentMethod));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<PaymentMethodDto>> updatePaymentMethod(
      Authentication authentication,
      @PathVariable Long id,
      @Valid @RequestBody UpdatePaymentMethodRequest request) {

    try {
      SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
      Long userId = securityUser.getUser().getId();

      log.debug("Updating payment method {} for user: {}", id, userId);

      PaymentMethodDto paymentMethod =
          paymentMethodService.updatePaymentMethod(userId, id, request);

      log.debug("Successfully updated payment method {} for user: {}", id, userId);

      return ResponseEntity.ok(
          ApiResponse.success(
              LocaleUtils.getMessage(PAYMENT_METHOD_UPDATE_SUCCESS), paymentMethod));

    } catch (PaymentMethodNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (StripeException e) {
      log.error("Stripe error updating payment method", e);
      return ResponseEntity.badRequest()
          .body(
              ApiResponse.error(
                  LocaleUtils.getMessage(PAYMENT_METHOD_UPDATE_ERROR) + ": " + e.getMessage()));
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> removePaymentMethod(
      Authentication authentication, @PathVariable Long id) {
    boolean removed = false;
    try {
      SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
      Long userId = securityUser.getUser().getId();

      log.debug("Removing payment method {} for user: {}", id, userId);

      removed = paymentMethodService.removePaymentMethod(userId, id);

      if (!removed) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ApiResponse.error(LocaleUtils.getMessage(PAYMENT_SERVICE_UNAVAILABLE)));
      }

      log.debug("Successfully removed payment method {} for user: {}", id, userId);
      return ResponseEntity.ok(
          ApiResponse.success(LocaleUtils.getMessage(PAYMENT_METHOD_REMOVE_SUCCESS), null));

    } catch (PaymentMethodNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (StripeException e) {
      log.error("Stripe error removing payment method", e);
      return ResponseEntity.badRequest()
          .body(
              ApiResponse.error(
                  LocaleUtils.getMessage(PAYMENT_METHOD_REMOVE_ERROR) + ": " + e.getMessage()));
    } catch (Exception e) {
      if (!removed) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ApiResponse.error(LocaleUtils.getMessage(PAYMENT_SERVICE_UNAVAILABLE)));
      }
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error(LocaleUtils.getMessage(PAYMENT_SERVICE_ERROR)));
    }
  }

  @PutMapping("/{id}/set-default")
  public ResponseEntity<ApiResponse<PaymentMethodDto>> setAsDefault(
      Authentication authentication, @PathVariable Long id) {

    try {
      SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
      Long userId = securityUser.getUser().getId();

      log.debug("Setting payment method {} as default for user: {}", id, userId);

      PaymentMethodDto paymentMethod = paymentMethodService.setAsDefault(userId, id);

      log.debug("Successfully set payment method {} as default for user: {}", id, userId);

      return ResponseEntity.ok(
          ApiResponse.success(
              LocaleUtils.getMessage(PAYMENT_METHOD_DEFAULT_SET_SUCCESS), paymentMethod));

    } catch (PaymentMethodNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PutMapping("/{id}/set-backup")
  public ResponseEntity<ApiResponse<PaymentMethodDto>> setAsBackup(
      Authentication authentication, @PathVariable Long id) {

    try {
      SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
      Long userId = securityUser.getUser().getId();

      log.debug("Setting payment method {} as backup for user: {}", id, userId);

      PaymentMethodDto paymentMethod = paymentMethodService.setAsBackup(userId, id);

      log.debug("Successfully set payment method {} as backup for user: {}", id, userId);

      return ResponseEntity.ok(
          ApiResponse.success(
              LocaleUtils.getMessage(PAYMENT_METHOD_BACKUP_SET_SUCCESS), paymentMethod));

    } catch (PaymentMethodNotFoundException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/setup-intent")
  public ResponseEntity<ApiResponse<String>> createSetupIntent(
      Authentication authentication, @RequestParam PaymentMethodType type) {

    try {
      SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
      Long userId = securityUser.getUser().getId();

      log.debug("Creating SetupIntent for user: {} with type: {}", userId, type);

      String customerId = paymentMethodService.getCustomerIdForUser(userId);
      var setupIntent = stripeSetupIntentService.createSetupIntent(customerId, type);

      log.debug("Created SetupIntent {} for user: {}", setupIntent.getId(), userId);

      return ResponseEntity.ok(
          ApiResponse.success("SetupIntent created successfully", setupIntent.getClientSecret()));

    } catch (StripeException e) {
      log.error("Stripe error creating SetupIntent", e);
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Failed to create SetupIntent: " + e.getMessage()));
    }
  }
}
