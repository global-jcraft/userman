package com.huddey.core.userman.exception;

public class InvalidInputException extends AuthenticationException {
  public InvalidInputException(String message) {
    super(message);
  }
}
