package com.huddey.core.userman.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import com.huddey.core.userman.data.dto.response.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler exceptionHandler;
  private WebRequest webRequest;
  private HttpServletRequest httpRequest;

  @BeforeEach
  void setUp() {
    exceptionHandler = new GlobalExceptionHandler();
    httpRequest = mock(HttpServletRequest.class);
    webRequest = mock(ServletWebRequest.class);
    when(((ServletWebRequest) webRequest).getRequest()).thenReturn(httpRequest);
    when(httpRequest.getRequestURI()).thenReturn("/api/test");
  }

  @Test
  @DisplayName("Should handle AuthenticationException correctly")
  void handleAuthenticationException() {
    // Given
    String errorMessage = "Invalid credentials";
    AuthenticationException ex = new AuthenticationException(errorMessage);

    // When
    ResponseEntity<ErrorResponse> response =
        exceptionHandler.handleAuthenticationException(ex, webRequest);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
    assertThat(response.getBody().getError()).isEqualTo("Authentication Error");
    assertThat(response.getBody().getPath()).isEqualTo("/api/test");
    assertThat(response.getBody().getTimestamp()).isNotNull();
  }

  @Test
  @DisplayName("Should handle UserAlreadyExistsException correctly")
  void handleUserAlreadyExistsException() {
    // Given
    String errorMessage = "User already exists with this email";
    UserAlreadyExistsException ex = new UserAlreadyExistsException(errorMessage);

    // When
    ResponseEntity<ErrorResponse> response =
        exceptionHandler.handleUserAlreadyExists(ex, webRequest);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
    assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
    assertThat(response.getBody().getError()).isEqualTo(errorMessage);
    assertThat(response.getBody().getPath()).isEqualTo("/api/test");
  }

  @Test
  @DisplayName("Should handle UserNotFoundException correctly")
  void handleUserNotFoundException() {
    // Given
    String errorMessage = "User not found with id: 1";
    UserNotFoundException ex = new UserNotFoundException(errorMessage);

    // When
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleUserNotFound(ex, webRequest);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
    assertThat(response.getBody().getError()).isEqualTo("Not Found");
    assertThat(response.getBody().getPath()).isEqualTo("/api/test");
  }

  @Test
  @DisplayName("Should handle ValidationException correctly")
  void handleValidationException() {
    // Given
    String errorMessage = "Invalid input data";
    ValidationException ex = new ValidationException(errorMessage);

    // When
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidation(ex, webRequest);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
    assertThat(response.getBody().getError()).isEqualTo("Validation Error");
    assertThat(response.getBody().getPath()).isEqualTo("/api/test");
  }

  @Test
  @DisplayName("Should handle MethodArgumentNotValidException correctly")
  void handleMethodArgumentNotValidException() {
    // Given
    MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
    BindingResult bindingResult = mock(BindingResult.class);
    when(ex.getBindingResult()).thenReturn(bindingResult);

    FieldError fieldError = new FieldError("user", "email", "Email is required");
    when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError));

    // When
    ResponseEntity<Object> response =
        exceptionHandler.handleMethodArgumentNotValid(ex, null, HttpStatus.BAD_REQUEST, webRequest);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    ErrorResponse errorResponse = (ErrorResponse) response.getBody();
    assertThat(errorResponse).isNotNull();
    assertThat(errorResponse.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(errorResponse.getMessage()).isEqualTo("Invalid request parameters");
    assertThat(errorResponse.getError()).isEqualTo("Validation Error");
    assertThat(errorResponse.getPath()).isEqualTo("/api/test");
    assertThat(errorResponse.getErrors()).containsEntry("email", "Email is required");
  }

  @Test
  @DisplayName("Should handle general exceptions correctly")
  void handleAllUncaughtException() {
    // Given
    Exception ex = new RuntimeException("Unexpected error");

    // When
    ResponseEntity<ErrorResponse> response =
        exceptionHandler.handleAllUncaughtException(ex, webRequest);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
    assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
    assertThat(response.getBody().getPath()).isEqualTo("/api/test");
  }
}
