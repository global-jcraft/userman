package com.huddey.core.userman.service;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;

import javax.management.relation.RoleNotFoundException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.huddey.core.notification.config.TokenService;
import com.huddey.core.userman.data.SecurityUser;
import com.huddey.core.userman.data.dto.UserRegistrationBasicFlowRequest;
import com.huddey.core.userman.data.dto.UserRegistrationRequest;
import com.huddey.core.userman.data.entity.*;
import com.huddey.core.userman.exception.AuthProviderNotFoundException;
import com.huddey.core.userman.exception.UserAlreadyExistsException;
import com.huddey.core.userman.exception.UserNotFoundException;
import com.huddey.core.userman.repository.AuthProviderRepository;
import com.huddey.core.userman.repository.RoleRepository;
import com.huddey.core.userman.repository.UserCredentialRepository;
import com.huddey.core.userman.repository.UserRepository;
import com.huddey.core.userman.utils.RequestUtils;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final AuthProviderRepository authProviderRepository;
  private final UserCredentialRepository userCredentialRepository;
  private final TokenService tokenService;

  @Value("${user.verification.token.expiry.hours:24}")
  private int verificationTokenExpiryHours;

  public CustomUserDetailsService(
      PasswordEncoder passwordEncoder,
      UserRepository userRepository,
      RoleRepository roleRepository,
      AuthProviderRepository authProviderRepository,
      UserCredentialRepository userCredentialRepository,
      TokenService tokenService) {
    this.passwordEncoder = passwordEncoder;
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.authProviderRepository = authProviderRepository;
    this.userCredentialRepository = userCredentialRepository;
    this.tokenService = tokenService;
  }

  /**
   * Load a user by email.
   *
   * @param email the email
   * @return the user details
   * @throws UsernameNotFoundException if the user is not found
   */
  @Override
  @Transactional
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () -> new UsernameNotFoundException("User not found with email: " + email));

    return new SecurityUser(user);
  }

  /**
   * Load a user by email.
   *
   * @param email
   * @return
   * @throws UsernameNotFoundException
   */
  @Transactional
  @Cacheable(value = "user-cache", key = "#email", unless = "#result == null")
  public User me(String email) throws UsernameNotFoundException {
    log.info("Loading user from database for email: {}", email);
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
  }

  /**
   * Create a new user.
   *
   * @param request the user registration request
   * @return the security user
   * @throws RoleNotFoundException if the default role is not found
   */
  @Transactional
  public SecurityUser createNewUserBasicFlow(UserRegistrationBasicFlowRequest request)
      throws RoleNotFoundException {
    Optional<User> user = checkIfUserExists(request.getEmail());
    if (user.isPresent()) {
      throw new UserAlreadyExistsException("User already exists with email: " + request.getEmail());
    }
    var newUser = buildUserBasicFlow(request);
    assignDefaultRole(newUser);
    userRepository.save(newUser);
    saveUserCredentials(newUser, request.getPassword());
    return new SecurityUser(newUser);
  }

  /**
   * Complete user info
   *
   * @param request the user registration request
   * @return the security user
   * @throws RoleNotFoundException if the default role is not found
   */
  @Transactional
  public SecurityUser updateUserInfo(UserRegistrationRequest request) throws RoleNotFoundException {
    User existingUser =
        checkIfUserExists(request.getEmail())
            .orElseThrow(
                () ->
                    new UserNotFoundException(
                        "User with email: " + request.getEmail() + " not found"));

    var verificationToken = tokenService.generateEmailConfirmationLink(request.getEmail());

    existingUser.setFirstName(request.getFirstName());
    existingUser.setLastName(request.getLastName());
    existingUser.setCompanyName(request.getCompanyName());
    existingUser.setPhoneNumber(request.getPhoneNumber());
    existingUser.setUpdatedAt(OffsetDateTime.now());
    existingUser.setRegistrationIp(RequestUtils.getClientIp());
    existingUser.setEmailVerificationToken(verificationToken);
    existingUser.setEmailVerificationTokenExpiresAt(
        OffsetDateTime.now().plusHours(verificationTokenExpiryHours));
    if (request.getPhoneNumber() != null
        && !request.getPhoneNumber().isBlank()
        && (existingUser.getPhoneNumber() == null
            || !existingUser.getPhoneNumber().equals(request.getPhoneNumber())
            || !existingUser.isPhoneNumberVerified())) {
      existingUser.setPhoneNumberVerified(
          false); // Reset verification if phone number changes or was not verified
      existingUser.setPhoneNumberVerificationToken(null);
      existingUser.setPhoneNumberVerificationTokenExpiresAt(null);
    }
    userRepository.save(existingUser);

    return new SecurityUser(existingUser);
  }

  /**
   * Check if a user with the given email exists.
   *
   * @param email the email
   */
  private Optional<User> checkIfUserExists(String email) {
    return userRepository.findByEmail(email);
  }

  /**
   * Build a new user from the registration request.
   *
   * @param request the user registration request
   * @return the user
   */
  private User buildUserBasicFlow(UserRegistrationBasicFlowRequest request) {
    User user = new User();
    user.setEmail(request.getEmail());
    user.setRoles(new HashSet<>());
    user.setCredentials(new HashSet<>());
    user.setStatus(UserStatus.PENDING);
    user.setRegistrationIp(RequestUtils.getClientIp());
    user.setCreatedAt(OffsetDateTime.now());
    /*    user.setEmailVerificationToken(generateVerificationToken());
    user.setEmailVerificationTokenExpiresAt(
        OffsetDateTime.now().plusHours(verificationTokenExpiryHours));*/
    return user;
  }

  /**
   * Build a new user from the registration request.
   *
   * @param request the user registration request
   * @return the user
   */
  private User buildUser(UserRegistrationRequest request) {
    User user = new User();
    user.setEmail(request.getEmail());
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setCompanyName(request.getCompanyName());
    user.setPhoneNumber(request.getPhoneNumber());
    user.setRoles(new HashSet<>());
    user.setCredentials(new HashSet<>());
    user.setStatus(UserStatus.PENDING);
    user.setRegistrationIp(RequestUtils.getClientIp());
    // user.setEmailVerificationToken(generateVerificationToken());
    user.setEmailVerificationTokenExpiresAt(
        OffsetDateTime.now().plusHours(verificationTokenExpiryHours));
    if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
      user.setPhoneNumberVerified(false); // Initialize as not verified
    }
    return user;
  }

  /**
   * Assign the default role to the user.
   *
   * @param user the user
   * @throws RoleNotFoundException if the default role is not found
   */
  private void assignDefaultRole(User user) throws RoleNotFoundException {
    Role userRole =
        roleRepository
            .findByName(UserRole.USER.getRoleName())
            .orElseThrow(() -> new RoleNotFoundException("Default role not found"));
    if (!userRole.getName().startsWith("ROLE_")) {
      throw new IllegalArgumentException("Role name must start with ROLE_ prefix");
    }
    user.getRoles().add(userRole);
  }

  /**
   * Save the user credentials.
   *
   * @param user the user
   * @param password the password
   */
  private void saveUserCredentials(User user, String password) {
    AuthProvider emailProvider =
        authProviderRepository
            .findByName("local")
            .orElseThrow(() -> new AuthProviderNotFoundException("Email provider not found"));
    UserCredential credentials = new UserCredential();
    credentials.setUser(user);
    credentials.setAuthProvider(emailProvider);
    credentials.setIdentifier(user.getEmail());
    credentials.setPasswordHash(passwordEncoder.encode(password));
    user.getCredentials().add(credentials);
    userCredentialRepository.save(credentials);
  }
}
