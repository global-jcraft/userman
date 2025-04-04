package com.huddey.core.userman.service;

import static com.huddey.core.userman.constants.Message.*;
import static com.huddey.core.userman.utils.RequestUtils.determineClientType;
import static com.huddey.core.userman.utils.RequestUtils.getLoginResponse;

import java.time.OffsetDateTime;
import java.util.UUID;

import javax.management.relation.RoleNotFoundException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.huddey.core.userman.auth.JwtAuthenticationFilter;
import com.huddey.core.userman.auth.JwtTokenProvider;
import com.huddey.core.userman.data.SecurityUser;
import com.huddey.core.userman.data.dto.*;
import com.huddey.core.userman.data.dto.response.LoginResponse;
import com.huddey.core.userman.data.dto.response.UserRegistrationResponse;
import com.huddey.core.userman.data.dto.token.TokenData;
import com.huddey.core.userman.data.dto.token.TokenRefreshResponse;
import com.huddey.core.userman.data.entity.*;
import com.huddey.core.userman.exception.*;
import com.huddey.core.userman.repository.AuthProviderRepository;
import com.huddey.core.userman.repository.RoleRepository;
import com.huddey.core.userman.repository.UserRepository;
import com.huddey.core.userman.security.token.MobileTokenGenerationStrategy;
import com.huddey.core.userman.security.token.TokenGenerationStrategy;
import com.huddey.core.userman.security.token.WebTokenGenerationStrategy;
import com.huddey.core.userman.utils.LocaleUtils;
import com.huddey.core.userman.utils.RequestUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final AuthProviderRepository authProviderRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final AuthenticationManager authenticationManager;
  private final UserDetailsService userDetailsService;
  private final CustomUserDetailsService customUserDetailsService;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Override
  public UserRegistrationResponse registerBasicFlow(
      UserRegistrationBasicFlowRequest user,
      HttpServletRequest servletRequest,
      HttpServletResponse servletResponse)
      throws RoleNotFoundException, UserAlreadyExistsException {

    TokenGenerationStrategy tokenGenerationStrategy;
    SecurityUser securityUser = customUserDetailsService.createNewUserBasicFlow(user);
    String clientType = determineClientType(servletRequest);

    // emailService.sendVerificationEmail(user.getEmail(), user.getEmailVerificationToken());

    if (clientType.equals("web")) {
      log.debug("Client type is web");
      tokenGenerationStrategy = new WebTokenGenerationStrategy(jwtTokenProvider);
      tokenGenerationStrategy.generateAndSetToken(servletResponse, securityUser, false);
      return UserRegistrationResponse.builder()
          .userId(securityUser.getUser().getId())
          .email(securityUser.getUser().getEmail())
          .firstName(securityUser.getUser().getFirstName())
          .lastName(securityUser.getUser().getLastName())
          .status(securityUser.getUser().getStatus().toString())
          .message("Registration successful. Please verify your email.")
          .build();
    } else {
      log.debug("Client type is mobile");
      tokenGenerationStrategy = new MobileTokenGenerationStrategy(jwtTokenProvider);
      tokenGenerationStrategy.generateAndSetToken(servletResponse, securityUser, false);
      return UserRegistrationResponse.builder()
          .userId(securityUser.getUser().getId())
          .email(securityUser.getUser().getEmail())
          .firstName(securityUser.getUser().getFirstName())
          .lastName(securityUser.getUser().getLastName())
          .status(securityUser.getUser().getStatus().toString())
          .message("Registration successful. Please verify your email.")
          .tokenData(
              TokenData.builder()
                  .accessToken(tokenGenerationStrategy.getAccessToken())
                  .refreshToken(tokenGenerationStrategy.getRefreshToken())
                  .expiresIn(jwtTokenProvider.getAccessTokenValidity())
                  .build())
          .build();
    }
  }

  @Override
  public UserRegistrationResponse completeRegistration(
      UserRegistrationRequest request,
      HttpServletRequest servletRequest,
      HttpServletResponse servletResponse)
      throws RoleNotFoundException, UserAlreadyExistsException {

    TokenGenerationStrategy tokenGenerationStrategy;
    SecurityUser securityUser = customUserDetailsService.updateUserInfo(request);
    String clientType = determineClientType(servletRequest);

    // emailService.sendVerificationEmail(user.getEmail(), user.getEmailVerificationToken());

    if (clientType.equals("web")) {
      log.debug("Client type is web");
      tokenGenerationStrategy = new WebTokenGenerationStrategy(jwtTokenProvider);
      tokenGenerationStrategy.generateAndSetToken(servletResponse, securityUser, false);
      return UserRegistrationResponse.builder()
          .userId(securityUser.getUser().getId())
          .email(securityUser.getUser().getEmail())
          .firstName(securityUser.getUser().getFirstName())
          .lastName(securityUser.getUser().getLastName())
          .status(securityUser.getUser().getStatus().toString())
          .message("Registration successful. Please verify your email.")
          .build();
    } else {
      log.debug("Client type is mobile");
      tokenGenerationStrategy = new MobileTokenGenerationStrategy(jwtTokenProvider);
      tokenGenerationStrategy.generateAndSetToken(servletResponse, securityUser, false);
      return UserRegistrationResponse.builder()
          .userId(securityUser.getUser().getId())
          .email(securityUser.getUser().getEmail())
          .firstName(securityUser.getUser().getFirstName())
          .lastName(securityUser.getUser().getLastName())
          .status(securityUser.getUser().getStatus().toString())
          .message("Registration successful. Please verify your email.")
          .tokenData(
              TokenData.builder()
                  .accessToken(tokenGenerationStrategy.getAccessToken())
                  .refreshToken(tokenGenerationStrategy.getRefreshToken())
                  .expiresIn(jwtTokenProvider.getAccessTokenValidity())
                  .build())
          .build();
    }
  }

  @Override
  public LoginResponse login(
      LoginRequest request,
      HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) {
    try {
      // Load user details first to validate existence and status
      UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
      // TODO: fix when notification service email send is done
      if (!userDetails.isEnabled()) {
        throw new AccountStatusException(LocaleUtils.getMessage(USER_ACCOUNT_ACTIVE_ERROR));
      }

      Authentication authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

      SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
      User user = securityUser.getUser();

      /*if (!user.isEmailVerified()) {
          throw new EmailNotVerifiedException("Please verify your email before logging in");
      }*/

      user.setLastLoginAt(OffsetDateTime.now());
      user.setLastLoginIp(RequestUtils.getClientIp());
      userRepository.save(user);

      TokenGenerationStrategy tokenGenerationStrategy;
      String clientType = determineClientType(servletRequest);

      return getLoginResponse(
          servletResponse,
          clientType,
          securityUser,
          user,
          jwtTokenProvider,
          request.isRememberMe());
    } catch (BadCredentialsException ex) {
      throw new AuthenticationException(LocaleUtils.getMessage(USER_INVALID_CREDENTIALS_ERROR));
    }
  }

  @Override
  public TokenRefreshResponse refreshToken(
      RefreshTokenRequest request,
      HttpServletRequest servletRequest,
      HttpServletResponse response) {
    if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
      throw new InvalidTokenException(LocaleUtils.getMessage(USER_INVALID_REFRESH_TOKEN));
    }

    String email = jwtTokenProvider.getUsername(request.getRefreshToken());
    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
    SecurityUser securityUser = (SecurityUser) userDetails;

    if (!securityUser.isEnabled()) {
      throw new AccountStatusException(LocaleUtils.getMessage(USER_ACCOUNT_ACTIVE));
    }

    String newAccessToken =
        jwtTokenProvider.generateAccessToken(userDetails, request.isRememberMe());
    String newRefreshToken =
        jwtTokenProvider.generateRefreshToken(userDetails, request.isRememberMe());

    // For web clients, set the tokens as secure HTTP-only cookies
    if (RequestUtils.determineClientType(servletRequest).equals("web")) {
      WebTokenGenerationStrategy tokenGenerationStrategy =
          new WebTokenGenerationStrategy(jwtTokenProvider);
      tokenGenerationStrategy.generateAndSetToken(response, securityUser, request.isRememberMe());

      response.addCookie(tokenGenerationStrategy.getAccessTokenCookie());
      response.addCookie(tokenGenerationStrategy.getRefreshTokenCookie());

      return TokenRefreshResponse.builder()
          .tokenType("Bearer")
          .expiresIn(jwtTokenProvider.getAccessTokenValidity())
          .build();
    }

    // For mobile clients, return tokens in the response body
    return TokenRefreshResponse.builder()
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken)
        .tokenType("Bearer")
        .expiresIn(jwtTokenProvider.getAccessTokenValidity())
        .build();
  }

  @Override
  public ResetPasswordResponse resetPasswordRequest(
      ResetPasswordRequest request,
      HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) {
    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new UserNotFoundException("User not found"));

    UserCredential localCredential =
        user.getCredentials().stream()
            .filter(credential -> "local".equals(credential.getAuthProvider().getName()))
            .findFirst()
            .orElseThrow(
                () ->
                    new UnsupportedOperationException(
                        "Password reset not supported for oauth2 users"));

    String resetToken = UUID.randomUUID().toString();
    OffsetDateTime tokenExpiresAt = OffsetDateTime.now().plusMinutes(30);
    localCredential.setPasswordResetToken(resetToken);
    localCredential.setPasswordResetTokenExpiresAt(tokenExpiresAt);
    userRepository.save(user);
    // send email functionality
    log.debug(
        "Reset password token for user {}: {} (expires at: {})",
        user.getEmail(),
        resetToken,
        tokenExpiresAt);
    return ResetPasswordResponse.builder().token(resetToken).success(true).build();
  }

  @Override
  public void resetPasswordComplete(
      ResetPasswordCompleteRequest request,
      HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) {
    // Find the user credential that matches the provided token
    UserCredential credential =
        userRepository.findAll().stream()
            .flatMap(user -> user.getCredentials().stream())
            .filter(creds -> request.getToken().equals(creds.getPasswordResetToken()))
            .findFirst()
            .orElseThrow(
                () -> new InvalidTokenException(LocaleUtils.getMessage(REFRESH_TOKEN_EXPIRED)));

    // Check if token has expired
    if (credential.getPasswordResetTokenExpiresAt() == null
        || credential.getPasswordResetTokenExpiresAt().isBefore(OffsetDateTime.now())) {
      throw new InvalidTokenException(LocaleUtils.getMessage(REFRESH_TOKEN_EXPIRED));
    }

    // Update the password in the credential
    credential.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));

    // Clear the reset token fields
    credential.setPasswordResetToken(null);
    credential.setPasswordResetTokenExpiresAt(null);

    // Save changes via the user entity
    userRepository.save(credential.getUser());
    log.debug("Password reset complete for user {}", credential.getUser().getEmail());
  }
}
