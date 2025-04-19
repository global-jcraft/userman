package com.huddey.userman.security.token;

import org.springframework.context.annotation.Configuration;

import com.huddey.userman.auth.JwtTokenProvider;
import com.huddey.userman.data.SecurityUser;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class MobileTokenGenerationStrategy implements TokenGenerationStrategy {
  private final JwtTokenProvider jwtTokenProvider;
  String accessToken;
  String refreshToken;

  public MobileTokenGenerationStrategy(JwtTokenProvider jwtTokenProvider) {
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Override
  public void generateAndSetToken(
      HttpServletResponse response, SecurityUser user, boolean rememberMe) {
    this.accessToken = jwtTokenProvider.generateAccessToken(user, rememberMe);
    this.refreshToken = jwtTokenProvider.generateRefreshToken(user, rememberMe);

    // Set the tokens in the response body
    response.setHeader("Access-Token", accessToken);
    response.setHeader("Refresh-Token", refreshToken);
  }

  @Override
  public String getAccessToken() {
    return this.accessToken;
  }

  @Override
  public String getRefreshToken() {
    return this.refreshToken;
  }
}
