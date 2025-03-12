package com.huddey.core.userman.auth.oauth2;

import java.io.IOException;
import java.time.OffsetDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws IOException {

    log.error("OAuth2 authentication failed", exception);

    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    ApiError apiError =
        ApiError.builder()
            .timestamp(OffsetDateTime.now())
            .status(HttpStatus.UNAUTHORIZED.value())
            .error("Authentication Failed")
            .path(request.getRequestURI())
            .build();

    if (exception instanceof OAuth2AuthenticationException oauth2Ex) {
      OAuth2Error error = oauth2Ex.getError();
      apiError.setMessage(error.getDescription());
      apiError.setErrorCode(error.getErrorCode());
    } else {
      apiError.setMessage(exception.getMessage());
    }

    objectMapper.writeValue(response.getOutputStream(), apiError);
  }
}
