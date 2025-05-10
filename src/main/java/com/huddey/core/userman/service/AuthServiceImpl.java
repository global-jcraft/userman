package com.huddey.core.userman.service;

import static com.huddey.core.notification.data.constants.NotificationConstants.EMAIL_NOTIFICATION;
import static com.huddey.core.userman.constants.Message.*;
import static com.huddey.core.userman.constants.UsermanConstants.WEB_CLIENT_TYPE;
import static com.huddey.core.userman.utils.ApiUtils.buildTokenResponse;
import static com.huddey.core.userman.utils.RequestUtils.*;
import static com.huddey.core.userman.utils.RequestUtils.getUserRegistrationResponse;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.management.relation.RoleNotFoundException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.huddey.core.notification.config.TokenService;
import com.huddey.core.notification.data.DecodedTokenData;
import com.huddey.core.notification.service.NotificationHandler;
import com.huddey.core.notification.utils.SecurityUtils;
import com.huddey.core.userman.auth.JwtAuthenticationFilter;
import com.huddey.core.userman.auth.JwtTokenProvider;
import com.huddey.core.userman.data.SecurityUser;
import com.huddey.core.userman.data.dto.*;
import com.huddey.core.userman.data.dto.response.LoginResponse;
import com.huddey.core.userman.data.dto.response.UserRegistrationResponse;
import com.huddey.core.userman.data.dto.response.UserVerificationResponse;
import com.huddey.core.userman.data.dto.token.TokenRefreshResponse;
import com.huddey.core.userman.data.entity.Role;
import com.huddey.core.userman.data.entity.User;
import com.huddey.core.userman.data.entity.UserCredential;
import com.huddey.core.userman.data.entity.UserStatus;
import com.huddey.core.userman.exception.*;
import com.huddey.core.userman.repository.AuthProviderRepository;
import com.huddey.core.userman.repository.RoleRepository;
import com.huddey.core.userman.repository.UserRepository;
import com.huddey.core.userman.token.WebTokenGenerationStrategy;
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
  private final NotificationHandler notificationHandler;
  private final TokenService tokenService;

  @Value("${app.confirmation.baseUrl}")
  private String baseUrl;

  @Override
  public UserRegistrationResponse registerBasicFlow(
      UserRegistrationBasicFlowRequest user,
      HttpServletRequest servletRequest,
      HttpServletResponse servletResponse)
      throws RoleNotFoundException, UserAlreadyExistsException {

    SecurityUser securityUser = customUserDetailsService.createNewUserBasicFlow(user);
    String clientType = determineClientType(servletRequest);

    return getUserRegistrationResponse(servletResponse, clientType, securityUser, jwtTokenProvider);
  }

  @Override
  public UserRegistrationResponse completeRegistration(
      UserRegistrationRequest request,
      HttpServletRequest servletRequest,
      HttpServletResponse servletResponse)
      throws RoleNotFoundException, UserAlreadyExistsException {

    SecurityUser securityUser = customUserDetailsService.updateUserInfo(request);
    String clientType = determineClientType(servletRequest);

    String confirmationLink =
        baseUrl + "/account-confirm?token=" + securityUser.getUser().getEmailVerificationToken();

    notificationHandler.notify(
        EMAIL_NOTIFICATION,
        securityUser.getUser().getEmail(),
        securityUser.getUser().getEmailVerificationToken(),
        securityUser.getUsername(),
        confirmationLink);

    return getUserRegistrationResponse(servletResponse, clientType, securityUser, jwtTokenProvider);
  }

  @Override
  public UserVerificationResponse userAccountVerification(
      String token, HttpServletRequest request, HttpServletResponse response) {
    log.debug("Verifying user by email with token: {}", token);
    User user =
        userRepository
            .findByEmailVerificationToken(token)
            .orElseThrow(() -> new UserNotFoundException("User not found with token: " + token));

    if (user.getEmailVerificationTokenExpiresAt().isBefore(OffsetDateTime.now())) {
      log.error("User verification token expired: {}", user.getEmail());
      throw new InvalidTokenException(LocaleUtils.getMessage(USER_EXPIRED_VERIFY_TOKEN));
    }

    if (!token.equals(user.getEmailVerificationToken()) || token == null) {
      log.error("Invalid token for user: {}", user.getEmail());
      throw new InvalidTokenException(LocaleUtils.getMessage(USER_VERIFY_INVALID_TOKEN));
    }

    if (!SecurityUtils.constantTimeEquals(token, user.getEmailVerificationToken())) {
      log.error("Invalid token for user: {}", user.getEmail());
      throw new InvalidTokenException(LocaleUtils.getMessage(USER_VERIFY_INVALID_TOKEN));
    }

    DecodedTokenData decodedTokenData = tokenService.decodeToken(token);
    if (!decodedTokenData.getEmail().equals(user.getEmail())) {
      log.error("Invalid token for user: {}", user.getEmail());
      throw new InvalidTokenException(LocaleUtils.getMessage(USER_VERIFY_INVALID_TOKEN));
    }
    log.debug("User verified successfully: {}", user.getEmail());

    user.setEmailVerificationToken(null);
    user.setEmailVerified(true);
    user.setEmailVerificationTokenExpiresAt(null);
    user.setStatus(UserStatus.ACTIVE);
    user.setUpdatedAt(OffsetDateTime.now());
    userRepository.save(user);

    log.debug("User status updated to ACTIVE: {}", user.getEmail());
    var tokenGenerationStrategy = new WebTokenGenerationStrategy(jwtTokenProvider);

    SecurityUser securityUser = new SecurityUser(user);
    tokenGenerationStrategy.generateAndSetToken(response, securityUser, false);

    return UserVerificationResponse.builder()
        .userId(user.getId())
        .email(user.getEmail())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .status(user.getStatus().toString())
        .message(LocaleUtils.getMessage(GLOBAL_USER_VERIFY_SUCCESS))
        .role(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
        .tokenData(buildTokenResponse(tokenGenerationStrategy, jwtTokenProvider))
        .build();
  }

  @Override
  public LoginResponse login(
      LoginRequest request,
      HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) {
    User user = null;
    try {
      // Load user details first to validate existence and status
      UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

      if (!userDetails.isEnabled()) {
        throw new AccountStatusException(LocaleUtils.getMessage(USER_ACCOUNT_ACTIVE_ERROR));
      }

      Authentication authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

      SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
      user = securityUser.getUser();

      if (!user.isEmailVerified()) {
        // FIXME: This should be handled in the frontend
        // throw new EmailNotVerifiedException("Please verify your email before logging in");
      }

      user.setLastLoginAt(OffsetDateTime.now());
      user.setLastLoginIp(RequestUtils.getClientIp());
      userRepository.save(user);

      String clientType = determineClientType(servletRequest);

      return getLoginResponse(
          servletResponse,
          clientType,
          securityUser,
          user,
          jwtTokenProvider,
          request.isRememberMe());
    } catch (BadCredentialsException ex) {
      assert user != null;
      log.error("Username or password is wrong for the user: {}", user.getEmail());
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
    if (determineClientType(servletRequest).equals(WEB_CLIENT_TYPE)) {
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
