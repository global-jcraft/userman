package com.huddey.core.payment.utils;

public class StripeUtils {

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
}
