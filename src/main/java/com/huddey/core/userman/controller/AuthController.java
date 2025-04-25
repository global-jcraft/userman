package com.huddey.core.userman.controller;

import static com.huddey.core.userman.constants.Message.*;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import javax.management.relation.RoleNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.huddey.core.userman.auth.JwtTokenProvider;
import com.huddey.core.userman.data.ApiResponse;
import com.huddey.core.userman.data.dto.*;
import com.huddey.core.userman.exception.UserAlreadyExistsException;
import com.huddey.core.userman.service.AuthService;
import com.huddey.core.userman.service.CustomUserDetailsService;
import com.huddey.core.userman.utils.ApiUtils;
import com.huddey.core.userman.utils.LocaleUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
@Slf4j
public class AuthController {

  final AuthService authService;

  @Autowired
  public AuthController(
      AuthService authService,
      JwtTokenProvider jwtTokenProvider,
      CustomUserDetailsService customUserDetailsService) {
    this.authService = authService;
  }

  @PostMapping("/basic-auth")
  public ResponseEntity<ApiResponse> registerBasicFlow(
      @Valid @RequestBody UserRegistrationBasicFlowRequest userRegistrationBasicFlowRequest,
      HttpServletRequest request,
      HttpServletResponse response)
      throws RoleNotFoundException, UserAlreadyExistsException {
    log.debug(
        "AuthController.registerBasicFlow() -> Registering a new user - Start time: {}",
        OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    ApiResponse apiResponse =
        ApiUtils.buildApiResponse(
            true,
            LocaleUtils.getMessage(SIMPLE_AUTH_REG_SUCCESS),
            authService.registerBasicFlow(userRegistrationBasicFlowRequest, request, response),
            null);
    return ResponseEntity.ok(apiResponse);
  }

  @PostMapping("/basic-auth-complete")
  public ResponseEntity<ApiResponse> registerBasicFlowComplete(
      @Valid @RequestBody UserRegistrationRequest userRegistrationRequest,
      HttpServletRequest request,
      HttpServletResponse response)
      throws RoleNotFoundException, UserAlreadyExistsException {
    log.debug(
        "AuthController.registerBasicFlowComplete() -> Update user missing info - Start time: {}",
        OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    ApiResponse apiResponse =
        ApiUtils.buildApiResponse(
            true,
            LocaleUtils.getMessage(SIMPLE_FULL_AUTH_REG_SUCCESS),
            authService.completeRegistration(userRegistrationRequest, request, response),
            null);
    return ResponseEntity.ok(apiResponse);
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse> login(
      @Valid @RequestBody LoginRequest request,
      HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) {
    log.debug(
        "AuthController.login() -> Signing in - Start time: {}",
        OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    ApiResponse apiResponse =
        ApiUtils.buildApiResponse(
            true,
            LocaleUtils.getMessage(SIMPLE_LOGIN_SUCCESS),
            authService.login(request, servletRequest, servletResponse),
            null);
    return ResponseEntity.ok(apiResponse);
  }

  /*@GetMapping("/verify-email/{token}")
  public ResponseEntity<ApiResponse> verifyEmail(@PathVariable String token) {
      authService.verifyEmail(token);
      return ResponseEntity.ok(new ApiResponse("Email verified successfully"));
  }*/

  @PostMapping("/refresh-token")
  public ResponseEntity<ApiResponse> refreshToken(
      @Valid @RequestBody RefreshTokenRequest request,
      HttpServletRequest servletRequest,
      HttpServletResponse response) {
    ApiResponse apiResponse =
        ApiUtils.buildApiResponse(
            true,
            LocaleUtils.getMessage(REFRESH_TOKEN_SUCCESS),
            authService.refreshToken(request, servletRequest, response),
            null);
    return ResponseEntity.ok(apiResponse);
  }

  @PostMapping("/reset-password-request")
  public ResponseEntity<ApiResponse> resetPasswordRequest(
      @Valid @RequestBody ResetPasswordRequest request,
      HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) {
    ApiResponse response =
        ApiUtils.buildApiResponse(
            true,
            LocaleUtils.getMessage(PASSWORD_RESET_REQUEST),
            authService.resetPasswordRequest(request, servletRequest, servletResponse),
            null);
    return ResponseEntity.ok().body(response);
  }

  @PostMapping("/reset-password-complete")
  public ResponseEntity<ApiResponse> resetPasswordComplete(
      @Valid @RequestBody ResetPasswordCompleteRequest request,
      HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) {
    authService.resetPasswordComplete(request, servletRequest, servletResponse);
    ApiResponse response =
        ApiResponse.builder()
            .success(true)
            .message(LocaleUtils.getMessage(PASSWORD_RESET_REQUEST_SUCCESS))
            .timestamp(OffsetDateTime.now())
            .build();
    return ResponseEntity.ok(response);
  }

  @PostMapping("/logout")
  public ResponseEntity<ApiResponse> logout(
      HttpServletRequest request, HttpServletResponse response) {
    new SecurityContextLogoutHandler().logout(request, response, null);
    return ResponseEntity.ok(
        ApiUtils.buildApiResponse(true, LocaleUtils.getMessage(SIMPLE_AUTH_LOGOUT), null, null));
  }
}
