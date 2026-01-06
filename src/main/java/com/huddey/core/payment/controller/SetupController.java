package com.huddey.core.payment.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.huddey.core.payment.service.StripeSetupService;
import com.huddey.core.userman.data.SecurityUser;
import com.stripe.exception.StripeException;

@RestController
@RequestMapping("/api/v1/setup")
public class SetupController {

  private final StripeSetupService stripeSetupService;

  public SetupController(StripeSetupService stripeSetupService) {
    this.stripeSetupService = stripeSetupService;
  }

  /**
   * WARNING: Only run this ONCE to set up your Stripe products and prices Remove this endpoint
   * after initial setup for security
   */
  @PostMapping("/setup-stripe")
  @PreAuthorize(
      "isAuthenticated() and hasAnyAuthority('ROLE_USER', 'ROLE_CONTENT_CREATOR', 'ROLE_ADMIN')")
  public ResponseEntity<Map<String, String>> setupStripe(Authentication authentication) {
    try {
      SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
      if (securityUser != null) {
        stripeSetupService.upsertCatalog();
      }

      Map<String, String> response = new HashMap<>();
      response.put("status", "success");
      response.put(
          "message", "Stripe products and prices created successfully. Check logs for price IDs.");

      return ResponseEntity.ok(response);

    } catch (StripeException e) {
      Map<String, String> error = new HashMap<>();
      error.put("error", e.getMessage());
      return ResponseEntity.badRequest().body(error);
    }
  }
}
