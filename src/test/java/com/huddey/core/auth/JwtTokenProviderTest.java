package com.huddey.core.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.Key;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import com.huddey.core.userman.auth.JwtTokenProvider;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JwtTokenProviderTest {

  @Test
  void testGenerateAccessTokenWithNullUserDetails() {
    JwtTokenProvider jwtTokenProvider = new JwtTokenProvider();
    assertThrows(
        NullPointerException.class,
        () -> {
          jwtTokenProvider.generateAccessToken(null, false);
        });
  }

  @Test
  void testGetUsernameWithInvalidToken() {
    JwtTokenProvider jwtTokenProvider = new JwtTokenProvider();
    String invalidToken = "invalid.token.string";

    // Use a long enough secret (>=48 characters for HS384)
    try {
      Field keyField = JwtTokenProvider.class.getDeclaredField("key");
      keyField.setAccessible(true);
      keyField.set(
          jwtTokenProvider,
          Keys.hmacShaKeyFor(
              "testSecretKeyForJwtInvalidTokenValidation123456".getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchFieldException | IllegalAccessException e) {
      fail("Failed to set up test: " + e.getMessage());
    }
    assertThrows(
        JwtException.class,
        () -> {
          jwtTokenProvider.getUsername(invalidToken);
        });
  }

  @Test
  void testInitializationOfKey() throws NoSuchFieldException, IllegalAccessException {
    JwtTokenProvider jwtTokenProvider = new JwtTokenProvider();
    String testSecret = "testSecretKeyForJwtTokenProviderValidation12345678";
    Field secretField = JwtTokenProvider.class.getDeclaredField("secret");
    secretField.setAccessible(true);
    secretField.set(jwtTokenProvider, testSecret);
    jwtTokenProvider.init();
    Field keyField = JwtTokenProvider.class.getDeclaredField("key");
    keyField.setAccessible(true);
    Key key = (Key) keyField.get(jwtTokenProvider);
    assertNotNull(key, "The key should be initialized");
    assertEquals(
        Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8)).getAlgorithm(),
        key.getAlgorithm(),
        "The key algorithm should match");
  }

  @Test
  void test_generateAccessToken_withoutRememberMe() {
    JwtTokenProvider jwtTokenProvider = new JwtTokenProvider();
    jwtTokenProvider.setSecret("testSecretKeyWithAtLeast256BitsForHmacSHA256Validation123456");
    jwtTokenProvider.setAccessTokenValidity(3600000); // 1 hour
    jwtTokenProvider.init();
    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn("testUser");
    String token = jwtTokenProvider.generateAccessToken(userDetails, false);
    assertNotNull(token);
    assertTrue(jwtTokenProvider.validateToken(token));
    assertEquals("testUser", jwtTokenProvider.getUsername(token));
  }

  @Test
  void test_generateRefreshToken_validityDuration() {
    JwtTokenProvider jwtTokenProvider = new JwtTokenProvider();
    // Set a proper secret before using the provider
    jwtTokenProvider.setSecret("testSecretKeyForJwtRefreshTokenValidation1234567890ABCDEFGH");
    jwtTokenProvider.setRefreshTokenValidity(3600000); // 1 hour
    jwtTokenProvider.setRememberMeRefreshTokenValidity(86400000); // 24 hours
    jwtTokenProvider.init();
    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn("testUser");
    String token1 = jwtTokenProvider.generateRefreshToken(userDetails, false);
    String token2 = jwtTokenProvider.generateRefreshToken(userDetails, true);
    long expTime1 =
        Jwts.parserBuilder()
            .setSigningKey(jwtTokenProvider.getKey())
            .build()
            .parseClaimsJws(token1)
            .getBody()
            .getExpiration()
            .getTime();
    long expTime2 =
        Jwts.parserBuilder()
            .setSigningKey(jwtTokenProvider.getKey())
            .build()
            .parseClaimsJws(token2)
            .getBody()
            .getExpiration()
            .getTime();
    assertNotEquals(expTime1, expTime2);
  }

  @Test
  void test_generateRefreshToken_whenRememberMeIsTrue() {
    JwtTokenProvider jwtTokenProvider = new JwtTokenProvider();
    jwtTokenProvider.setSecret("testSecretKeyForJwtRefreshTokenValidationSufficientLength12345");
    jwtTokenProvider.setRememberMeRefreshTokenValidity(3600000L);
    jwtTokenProvider.init();
    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn("testUser");
    String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails, true);
    assertNotNull(refreshToken);
    assertTrue(jwtTokenProvider.validateToken(refreshToken));
    assertEquals("testUser", jwtTokenProvider.getUsername(refreshToken));
  }

  @Test
  void test_getUsername_extractsSubjectFromValidToken() {
    JwtTokenProvider jwtTokenProvider = new JwtTokenProvider();
    jwtTokenProvider.setSecret("testSecretKeyForJwtUsernameExtractionValidation12345");
    jwtTokenProvider.init();
    String expectedUsername = "testUser";
    String token =
        Jwts.builder().setSubject(expectedUsername).signWith(jwtTokenProvider.getKey()).compact();
    String extractedUsername = jwtTokenProvider.getUsername(token);
    assertEquals(expectedUsername, extractedUsername);
  }

  @Test
  void test_validateToken_withValidToken_returnsTrue() {
    JwtTokenProvider jwtTokenProvider = new JwtTokenProvider();
    jwtTokenProvider.setSecret("testSecretKeyForJwtValidTokenValidation123456");
    jwtTokenProvider.init();
    String validToken =
        Jwts.builder().setSubject("testUser").signWith(jwtTokenProvider.getKey()).compact();
    boolean result = jwtTokenProvider.validateToken(validToken);
    assertTrue(result);
  }
}
