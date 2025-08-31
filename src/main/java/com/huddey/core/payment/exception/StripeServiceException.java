package com.huddey.core.payment.exception;

public class StripeServiceException extends RuntimeException {
  public StripeServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
