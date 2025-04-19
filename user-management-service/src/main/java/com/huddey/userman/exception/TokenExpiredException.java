package com.huddey.userman.exception;

public class TokenExpiredException extends AuthenticationException {
  public TokenExpiredException(String message) {
    super(message);
  }
}
