package com.huddey.core.userman.security.token;

import org.springframework.context.annotation.Configuration;

import com.huddey.core.userman.configuration.JwtTokenProvider;
import com.huddey.core.userman.data.SecurityUser;

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
  public void generateAnsSetToken(HttpServletResponse response, SecurityUser user) {
    this.accessToken = jwtTokenProvider.generateAccessToken(user);
    this.refreshToken = jwtTokenProvider.generateRefreshToken(user);

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
