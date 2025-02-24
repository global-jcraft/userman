package com.huddey.core.userman.security.token;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;

import com.huddey.core.userman.auth.JwtTokenProvider;
import com.huddey.core.userman.data.SecurityUser;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class WebTokenGenerationStrategy implements TokenGenerationStrategy {
  private final JwtTokenProvider jwtTokenProvider;
  String accessToken;
  String refreshToken;
  @Getter
  Cookie accessTokenCookie;
  @Getter
  Cookie refreshTokenCookie;

  public WebTokenGenerationStrategy(JwtTokenProvider jwtTokenProvider) {
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Override
  public void generateAndSetToken(HttpServletResponse response, SecurityUser user) {
    this.accessToken = jwtTokenProvider.generateAccessToken(user);
    this.refreshToken = jwtTokenProvider.generateRefreshToken(user);

    // Set the access token as a secure HTTP-only cookie
    accessTokenCookie = new Cookie("access_token", accessToken);
    accessTokenCookie.setHttpOnly(true);
    accessTokenCookie.setSecure(true);
    accessTokenCookie.setPath("/");
    accessTokenCookie.setMaxAge(3600); // Set the cookie expiration time

    // Set the refresh token as a secure HTTP-only cookie
    refreshTokenCookie = new Cookie("refresh_token", refreshToken);
    refreshTokenCookie.setHttpOnly(true);
    refreshTokenCookie.setSecure(true);
    refreshTokenCookie.setPath("/");
    refreshTokenCookie.setMaxAge(86400); // Set the cookie expiration time

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
