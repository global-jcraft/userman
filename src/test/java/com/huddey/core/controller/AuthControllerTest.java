package com.huddey.core.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.huddey.core.userman.auth.JwtTokenProvider;
import com.huddey.core.userman.controller.AuthController;
import com.huddey.core.userman.data.ApiResponse;
import com.huddey.core.userman.data.dto.*;
import com.huddey.core.userman.data.dto.response.LoginResponse;
import com.huddey.core.userman.data.dto.response.UserRegistrationResponse;
import com.huddey.core.userman.data.dto.token.TokenData;
import com.huddey.core.userman.exception.RoleNotFoundException;
import com.huddey.core.userman.exception.UserAlreadyExistsException;
import com.huddey.core.userman.service.AuthService;
import com.huddey.core.userman.service.CustomUserDetailsService;
import com.huddey.core.userman.utils.LocaleUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock private AuthService authService;

  @Mock private JwtTokenProvider jwtTokenProvider;

  @Mock private CustomUserDetailsService customUserDetailsService;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private MessageSource messageSource;

  private AuthController authController;

  @BeforeEach
  void setUp() {
    authController = new AuthController(authService, jwtTokenProvider, customUserDetailsService);
    LocaleUtils.instance = new LocaleUtils(messageSource);
  }

  @Test
  void registerBasicFlow_ShouldReturnSuccessResponse() throws Exception {
    // Arrange
    UserRegistrationBasicFlowRequest registrationRequest = new UserRegistrationBasicFlowRequest();
    UserRegistrationResponse expectedResponse = new UserRegistrationResponse();

    when(authService.registerBasicFlow(
            any(UserRegistrationBasicFlowRequest.class),
            any(HttpServletRequest.class),
            any(HttpServletResponse.class)))
        .thenReturn(expectedResponse);

    // Act
    ResponseEntity<ApiResponse> responseEntity =
        authController.registerBasicFlow(registrationRequest, request, response);

    // Assert
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseEntity.getBody()).isNotNull();
    assertNotNull(responseEntity.getBody());
  }

  @Test
  void registerBasicFlowComplete_ShouldReturnSuccessResponse() throws Exception {
    // Arrange
    UserRegistrationRequest registrationRequest = new UserRegistrationRequest();
    UserRegistrationResponse expectedResponse = new UserRegistrationResponse();

    when(authService.completeRegistration(
            any(UserRegistrationRequest.class),
            any(HttpServletRequest.class),
            any(HttpServletResponse.class)))
        .thenReturn(expectedResponse);

    // Act
    ResponseEntity<ApiResponse> responseEntity =
        authController.registerBasicFlowComplete(registrationRequest, request, response);

    // Assert
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseEntity.getBody()).isNotNull();
    assertNotNull(responseEntity.getBody());
  }

  @Test
  void registerBasicFlow_WhenUserAlreadyExists_ShouldThrowException() throws Exception {
    // Arrange
    UserRegistrationBasicFlowRequest registrationRequest = new UserRegistrationBasicFlowRequest();

    when(authService.registerBasicFlow(
            any(UserRegistrationBasicFlowRequest.class),
            any(HttpServletRequest.class),
            any(HttpServletResponse.class)))
        .thenThrow(new UserAlreadyExistsException("User already exists"));

    // Act & Assert
    assertThrows(
        UserAlreadyExistsException.class,
        () -> {
          authController.registerBasicFlow(registrationRequest, request, response);
        });
  }

  @Test
  void registerBasicFlowComplete_WhenRoleNotFound_ShouldThrowException() throws Exception {
    // Arrange
    UserRegistrationRequest registrationRequest = new UserRegistrationRequest();

    when(authService.completeRegistration(
            any(UserRegistrationRequest.class),
            any(HttpServletRequest.class),
            any(HttpServletResponse.class)))
        .thenThrow(new RoleNotFoundException("Role not found"));

    // Act & Assert
    assertThrows(
        RoleNotFoundException.class,
        () -> {
          authController.registerBasicFlowComplete(registrationRequest, request, response);
        });
  }

  @Test
  void login_ShouldReturnSuccessResponse() {
    // Arrange
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setEmail("test@example.com");
    loginRequest.setPassword("password123");

    LoginResponse expectedResponse = new LoginResponse();
    TokenData tokenData = new TokenData();
    tokenData.setAccessToken("access-token");
    tokenData.setRefreshToken("refresh-token");
    expectedResponse.setTokenData(tokenData);
    expectedResponse.getTokenData().setAccessToken("access-token");
    expectedResponse.getTokenData().setRefreshToken("refresh-token");
    expectedResponse.getTokenData().setTokenType("Bearer");

    when(authService.login(
            any(LoginRequest.class), any(HttpServletRequest.class), any(HttpServletResponse.class)))
        .thenReturn(expectedResponse);

    // Act
    ResponseEntity<ApiResponse> responseEntity =
        authController.login(loginRequest, request, response);

    // Assert
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseEntity.getBody()).isNotNull();
    var login = (LoginResponse) responseEntity.getBody().getData();
    assertThat(login.getTokenData().getAccessToken()).isEqualTo("access-token");
    assertThat(login.getTokenData().getRefreshToken()).isEqualTo("refresh-token");
    assertThat(login.getTokenData().getTokenType()).isEqualTo("Bearer");
  }

  @Test
  void resetPasswordRequest_ShouldReturnSuccessResponse() {
    // Arrange
    ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
    resetPasswordRequest.setEmail("test@example.com");

    ResetPasswordResponse resetPasswordResponse = new ResetPasswordResponse();

    when(authService.resetPasswordRequest(
            any(ResetPasswordRequest.class),
            any(HttpServletRequest.class),
            any(HttpServletResponse.class)))
        .thenReturn(resetPasswordResponse);

    when(messageSource.getMessage(eq("password.reset.request"), any(), any(Locale.class)))
        .thenReturn("Password reset request sent successfully");

    // Act
    ResponseEntity<ApiResponse> responseEntity =
        authController.resetPasswordRequest(resetPasswordRequest, request, response);

    // Assert
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseEntity.getBody()).isNotNull();
    assertThat(responseEntity.getBody().isSuccess()).isTrue();
    assertThat(responseEntity.getBody().getMessage())
        .isEqualTo("Password reset request sent successfully");
    assertThat(responseEntity.getBody().getData()).isEqualTo(resetPasswordResponse);
    assertThat(responseEntity.getBody().getTimestamp()).isNotNull();

    verify(authService).resetPasswordRequest(resetPasswordRequest, request, response);
  }

  @Test
  void resetPasswordRequest_WhenServiceThrowsException_ShouldPropagateException() {
    // Arrange
    ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
    resetPasswordRequest.setEmail("test@example.com");

    when(authService.resetPasswordRequest(
            any(ResetPasswordRequest.class),
            any(HttpServletRequest.class),
            any(HttpServletResponse.class)))
        .thenThrow(new RuntimeException("Failed to process reset request"));

    // Act & Assert
    assertThrows(
        RuntimeException.class,
        () -> authController.resetPasswordRequest(resetPasswordRequest, request, response));
  }

  @Test
  void resetPasswordComplete_ShouldReturnSuccessResponse() {
    // Arrange
    ResetPasswordCompleteRequest resetPasswordCompleteRequest = new ResetPasswordCompleteRequest();
    resetPasswordCompleteRequest.setToken("reset-token");
    resetPasswordCompleteRequest.setNewPassword("newPassword123");

    when(messageSource.getMessage(eq("password.reset.request.success"), any(), any(Locale.class)))
        .thenReturn("Password has been reset successfully");

    // Act
    ResponseEntity<ApiResponse> responseEntity =
        authController.resetPasswordComplete(resetPasswordCompleteRequest, request, response);

    // Assert
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseEntity.getBody()).isNotNull();
    assertThat(responseEntity.getBody().isSuccess()).isTrue();
    assertThat(responseEntity.getBody().getMessage())
        .isEqualTo("Password has been reset successfully");
    assertThat(responseEntity.getBody().getTimestamp()).isNotNull();

    verify(authService).resetPasswordComplete(resetPasswordCompleteRequest, request, response);
  }

  @Test
  void resetPasswordComplete_WhenServiceThrowsException_ShouldPropagateException() {
    // Arrange
    ResetPasswordCompleteRequest resetPasswordCompleteRequest = new ResetPasswordCompleteRequest();
    resetPasswordCompleteRequest.setToken("reset-token");
    resetPasswordCompleteRequest.setNewPassword("newPassword123");

    doThrow(new RuntimeException("Failed to reset password"))
        .when(authService)
        .resetPasswordComplete(
            any(ResetPasswordCompleteRequest.class),
            any(HttpServletRequest.class),
            any(HttpServletResponse.class));

    // Act & Assert
    assertThrows(
        RuntimeException.class,
        () ->
            authController.resetPasswordComplete(resetPasswordCompleteRequest, request, response));
  }
}
