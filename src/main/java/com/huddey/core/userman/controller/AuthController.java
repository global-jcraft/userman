package com.huddey.core.userman.controller;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import javax.management.relation.RoleNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.huddey.core.userman.auth.JwtTokenProvider;
import com.huddey.core.userman.data.dto.*;
import com.huddey.core.userman.data.dto.response.LoginResponse;
import com.huddey.core.userman.data.dto.response.UserRegistrationResponse;
import com.huddey.core.userman.data.dto.token.TokenRefreshResponse;
import com.huddey.core.userman.exception.UserAlreadyExistsException;
import com.huddey.core.userman.service.AuthService;
import com.huddey.core.userman.service.CustomUserDetailsService;

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
  public ResponseEntity<UserRegistrationResponse> registerBasicFlow(
      @Valid @RequestBody UserRegistrationBasicFlowRequest userRegistrationBasicFlowRequest,
      HttpServletRequest request,
      HttpServletResponse response)
      throws RoleNotFoundException, UserAlreadyExistsException {
    log.info(
        "AuthController.registerBasicFlow() -> Registering a new user - Start time: {}",
        OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    return ResponseEntity.ok(
        authService.registerBasicFlow(userRegistrationBasicFlowRequest, request, response));
  }

  @PostMapping("/basic-auth-complete")
  public ResponseEntity<UserRegistrationResponse> registerBasicFlowComplete(
      @Valid @RequestBody UserRegistrationRequest userRegistrationRequest,
      HttpServletRequest request,
      HttpServletResponse response)
      throws RoleNotFoundException, UserAlreadyExistsException {
    log.info(
        "AuthController.registerBasicFlowComplete() -> Update user missing info - Start time: {}",
        OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    return ResponseEntity.ok(
        authService.completeRegistration(userRegistrationRequest, request, response));
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(
      @Valid @RequestBody LoginRequest request,
      HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) {
    log.info(
        "AuthController.login() -> Signing in - Start time: {}",
        OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    return ResponseEntity.ok(authService.login(request, servletRequest, servletResponse));
  }

  /*@GetMapping("/verify-email/{token}")
  public ResponseEntity<ApiResponse> verifyEmail(@PathVariable String token) {
      authService.verifyEmail(token);
      return ResponseEntity.ok(new ApiResponse("Email verified successfully"));
  }*/

  @PostMapping("/refresh-token")
  public ResponseEntity<TokenRefreshResponse> refreshToken(
      @Valid @RequestBody TokenRefreshRequest request,
      HttpServletRequest servletRequest,
      HttpServletResponse response) {
    return ResponseEntity.ok(authService.refreshToken(request, servletRequest, response));
  }
}
