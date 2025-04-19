package com.huddey.userman.exception;

public class AccountDeactivatedException extends AccountStatusException {
  public AccountDeactivatedException(String message) {
    super(message);
  }
}
