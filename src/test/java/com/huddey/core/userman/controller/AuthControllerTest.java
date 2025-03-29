package com.huddey.core.userman.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.huddey.core.userman.auth.JwtTokenProvider;
import com.huddey.core.userman.data.dto.LoginRequest;
import com.huddey.core.userman.data.dto.UserRegistrationBasicFlowRequest;
import com.huddey.core.userman.data.dto.UserRegistrationRequest;
import com.huddey.core.userman.data.dto.response.LoginResponse;
import com.huddey.core.userman.data.dto.response.UserRegistrationResponse;
import com.huddey.core.userman.exception.RoleNotFoundException;
import com.huddey.core.userman.exception.UserAlreadyExistsException;
import com.huddey.core.userman.service.AuthService;
import com.huddey.core.userman.service.CustomUserDetailsService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock private AuthService authService;

  @Mock private JwtTokenProvider jwtTokenProvider;

  @Mock private CustomUserDetailsService customUserDetailsService;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  private AuthController authController;

  @BeforeEach
  void setUp() {
    authController = new AuthController(authService, jwtTokenProvider, customUserDetailsService);
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
    ResponseEntity<UserRegistrationResponse> responseEntity =
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
    ResponseEntity<UserRegistrationResponse> responseEntity =
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
    expectedResponse.setAccessToken("access-token");
    expectedResponse.setRefreshToken("refresh-token");
    expectedResponse.setTokenType("Bearer");

    when(authService.login(
            any(LoginRequest.class), any(HttpServletRequest.class), any(HttpServletResponse.class)))
        .thenReturn(expectedResponse);

    // Act
    ResponseEntity<LoginResponse> responseEntity =
        authController.login(loginRequest, request, response);

    // Assert
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseEntity.getBody()).isNotNull();
    assertThat(responseEntity.getBody().getAccessToken()).isEqualTo("access-token");
    assertThat(responseEntity.getBody().getRefreshToken()).isEqualTo("refresh-token");
    assertThat(responseEntity.getBody().getTokenType()).isEqualTo("Bearer");
  }
}
