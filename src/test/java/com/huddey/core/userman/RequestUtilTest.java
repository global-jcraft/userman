package com.huddey.core.userman;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.huddey.core.userman.utils.RequestUtils;

import jakarta.servlet.http.HttpServletRequest;

class RequestUtilTest {

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
