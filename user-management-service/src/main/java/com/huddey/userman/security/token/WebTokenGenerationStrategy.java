package com.huddey.userman.security.token;

import org.springframework.context.annotation.Configuration;

import com.huddey.userman.auth.JwtTokenProvider;
import com.huddey.userman.data.SecurityUser;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;

@Configuration
public class WebTokenGenerationStrategy implements TokenGenerationStrategy {
  private final JwtTokenProvider jwtTokenProvider;
  String accessToken;
  String refreshToken;
  @Getter Cookie accessTokenCookie;
  @Getter Cookie refreshTokenCookie;

  public WebTokenGenerationStrategy(JwtTokenProvider jwtTokenProvider) {
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Override
  public void generateAndSetToken(
      HttpServletResponse response, SecurityUser user, boolean rememberMe) {
    this.accessToken = jwtTokenProvider.generateAccessToken(user, rememberMe);
    this.refreshToken = jwtTokenProvider.generateRefreshToken(user, rememberMe);

    // Set the access token as a secure HTTP-only cookie
    accessTokenCookie = new Cookie("access_token", accessToken);
    accessTokenCookie.setHttpOnly(true);
    accessTokenCookie.setSecure(true);
    accessTokenCookie.setPath("/");
    accessTokenCookie.setMaxAge(
        (int)
            (rememberMe
                ? jwtTokenProvider.getRememberMeAccessTokenValidity() / 1000
                : jwtTokenProvider.getAccessTokenValidity() / 1000));

    // Set the refresh token as a secure HTTP-only cookie
    refreshTokenCookie = new Cookie("refresh_token", refreshToken);
    refreshTokenCookie.setHttpOnly(true);
    refreshTokenCookie.setSecure(true);
    refreshTokenCookie.setPath("/");
    refreshTokenCookie.setMaxAge(
        (int)
            (rememberMe
                ? jwtTokenProvider.getRememberMeRefreshTokenValidity() / 1000
                : jwtTokenProvider.getRefreshTokenValidity() / 1000));

    response.addCookie(accessTokenCookie);
    response.addCookie(refreshTokenCookie);
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
