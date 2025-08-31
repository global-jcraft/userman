package com.huddey.core.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.huddey.core.userman.auth.JwtTokenProvider;
import com.huddey.core.userman.data.SecurityUser;
import com.huddey.core.userman.data.entity.Role;
import com.huddey.core.userman.data.entity.User;
import com.huddey.core.userman.token.WebTokenGenerationStrategy;

import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class WebTokenGenerationStrategyTest {

  @Mock private JwtTokenProvider jwtTokenProvider;

  @Mock private HttpServletResponse response;

  @InjectMocks private WebTokenGenerationStrategy tokenStrategy;

  private SecurityUser testUser;
  private static final String TEST_ACCESS_TOKEN = "test.access.token";
  private static final String TEST_REFRESH_TOKEN = "test.refresh.token";
  private static final long ACCESS_TOKEN_VALIDITY = 3600000; // 1 hour in milliseconds
  private static final long REFRESH_TOKEN_VALIDITY = 86400000; // 24 hours in milliseconds
  private static final long REMEMBER_ME_ACCESS_TOKEN_VALIDITY = 604800000; // 7 days in milliseconds
  private static final long REMEMBER_ME_REFRESH_TOKEN_VALIDITY =
      2592000000L; // 30 days in milliseconds

  @BeforeEach
  void setUp() {
    Role role = new Role();
    role.setId(1L);
    role.setName("ROLE_USER");
    User user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");
    user.setRoles(new HashSet<>(List.of(role)));
    testUser = new SecurityUser(user);
  }

  @Test
  void generateAndSetToken_WithoutRememberMe_ShouldSetCorrectCookies() {
    // Arrange
    when(jwtTokenProvider.generateAccessToken(testUser, false)).thenReturn(TEST_ACCESS_TOKEN);
    when(jwtTokenProvider.generateRefreshToken(testUser, false)).thenReturn(TEST_REFRESH_TOKEN);
    when(jwtTokenProvider.getAccessTokenValidity()).thenReturn(ACCESS_TOKEN_VALIDITY);
    when(jwtTokenProvider.getRefreshTokenValidity()).thenReturn(REFRESH_TOKEN_VALIDITY);

    // Act
    tokenStrategy.generateAndSetToken(response, testUser, false);

    // Assert
    ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
    verify(response, times(2)).addHeader(eq("Set-Cookie"), headerCaptor.capture());

    List<String> capturedHeaders = headerCaptor.getAllValues();
    String accessCookieHeader = capturedHeaders.get(0);
    String refreshCookieHeader = capturedHeaders.get(1);

    // Verify access token cookie header
    assertTrue(accessCookieHeader.contains("access_token=" + TEST_ACCESS_TOKEN));
    assertTrue(accessCookieHeader.contains("Path=/"));
    assertTrue(accessCookieHeader.contains("HttpOnly"));
    assertTrue(accessCookieHeader.contains("Max-Age=" + (ACCESS_TOKEN_VALIDITY / 1000)));

    // Verify refresh token cookie header
    assertTrue(refreshCookieHeader.contains("refresh_token=" + TEST_REFRESH_TOKEN));
    assertTrue(refreshCookieHeader.contains("Path=/"));
    assertTrue(refreshCookieHeader.contains("HttpOnly"));
    assertTrue(refreshCookieHeader.contains("Max-Age=" + (REFRESH_TOKEN_VALIDITY / 1000)));

    // Verify tokens are stored in the strategy
    assertEquals(TEST_ACCESS_TOKEN, tokenStrategy.getAccessToken());
    assertEquals(TEST_REFRESH_TOKEN, tokenStrategy.getRefreshToken());
  }

  @Test
  void generateAndSetToken_WithRememberMe_ShouldSetLongerExpirationCookies() {
    // Arrange
    when(jwtTokenProvider.generateAccessToken(testUser, true)).thenReturn(TEST_ACCESS_TOKEN);
    when(jwtTokenProvider.generateRefreshToken(testUser, true)).thenReturn(TEST_REFRESH_TOKEN);
    when(jwtTokenProvider.getRememberMeAccessTokenValidity())
        .thenReturn(REMEMBER_ME_ACCESS_TOKEN_VALIDITY);
    when(jwtTokenProvider.getRememberMeRefreshTokenValidity())
        .thenReturn(REMEMBER_ME_REFRESH_TOKEN_VALIDITY);

    // Act
    tokenStrategy.generateAndSetToken(response, testUser, true);

    // Assert
    ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
    verify(response, times(2)).addHeader(eq("Set-Cookie"), headerCaptor.capture());

    List<String> capturedHeaders = headerCaptor.getAllValues();
    String accessCookieHeader = capturedHeaders.get(0);
    String refreshCookieHeader = capturedHeaders.get(1);

    // Verify access token cookie with remember me duration
    assertTrue(
        accessCookieHeader.contains("Max-Age=" + (REMEMBER_ME_ACCESS_TOKEN_VALIDITY / 1000)));
    // Verify refresh token cookie with remember me duration
    assertTrue(
        refreshCookieHeader.contains("Max-Age=" + (REMEMBER_ME_REFRESH_TOKEN_VALIDITY / 1000)));
  }

  @Test
  void generateAndSetToken_WhenTokenGenerationFails_ShouldPropagateException() {
    // Arrange
    when(jwtTokenProvider.generateAccessToken(testUser, false))
        .thenThrow(new RuntimeException("Token generation failed"));

    // Act & Assert
    assertThrows(
        RuntimeException.class, () -> tokenStrategy.generateAndSetToken(response, testUser, false));
  }

  @Test
  void getAccessToken_BeforeGeneration_ShouldReturnNull() {
    // Act & Assert
    assertNull(tokenStrategy.getAccessToken());
  }

  @Test
  void getRefreshToken_BeforeGeneration_ShouldReturnNull() {
    // Act & Assert
    assertNull(tokenStrategy.getRefreshToken());
  }
}
