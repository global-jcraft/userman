package com.huddey.core.userman.service;

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

import com.huddey.core.userman.configuration.JwtTokenProvider;
import com.huddey.core.userman.data.SecurityUser;
import com.huddey.core.userman.data.dto.*;
import com.huddey.core.userman.data.entity.*;
import com.huddey.core.userman.exception.*;
import com.huddey.core.userman.mapper.UserMapper;
import com.huddey.core.userman.repository.AuthProviderRepository;
import com.huddey.core.userman.repository.RoleRepository;
import com.huddey.core.userman.repository.UserRepository;
import com.huddey.core.userman.utils.RequestUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthService {

  private final UserRepository userRepository;
  private final AuthProviderRepository authProviderRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider tokenProvider;
  private final AuthenticationManager authenticationManager;
  private final UserDetailsService userDetailsService;

  public UserRegistrationResponse register(UserRegistrationRequest request)
      throws RoleNotFoundException {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new UserAlreadyExistsException("Email already registered");
    }

    User user = createNewUser(request);
    user = userRepository.save(user);

    // emailService.sendVerificationEmail(user.getEmail(), user.getEmailVerificationToken());

    return UserRegistrationResponse.builder()
        .userId(user.getId())
        .email(user.getEmail())
        .status(user.getStatus().toString())
        .message("Registration successful. Please verify your email.")
        .build();
  }

  public LoginResponse login(LoginRequest request) {
    try {
      Authentication authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

      SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
      User user = securityUser.getUser();

      /*if (!user.isEmailVerified()) {
          throw new EmailNotVerifiedException("Please verify your email before logging in");
      }*/

      user.setLastLoginAt(OffsetDateTime.now());
      user.setLastLoginIp(RequestUtil.getClientIp());
      userRepository.save(user);

      String accessToken = tokenProvider.generateAccessToken(securityUser);
      String refreshToken = tokenProvider.generateRefreshToken(securityUser);

      return LoginResponse.builder()
          .accessToken(accessToken)
          .refreshToken(refreshToken)
          .tokenType("Bearer")
          .expiresIn(tokenProvider.getAccessTokenValidity())
          .user(UserMapper.toDto(user))
          .build();
    } catch (BadCredentialsException ex) {
      throw new AuthenticationException("Invalid email or password");
    }
  }

  private User createNewUser(UserRegistrationRequest request) throws RoleNotFoundException {
    User user = new User();
    user.setEmail(request.getEmail());
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setCompanyName(request.getCompanyName());
    user.setPhoneNumber(request.getPhoneNumber());
    user.setStatus(UserStatus.PENDING);
    user.setRegistrationIp(RequestUtil.getClientIp());
    user.setEmailVerificationToken(generateVerificationToken());
    user.setEmailVerificationTokenExpiresAt(OffsetDateTime.now().plusHours(24));

    Role userRole =
        roleRepository
            .findByName(UserRole.USER.getRoleName())
            .orElseThrow(() -> new RoleNotFoundException("Default role not found"));
    user.getRoles().add(userRole);

    AuthProvider emailProvider =
        authProviderRepository
            .findByName("local")
            .orElseThrow(() -> new AuthProviderNotFoundException("Email provider not found"));

    UserCredential credentials = new UserCredential();
    credentials.setUser(user);
    credentials.setAuthProvider(emailProvider);
    credentials.setIdentifier(request.getEmail());
    credentials.setPasswordHash(passwordEncoder.encode(request.getPassword()));

    user.getCredentials().add(credentials);

    return user;
  }

  private String generateVerificationToken() {
    return UUID.randomUUID().toString();
  }

  public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
    if (!tokenProvider.validateToken(request.getRefreshToken())) {
      throw new InvalidTokenException("Invalid refresh token");
    }

    String email = tokenProvider.getUsername(request.getRefreshToken());
    UserDetails userDetails = userDetailsService.loadUserByUsername(email);
    SecurityUser securityUser = (SecurityUser) userDetails;

    if (!securityUser.isEnabled()) {
      throw new AccountStatusException("User account is not active");
    }

    String newAccessToken = tokenProvider.generateAccessToken(userDetails);
    String newRefreshToken = tokenProvider.generateRefreshToken(userDetails);

    return TokenRefreshResponse.builder()
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken)
        .tokenType("Bearer")
        .expiresIn(tokenProvider.getAccessTokenValidity())
        .build();
  }
}
