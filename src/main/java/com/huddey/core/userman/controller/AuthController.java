package com.huddey.core.userman.controller;

import javax.management.relation.RoleNotFoundException;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.huddey.core.userman.data.dto.*;
import com.huddey.core.userman.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<UserRegistrationResponse> register(
      @Valid @RequestBody UserRegistrationRequest request) throws RoleNotFoundException {
    return ResponseEntity.ok(authService.register(request));
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  /*@GetMapping("/verify-email/{token}")
  public ResponseEntity<ApiResponse> verifyEmail(@PathVariable String token) {
      authService.verifyEmail(token);
      return ResponseEntity.ok(new ApiResponse("Email verified successfully"));
  }*/

  @PostMapping("/refresh-token")
  public ResponseEntity<TokenRefreshResponse> refreshToken(
      @Valid @RequestBody TokenRefreshRequest request) {
    return ResponseEntity.ok(authService.refreshToken(request));
  }
}
