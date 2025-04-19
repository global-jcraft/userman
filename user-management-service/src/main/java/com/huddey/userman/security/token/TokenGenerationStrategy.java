package com.huddey.userman.security.token;

import com.huddey.userman.data.SecurityUser;

import jakarta.servlet.http.HttpServletResponse;

public interface TokenGenerationStrategy {
  void generateAndSetToken(HttpServletResponse response, SecurityUser user, boolean rememberMe);

  String getAccessToken();

  String getRefreshToken();
}
