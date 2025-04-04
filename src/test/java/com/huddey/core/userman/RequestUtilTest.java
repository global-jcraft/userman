package com.huddey.core.userman;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.huddey.core.userman.auth.JwtTokenProvider;
import com.huddey.core.userman.data.SecurityUser;
import com.huddey.core.userman.data.dto.response.LoginResponse;
import com.huddey.core.userman.data.entity.User;
import com.huddey.core.userman.data.entity.UserStatus;
import com.huddey.core.userman.utils.RequestUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class RequestUtilTest {

  @Mock private JwtTokenProvider jwtTokenProvider;

  @Mock private SecurityUser securityUser;

  @Mock private HttpServletResponse servletResponse;

  private User user;

  private static final String TEST_ACCESS_TOKEN = "test.access.token";
  private static final String TEST_REFRESH_TOKEN = "test.refresh.token";

  @BeforeEach
  void setUp() {
    user =
        User.builder()
            .id(1L)
            .email("test@example.com")
            .firstName("John")
            .lastName("Doe")
            .companyName("Test Company")
            .phoneNumber("+1234567890")
            .profilePictureUrl("https://example.com/profile.jpg")
            .roles(new HashSet<>())
            .status(UserStatus.ACTIVE)
            .lastLoginAt(null)
            .build();
  }

  @Test
  void test_getClientIp_1() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("192.168.1.1");
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    String result = RequestUtils.getClientIp();

    assertEquals("192.168.1.1", result);
  }

  @Test
  void test_getClientIp_3() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Forwarded-For", "unknown");
    request.addHeader("Proxy-Client-IP", "");
    request.addHeader("WL-Proxy-Client-IP", "192.168.1.1");

    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    String clientIp = RequestUtils.getClientIp();

    assertEquals("192.168.1.1", clientIp);

    RequestContextHolder.resetRequestAttributes();
  }

  @Test
  void test_getClientIp_allHeadersNullOrUnknown() {
    // Set up
    ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(attributes.getRequest()).thenReturn(request);
    when(request.getHeader("X-Forwarded-For")).thenReturn(null);
    when(request.getHeader("Proxy-Client-IP")).thenReturn("unknown");
    when(request.getHeader("WL-Proxy-Client-IP")).thenReturn("");
    when(request.getRemoteAddr()).thenReturn("192.168.1.1");
    RequestContextHolder.setRequestAttributes(attributes);

    // Execute
    String result = RequestUtils.getClientIp();

    // Verify
    assertEquals("192.168.1.1", result);
  }

  @Test
  void test_getLoginResponse_forMobileClient() {
    // Arrange
    String clientType = "MOBILE";
    boolean rememberMe = false;

    when(jwtTokenProvider.getAccessTokenValidity()).thenReturn(3600L);
    when(jwtTokenProvider.generateAccessToken(securityUser, rememberMe))
        .thenReturn(TEST_ACCESS_TOKEN);
    when(jwtTokenProvider.generateRefreshToken(securityUser, rememberMe))
        .thenReturn(TEST_REFRESH_TOKEN);

    // Act
    LoginResponse response =
        RequestUtils.getLoginResponse(
            servletResponse, clientType, securityUser, user, jwtTokenProvider, rememberMe);

    // Assert
    assertNotNull(response);
    assertNotNull(response.getAccessToken());
    assertNotNull(response.getRefreshToken());
    assertEquals("Bearer", response.getTokenType());
    assertEquals(3600L, response.getExpiresIn());
    assertNotNull(response.getUser());
  }

  @Test
  void test_getLoginResponse_forWebClientType() {
    // Arrange
    String clientType = "WEB";
    boolean rememberMe = false;
    long expiresIn = 3600L;

    when(jwtTokenProvider.getAccessTokenValidity()).thenReturn(expiresIn);

    // Act
    LoginResponse response =
        RequestUtils.getLoginResponse(
            servletResponse, clientType, securityUser, user, jwtTokenProvider, rememberMe);

    // Assert
    assertNotNull(response);
    assertNotNull(response.getUser());
    assertEquals(expiresIn, response.getExpiresIn());
    assertNull(response.getAccessToken());
    assertNull(response.getRefreshToken());
  }

  @Test
  void test_getLoginResponse_unknownClientType() {
    String unknownClientType = "unknown";
    boolean rememberMe = false;

    when(jwtTokenProvider.getAccessTokenValidity()).thenReturn(3600L);

    LoginResponse response =
        RequestUtils.getLoginResponse(
            servletResponse, unknownClientType, securityUser, user, jwtTokenProvider, rememberMe);

    assertEquals("Bearer", response.getTokenType());
    assertEquals(3600L, response.getExpiresIn());
  }

  @Test
  @DisplayName("Determine client type returns mobile when user agent is Android")
  void determine_clientType_returns_mobile_when_user_ggent_is_android() {
    // Arrange
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("User-Agent")).thenReturn("Android");

    // Act
    String clientType = RequestUtils.determineClientType(request);

    // Assert
    assertEquals("mobile", clientType);
  }

  @Test
  @DisplayName("Determine client type returns mobile when user agent is iOS")
  void determine_clientType_returns_mobile_when_user_agent_is_iOS() {
    // Arrange
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("User-Agent")).thenReturn("iOS");

    // Act
    String clientType = RequestUtils.determineClientType(request);

    // Assert
    assertEquals("mobile", clientType);
  }

  @Test
  @DisplayName("Determine client type returns web when user agent is something else")
  void determine_clientType_returns_web_when_user_agent_is_something_else() {
    // Arrange
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");

    // Act
    String clientType = RequestUtils.determineClientType(request);

    // Assert
    assertEquals("web", clientType);
  }

  @Test
  @DisplayName("Determine client type returns web when user agent is null")
  void determine_clientType_returns_web_when_user_agent_is_null() {
    // Arrange
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader("User-Agent")).thenReturn(null);

    // Act
    String clientType = RequestUtils.determineClientType(request);

    // Assert
    assertEquals("web", clientType);
  }
}
