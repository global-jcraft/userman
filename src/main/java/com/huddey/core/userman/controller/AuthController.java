package com.huddey.core.userman.controller;

import static com.huddey.core.userman.constants.Message.*;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import javax.management.relation.RoleNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.huddey.core.userman.auth.JwtTokenProvider;
import com.huddey.core.userman.data.ApiResponse;
import com.huddey.core.userman.data.SecurityUser;
import com.huddey.core.userman.data.dto.*;
import com.huddey.core.userman.data.dto.response.*;
import com.huddey.core.userman.data.dto.token.TokenRefreshResponse;
import com.huddey.core.userman.exception.UserAlreadyExistsException;
import com.huddey.core.userman.mapper.UserMapper;
import com.huddey.core.userman.service.AuthService;
import com.huddey.core.userman.service.CustomUserDetailsService;
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
  final CustomUserDetailsService customUserDetailsService;

  @Autowired
  public AuthController(
      AuthService authService,
      JwtTokenProvider jwtTokenProvider,
      CustomUserDetailsService customUserDetailsService) {
    this.authService = authService;
    this.customUserDetailsService = customUserDetailsService;
  }

  @PostMapping("/basic-auth")
  public ResponseEntity<ApiResponse<UserRegistrationResponse>> registerBasicFlow(
      @Valid @RequestBody UserRegistrationBasicFlowRequest userRegistrationBasicFlowRequest,
      HttpServletRequest request,
      HttpServletResponse response)
      throws RoleNotFoundException, UserAlreadyExistsException {
    log.debug(
        "AuthController.registerBasicFlow() -> Registering a new user - Start time: {}",
        OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

    return ResponseEntity.ok(
        ApiResponse.success(
            LocaleUtils.getMessage(SIMPLE_AUTH_REG_SUCCESS),
            authService.registerBasicFlow(userRegistrationBasicFlowRequest, request, response)));
  }

  @PostMapping("/basic-auth-complete")
  @PreAuthorize(
      "isAuthenticated() and hasAnyAuthority('ROLE_USER', 'ROLE_CONTENT_CREATOR', 'ROLE_ADMIN')")
  public ResponseEntity<ApiResponse<UserRegistrationResponse>> registerBasicFlowComplete(
      @Valid @RequestBody UserRegistrationRequest userRegistrationRequest,
      HttpServletRequest request,
      HttpServletResponse response)
      throws RoleNotFoundException, UserAlreadyExistsException {
    log.debug(
        "AuthController.registerBasicFlowComplete() -> Update user missing info - Start time: {}",
        OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

    return ResponseEntity.ok(
        ApiResponse.success(
            LocaleUtils.getMessage(SIMPLE_FULL_AUTH_REG_SUCCESS),
            authService.completeRegistration(userRegistrationRequest, request, response)));
  }

  @GetMapping("/account-confirm")
  public ResponseEntity<ApiResponse<UserVerificationResponse>> confirmAccountRegistrations(
      @RequestParam String token, HttpServletRequest request, HttpServletResponse response) {
    log.debug(
        "AuthController.confirmAccountRegistrations() -> Confirming account registrations - Start time: {}",
        OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

    return ResponseEntity.ok(
        ApiResponse.success(
            LocaleUtils.getMessage(GLOBAL_USER_VERIFY_SUCCESS),
            authService.userAccountVerification(token, request, response)));
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<LoginResponse>> login(
      @Valid @RequestBody LoginRequest request,
      HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) {
    log.debug(
        "AuthController.login() -> Signing in - Start time: {}",
        OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

    return ResponseEntity.ok(
        ApiResponse.success(
            LocaleUtils.getMessage(SIMPLE_LOGIN_SUCCESS),
            authService.login(request, servletRequest, servletResponse)));
  }

  @PostMapping("/refresh-token")
  @PreAuthorize(
      "isAuthenticated() and hasAnyAuthority('ROLE_USER', 'ROLE_CONTENT_CREATOR', 'ROLE_ADMIN')")
  public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(
      @Valid @RequestBody RefreshTokenRequest request,
      HttpServletRequest servletRequest,
      HttpServletResponse response) {

    return ResponseEntity.ok(
        ApiResponse.success(
            LocaleUtils.getMessage(REFRESH_TOKEN_SUCCESS),
            authService.refreshToken(request, servletRequest, response)));
  }

  @PostMapping("/reset-password-request")
  @PreAuthorize(
      "isAuthenticated() and hasAnyAuthority('ROLE_USER', 'ROLE_CONTENT_CREATOR', 'ROLE_ADMIN')")
  public ResponseEntity<ApiResponse<ResetPasswordResponse>> resetPasswordRequest(
      @Valid @RequestBody ResetPasswordRequest request,
      HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) {

    return ResponseEntity.ok(
        ApiResponse.success(
            LocaleUtils.getMessage(PASSWORD_RESET_REQUEST),
            authService.resetPasswordRequest(request, servletRequest, servletResponse)));
  }

  @PostMapping("/reset-password-complete")
  public ResponseEntity<ApiResponse<Void>> resetPasswordComplete(
      @Valid @RequestBody ResetPasswordCompleteRequest request,
      HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) {
    authService.resetPasswordComplete(request, servletRequest, servletResponse);
    return ResponseEntity.ok(
        ApiResponse.success(LocaleUtils.getMessage(PASSWORD_RESET_REQUEST_SUCCESS)));
  }

  @PostMapping("/request-phone-verification")
  @PreAuthorize(
      "isAuthenticated() and hasAnyAuthority('ROLE_USER', 'ROLE_CONTENT_CREATOR', 'ROLE_ADMIN')")
  public ResponseEntity<ApiResponse<PhoneNumberVerificationResponse>> requestPhoneVerification(
      @Valid @RequestBody PhoneNumberVerificationRequest verificationRequest,
      HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) {
    log.debug(
        "Received request for phone number verification for email: {}",
        verificationRequest.getEmail());

    return ResponseEntity.ok(
        ApiResponse.success(
            LocaleUtils.getMessage(PHONE_VERIFICATION_SENT_SUCCESS),
            authService.requestPhoneNumberVerification(verificationRequest, servletRequest)));
  }

  @PostMapping("/verify-phone")
  @PreAuthorize(
      "isAuthenticated() and hasAnyAuthority('ROLE_USER', 'ROLE_CONTENT_CREATOR', 'ROLE_ADMIN')")
  public ResponseEntity<ApiResponse<VerifyPhoneNumberResponse>> verifyPhoneNumber(
      @Valid @RequestBody VerifyPhoneNumberRequest verifyRequest,
      HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) {
    log.debug("Received request to verify phone number for email: {}", verifyRequest.getEmail());
    return ResponseEntity.ok(
        ApiResponse.success(
            LocaleUtils.getMessage(PHONE_VERIFICATION_SUCCESS),
            authService.verifyPhoneNumber(verifyRequest, servletRequest)));
  }

  @GetMapping("/me")
  @PreAuthorize(
      "isAuthenticated() and hasAnyAuthority('ROLE_USER', 'ROLE_CONTENT_CREATOR', 'ROLE_ADMIN')")
  public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(Authentication authentication) {
    SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
    return ResponseEntity.ok(
        ApiResponse.success(
            LocaleUtils.getMessage(PROFILE_FETCH_SUCCESS),
            UserMapper.toDto(securityUser.getUser())));
  }
}
