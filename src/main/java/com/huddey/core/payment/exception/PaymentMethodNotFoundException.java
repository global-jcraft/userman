package com.huddey.core.payment.exception;

public class PaymentMethodNotFoundException extends RuntimeException {
  public PaymentMethodNotFoundException(String message) {
    super(message);
  }
}
