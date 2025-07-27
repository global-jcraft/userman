package com.huddey.core.userman.auth;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Component
@Validated
@Getter
@Setter
public class JwtTokenProvider {
  @Value("${app.security.jwt.secret}")
  @NotBlank
  private String secret;

  @Value("${app.security.jwt.accessTokenValidity}")
  private long accessTokenValidity;

  @Value("${app.security.jwt.refreshTokenValidity}")
  private long refreshTokenValidity;

  @Value("${app.security.jwt.rememberMeAccessTokenValidity}")
  private long rememberMeAccessTokenValidity;

  @Value("${app.security.jwt.rememberMeRefreshTokenValidity}")
  private long rememberMeRefreshTokenValidity;

  private Key key;

  @PostConstruct
  public void init() {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(UserDetails userDetails, boolean rememberMe) {
    long validity = rememberMe ? rememberMeAccessTokenValidity : accessTokenValidity;
    return generateToken(userDetails, validity);
  }

  public String generateRefreshToken(UserDetails userDetails, boolean rememberMe) {
    long validity = rememberMe ? rememberMeRefreshTokenValidity : refreshTokenValidity;
    return generateToken(userDetails, validity);
  }

  private String generateToken(UserDetails userDetails, long validity) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + validity);

    return Jwts.builder()
        .setSubject(userDetails.getUsername())
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(key)
        .compact();
  }

  public String getUsername(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }
}
