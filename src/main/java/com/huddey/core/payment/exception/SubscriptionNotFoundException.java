package com.huddey.core.payment.exception;

import com.huddey.core.userman.exception.BaseException;

public class SubscriptionNotFoundException extends BaseException {
  public SubscriptionNotFoundException(String message) {
    super(message);
  }
}
