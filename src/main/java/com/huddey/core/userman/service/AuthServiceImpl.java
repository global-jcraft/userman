package com.huddey.core.userman.service;

import static com.huddey.core.common.api.ApiUtils.buildTokenResponse;
import static com.huddey.core.notification.data.constants.NotificationConstants.EMAIL_NOTIFICATION;
import static com.huddey.core.notification.data.constants.NotificationConstants.SMS_NOTIFICATION;
import static com.huddey.core.userman.constants.Message.*;
import static com.huddey.core.userman.constants.UsermanConstants.WEB_CLIENT_TYPE;
import static com.huddey.core.userman.utils.RequestUtils.*;
import static com.huddey.core.userman.utils.RequestUtils.getUserRegistrationResponse;

import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.management.relation.RoleNotFoundException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.huddey.core.common.utils.LocaleUtils;
import com.huddey.core.notification.config.TokenService;
import com.huddey.core.notification.data.DecodedTokenData;
import com.huddey.core.notification.service.NotificationHandler;
import com.huddey.core.notification.utils.SecurityUtils;
import com.huddey.core.payment.service.SubscriptionService;
import com.huddey.core.userman.auth.JwtAuthenticationFilter;
import com.huddey.core.userman.auth.JwtTokenProvider;
import com.huddey.core.userman.data.SecurityUser;
import com.huddey.core.userman.data.dto.*;
import com.huddey.core.userman.data.dto.response.*;
import com.huddey.core.userman.data.dto.token.TokenRefreshResponse;
import com.huddey.core.userman.data.entity.Role;
import com.huddey.core.userman.data.entity.User;
import com.huddey.core.userman.data.entity.UserCredential;
import com.huddey.core.userman.data.entity.UserStatus;
import com.huddey.core.userman.exception.*;
import com.huddey.core.userman.repository.AuthProviderRepository;
import com.huddey.core.userman.repository.RoleRepository;
import com.huddey.core.userman.repository.UserCredentialRepository;
import com.huddey.core.userman.repository.UserRepository;
import com.huddey.core.userman.token.WebTokenGenerationStrategy;
import com.huddey.core.userman.utils.RequestUtils;
import com.stripe.exception.StripeException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
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
  private final UserCredentialRepository userCredentialRepository;
  private final SubscriptionService subscriptionService;

  @Value("${app.confirmation.baseUrl}")
  private String baseUrl;

  @Value("${app.phone.verification.token.expiry.minutes:10}") // Default to 10 minutes
  private long phoneTokenExpiryMinutes;

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
  @Transactional
  public UserRegistrationResponse completeRegistration(
      UserRegistrationRequest request,
      HttpServletRequest servletRequest,
      HttpServletResponse servletResponse)
      throws RoleNotFoundException, UserAlreadyExistsException, StripeException {

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

    var customer =
        subscriptionService.getOrCreateStripeCustomer(
            securityUser.getUser().getId(), securityUser.getUser().getEmail());
    var freeSubscription =
        subscriptionService.createFreeLocalSubscription(
            securityUser.getUser().getId(),
            customer.getId(),
            "huddey_free", // planKey
            "month", // billing interval
            "EUR", // default currency (adjust if you detect from user/locale)
            1L // seats
            );

    var regResponse =
        getUserRegistrationResponse(servletResponse, clientType, securityUser, jwtTokenProvider);

    regResponse.setUserSubscription(
        UserSubscriptionResponse.builder()
            .id(freeSubscription.getId())
            .userId(freeSubscription.getUserId())
            .stripeCustomerId(freeSubscription.getStripeCustomerId())
            .stripeSubscriptionId(
                freeSubscription.getStripeSubscriptionId()) // will be null for Free
            .planKey(freeSubscription.getPlanKey())
            .billingInterval(freeSubscription.getBillingInterval())
            .currency(freeSubscription.getCurrency())
            .seatCount(1L)
            .status(freeSubscription.getStatus())
            .createdAt(freeSubscription.getCreatedAt())
            .updatedAt(freeSubscription.getUpdatedAt())
            .currentPeriodStart(freeSubscription.getCurrentPeriodStart())
            .currentPeriodEnd(freeSubscription.getCurrentPeriodEnd())
            .cancelAtPeriodEnd(freeSubscription.isCancelAtPeriodEnd())
            .build());

    return regResponse;
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
    long startTime = System.currentTimeMillis();
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
      User user = securityUser.getUser();

      if (!user.isEmailVerified()) {
        // FIXME: This should be handled in the frontend
        // throw new EmailNotVerifiedException("Please verify your email before logging in");
      }

      updateLastLogin(user);

      String clientType = determineClientType(servletRequest);

      LoginResponse response =
          getLoginResponse(
              servletResponse,
              clientType,
              securityUser,
              user,
              jwtTokenProvider,
              request.isRememberMe());

      long duration = System.currentTimeMillis() - startTime;
      log.debug("Login completed for {} in {}ms", request.getEmail(), duration);
      return response;
    } catch (BadCredentialsException ex) {
      log.error("Username or password is wrong for the user: {}", request.getEmail());
      throw new AuthenticationException(LocaleUtils.getMessage(USER_INVALID_CREDENTIALS_ERROR));
    }
  }

  @Async("loginTaskExecutor")
  @Transactional
  protected void updateLastLogin(User user) {
    user.setLastLoginAt(OffsetDateTime.now());
    user.setLastLoginIp(RequestUtils.getClientIp());
    userRepository.save(user);
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

      response.addHeader("Set-Cookie", tokenGenerationStrategy.getAccessTokenCookie().toString());
      response.addHeader("Set-Cookie", tokenGenerationStrategy.getRefreshTokenCookie().toString());

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
  @Transactional
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

    String resetLink = baseUrl + "/reset-password?token=" + resetToken;
    var securityUser = new SecurityUser(user);
    notificationHandler.notify(
        EMAIL_NOTIFICATION,
        securityUser.getUser().getEmail(),
        securityUser.getUser().getCredentials().stream()
                .filter(f -> f.getAuthProvider().getName().equals("local"))
                .map(UserCredential::getPasswordResetToken)
                .findFirst()
                .isPresent()
            ? localCredential.getPasswordResetToken()
            : null,
        securityUser.getUsername(),
        resetLink);

    log.debug(
        "Reset password token for user {}: {} (expires at: {})",
        user.getEmail(),
        resetToken,
        tokenExpiresAt);
    return ResetPasswordResponse.builder().token(resetToken).success(true).build();
  }

  @Override
  @Transactional
  public void resetPasswordComplete(
      ResetPasswordCompleteRequest request,
      HttpServletRequest servletRequest,
      HttpServletResponse servletResponse) {
    // Find the user credential that matches the provided token
    UserCredential credential =
        userCredentialRepository
            .findByPasswordResetToken(request.getToken())
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

  @Override
  @Transactional
  public PhoneNumberVerificationResponse requestPhoneNumberVerification(
      PhoneNumberVerificationRequest request, HttpServletRequest servletRequest) {
    log.debug("Received request for phone number verification for email: {}", request.getEmail());
    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(
                () ->
                    new UserNotFoundException("User not found with email: " + request.getEmail()));

    if (request.getPhoneNumber() == null
        || request.getPhoneNumber().isBlank()
        || !request.getPhoneNumber().matches("\\+?[0-9]+")) {
      throw new InvalidInputException(LocaleUtils.getMessage(PHONE_NUMBER_REQUIRED));
    }

    if (user.getPhoneNumber() == null || user.getPhoneNumber().isBlank()) {
      throw new InvalidInputException(LocaleUtils.getMessage(PHONE_NUMBER_REQUIRED));
    }

    if (user.isPhoneNumberVerified()) {
      return PhoneNumberVerificationResponse.builder()
          .message(LocaleUtils.getMessage(PHONE_ALREADY_VERIFIED))
          .build();
    }

    String otp = generateOtp();
    user.setPhoneNumberVerificationToken(otp);
    user.setPhoneNumberVerificationTokenExpiresAt(
        OffsetDateTime.now().plusMinutes(phoneTokenExpiryMinutes));
    userRepository.save(user);

    // TODO: Implement actual SMS sending logic here
    // For now, we'll log the OTP
    log.debug("OTP for phone number verification for user {}: {}", user.getEmail(), otp);
    notificationHandler.notify(SMS_NOTIFICATION, user.getPhoneNumber(), otp, user.getEmail(), null);

    return PhoneNumberVerificationResponse.builder()
        .message(LocaleUtils.getMessage(PHONE_VERIFICATION_SENT_SUCCESS))
        .build();
  }

  @Override
  public VerifyPhoneNumberResponse verifyPhoneNumber(
      VerifyPhoneNumberRequest request, HttpServletRequest servletRequest) {
    User user =
        userRepository
            .findByEmail(request.getEmail())
            .orElseThrow(
                () ->
                    new UserNotFoundException("User not found with email: " + request.getEmail()));

    if (user.isPhoneNumberVerified()) {
      return VerifyPhoneNumberResponse.builder()
          .success(true)
          .status("ALREADY_VERIFIED")
          .message(LocaleUtils.getMessage(PHONE_ALREADY_VERIFIED))
          .build();
    }

    if (user.getPhoneNumberVerificationToken() == null
        || user.getPhoneNumberVerificationTokenExpiresAt() == null) {
      throw new InvalidTokenException(LocaleUtils.getMessage(PHONE_VERIFICATION_INVALID_TOKEN));
    }

    if (user.getPhoneNumberVerificationTokenExpiresAt().isBefore(OffsetDateTime.now())) {
      user.setPhoneNumberVerificationToken(null);
      user.setPhoneNumberVerificationTokenExpiresAt(null);
      userRepository.save(user);
      throw new InvalidTokenException(LocaleUtils.getMessage(PHONE_VERIFICATION_EXPIRED_TOKEN));
    }

    if (!SecurityUtils.constantTimeEquals(
        request.getToken(), user.getPhoneNumberVerificationToken())) {
      // Optional: Implement attempt counting to prevent brute-force
      throw new InvalidTokenException(LocaleUtils.getMessage(PHONE_VERIFICATION_INVALID_TOKEN));
    }

    user.setPhoneNumberVerified(true);
    user.setPhoneNumberVerificationToken(null);
    user.setPhoneNumberVerificationTokenExpiresAt(null);
    // Optionally, update user status if phone verification is a prerequisite for ACTIVE status
    // if (user.isEmailVerified()) { // Example condition
    //     user.setStatus(UserStatus.ACTIVE);
    // }
    userRepository.save(user);

    return VerifyPhoneNumberResponse.builder()
        .success(true)
        .status("PHONE_VERIFIED")
        .message(LocaleUtils.getMessage(PHONE_VERIFICATION_SUCCESS))
        .build();
  }

  private String generateOtp() {
    // Generate a 6-digit OTP
    return new DecimalFormat("000000").format(new SecureRandom().nextInt(999999));
  }
}
