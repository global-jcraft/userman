package com.huddey.userman.exception;

public class InvalidTokenException extends AuthenticationException {
  public InvalidTokenException(String message) {
    super(message);
  }
}
