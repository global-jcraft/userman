package com.huddey.core.userman.auth;

import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationEntryPointTest {

  @Mock private ObjectMapper objectMapper;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private AuthenticationException authException;

  @InjectMocks private CustomAuthenticationEntryPoint entryPoint;

  private ByteArrayOutputStream outputStream;
  private ServletOutputStream servletOutputStream;

  @BeforeEach
  void setUp() throws IOException {
    outputStream = new ByteArrayOutputStream();
    servletOutputStream =
        new ServletOutputStream() {
          @Override
          public void write(int b) throws IOException {
            outputStream.write(b);
          }

          @Override
          public boolean isReady() {
            return true;
          }

          @Override
          public void setWriteListener(jakarta.servlet.WriteListener writeListener) {
            // Not needed for testing
          }
        };
    when(response.getOutputStream()).thenReturn(servletOutputStream);
  }

  @Test
  @DisplayName("Test basic unauthorized access response")
  void commence_BasicUnauthorizedAccess_ShouldReturnProperResponse() throws Exception {
    // Given
    String path = "/api/test";
    String errorMessage = "Full authentication is required";
    when(request.getServletPath()).thenReturn(path);
    when(authException.getMessage()).thenReturn(errorMessage);

    // When
    entryPoint.commence(request, response, authException);

    // Then
    verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(objectMapper)
        .writeValue(
            any(ServletOutputStream.class),
            argThat(
                body -> {
                  Map<String, Object> map = (Map<String, Object>) body;
                  return map.get(CustomAuthenticationEntryPoint.STATUS)
                          .equals(HttpServletResponse.SC_UNAUTHORIZED)
                      && map.get(CustomAuthenticationEntryPoint.ERROR).equals("Unauthorized")
                      && map.get(CustomAuthenticationEntryPoint.MESSAGE).equals(errorMessage)
                      && map.get(CustomAuthenticationEntryPoint.PATH).equals(path)
                      && map.get(CustomAuthenticationEntryPoint.TIMESTAMP) != null;
                }));
  }

  @Test
  @DisplayName("Test OAuth2 authentication failure response")
  void commence_OAuth2AuthenticationFailure_ShouldReturnOAuth2ErrorDetails() throws Exception {
    // Given
    OAuth2Error oauth2Error =
        new OAuth2Error(
            "invalid_token", "The token has expired", "https://oauth2.example.com/errors");
    OAuth2AuthenticationException oauth2Exception = new OAuth2AuthenticationException(oauth2Error);
    when(request.getServletPath()).thenReturn("/api/oauth2");
    when(request.getParameter("code")).thenReturn("some-code");

    // When
    entryPoint.commence(request, response, oauth2Exception);

    // Then
    verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(objectMapper)
        .writeValue(
            any(ServletOutputStream.class),
            argThat(
                body -> {
                  Map<String, Object> map = (Map<String, Object>) body;
                  return map.get("error_code").equals("invalid_token")
                      && map.get(CustomAuthenticationEntryPoint.MESSAGE)
                          .equals("The token has expired")
                      && map.get("error_uri").equals("https://oauth2.example.com/errors")
                      && map.get("auth_type").equals("OAuth2");
                }));
  }

  @Test
  @DisplayName("Test JWT authentication failure response")
  void commence_JWTAuthenticationFailure_ShouldIncludeJWTAuthType() throws Exception {
    // Given
    when(request.getServletPath()).thenReturn("/api/secured");
    when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
    when(authException.getMessage()).thenReturn("Invalid JWT token");

    // When
    entryPoint.commence(request, response, authException);

    // Then
    verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(objectMapper)
        .writeValue(
            any(ServletOutputStream.class),
            argThat(
                body -> {
                  Map<String, Object> map = (Map<String, Object>) body;
                  return map.get(CustomAuthenticationEntryPoint.MESSAGE).equals("Invalid JWT token")
                      && map.get("auth_type").equals("JWT");
                }));
  }
}
