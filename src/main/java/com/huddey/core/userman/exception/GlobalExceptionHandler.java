package com.huddey.core.userman.exception;

import static com.huddey.core.userman.constants.Message.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.huddey.core.common.utils.LocaleUtils;
import com.huddey.core.userman.data.dto.response.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthenticationException(
      AuthenticationException ex, WebRequest request) {
    log.error("Authentication error: ", ex);
    ErrorResponse error =
        ErrorResponse.builder()
            .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .status(HttpStatus.UNAUTHORIZED.value())
            .error(LocaleUtils.getMessage(LocaleUtils.getMessage(GLOBAL_AUTH_ERROR)))
            .message(ex.getMessage())
            .path(((ServletWebRequest) request).getRequest().getRequestURI())
            .build();
    return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleUserAlreadyExists(
      UserAlreadyExistsException ex, WebRequest request) {
    log.error("User already exists: ", ex);
    ErrorResponse error =
        ErrorResponse.builder()
            .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .status(HttpStatus.CONFLICT.value())
            .error(ex.getMessage())
            .message(ex.getMessage())
            .path(((ServletWebRequest) request).getRequest().getRequestURI())
            .build();
    return new ResponseEntity<>(error, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUserNotFound(
      UserNotFoundException ex, WebRequest request) {
    log.error("User not found: ", ex);
    ErrorResponse error =
        ErrorResponse.builder()
            .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .status(HttpStatus.NOT_FOUND.value())
            .error("Not Found")
            .message(ex.getMessage())
            .path(((ServletWebRequest) request).getRequest().getRequestURI())
            .build();
    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ErrorResponse> handleValidation(
      ValidationException ex, WebRequest request) {
    log.error("Validation error: ", ex);
    ErrorResponse error =
        ErrorResponse.builder()
            .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .status(HttpStatus.BAD_REQUEST.value())
            .error(LocaleUtils.getMessage(GLOBAL_VALIDATION_ERROR))
            .message(ex.getMessage())
            .path(((ServletWebRequest) request).getRequest().getRequestURI())
            .build();
    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(AuthorizationDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAuthorizationDenied(
      AuthorizationDeniedException ex, WebRequest request) {
    log.error("Access denied: ", ex);
    ErrorResponse error =
        ErrorResponse.builder()
            .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .status(HttpStatus.FORBIDDEN.value())
            .error("Access Denied")
            .message("You are not authorized to access this resource.")
            .path(((ServletWebRequest) request).getRequest().getRequestURI())
            .build();
    return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .status(HttpStatus.BAD_REQUEST.value())
            .error(LocaleUtils.getMessage(GLOBAL_VALIDATION_ERROR))
            .message(LocaleUtils.getMessage(GLOBAL_REQUEST_ERROR))
            .path(((ServletWebRequest) request).getRequest().getRequestURI())
            .errors(errors)
            .build();

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAllUncaughtException(
      Exception ex, WebRequest request) {
    log.error("Unexpected error occurred: ", ex);
    ErrorResponse error =
        ErrorResponse.builder()
            .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error(LocaleUtils.getMessage(GLOBAL_INTERNAL_ERROR))
            .message(LocaleUtils.getMessage(GLOBAL_INTERNAL_UNEXPECTED_ERROR))
            .path(((ServletWebRequest) request).getRequest().getRequestURI())
            .build();
    return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
