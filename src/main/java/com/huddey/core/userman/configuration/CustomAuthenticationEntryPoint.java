package com.huddey.core.userman.configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  public static final String STATUS = "status";
  public static final String ERROR = "error";
  public static final String MESSAGE = "message";
  public static final String PATH = "path";
  public static final String TIMESTAMP = "timestamp";

  private final ObjectMapper objectMapper;

  @Autowired
  public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException, ServletException {

    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

    Map<String, Object> body = new HashMap<>();
    body.put(STATUS, HttpServletResponse.SC_UNAUTHORIZED);
    body.put(ERROR, "Unauthorized");
    body.put(MESSAGE, authException.getMessage());
    body.put(PATH, request.getServletPath());
    body.put(TIMESTAMP, LocalDateTime.now().toString());

    // Handle OAuth2 specific errors
    if (authException instanceof OAuth2AuthenticationException oauth2AuthenticationException) {
      OAuth2Error oauth2Error = (oauth2AuthenticationException).getError();
      body.put(MESSAGE, oauth2Error.getDescription());
      body.put("error_code", oauth2Error.getErrorCode());
      body.put("error_uri", oauth2Error.getUri());
    } else {
      body.put(MESSAGE, authException.getMessage());
    }

    // Add information about the authentication method that failed
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      body.put("auth_type", "JWT");
    } else if (request.getParameter("code") != null) {
      body.put("auth_type", "OAuth2");
    }

    objectMapper.writeValue(response.getOutputStream(), body);
  }
}
