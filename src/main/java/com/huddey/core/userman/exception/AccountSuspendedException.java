package com.huddey.core.userman.exception;

public class AccountSuspendedException extends AccountStatusException {
  public AccountSuspendedException(String message) {
    super(message);
  }
}
