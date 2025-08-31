package com.huddey.core.userman.token;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseCookie;

import com.huddey.core.userman.auth.JwtTokenProvider;
import com.huddey.core.userman.data.SecurityUser;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;

@Configuration
public class WebTokenGenerationStrategy implements TokenGenerationStrategy {
  private final JwtTokenProvider jwtTokenProvider;
  String accessToken;
  String refreshToken;
  @Getter ResponseCookie accessTokenCookie;
  @Getter ResponseCookie refreshTokenCookie;

  public WebTokenGenerationStrategy(JwtTokenProvider jwtTokenProvider) {
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Override
  public void generateAndSetToken(
      HttpServletResponse response, SecurityUser user, boolean rememberMe) {
    this.accessToken = jwtTokenProvider.generateAccessToken(user, rememberMe);
    this.refreshToken = jwtTokenProvider.generateRefreshToken(user, rememberMe);

    // Set the access token as a secure HTTP-only cookie
    accessTokenCookie =
        ResponseCookie.from("access_token", accessToken)
            .httpOnly(true)
            .secure(true) // Set to true in production with HTTPS
            .path("/")
            .maxAge(
                rememberMe
                    ? jwtTokenProvider.getRememberMeAccessTokenValidity() / 1000
                    : jwtTokenProvider.getAccessTokenValidity() / 1000)
            .sameSite("None")
            .build();

    // Set the refresh token as a secure HTTP-only cookie
    refreshTokenCookie =
        ResponseCookie.from("refresh_token", refreshToken)
            .httpOnly(true)
            .secure(true) // Set to true in production with HTTPS
            .path("/")
            .maxAge(
                rememberMe
                    ? jwtTokenProvider.getRememberMeRefreshTokenValidity() / 1000
                    : jwtTokenProvider.getRefreshTokenValidity() / 1000)
            .sameSite("None")
            .build();

    response.addHeader("Set-Cookie", accessTokenCookie.toString());
    response.addHeader("Set-Cookie", refreshTokenCookie.toString());
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
