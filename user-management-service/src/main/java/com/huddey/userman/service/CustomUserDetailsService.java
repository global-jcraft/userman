package com.huddey.userman.service;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import javax.management.relation.RoleNotFoundException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.huddey.userman.data.SecurityUser;
import com.huddey.userman.data.dto.UserRegistrationBasicFlowRequest;
import com.huddey.userman.data.dto.UserRegistrationRequest;
import com.huddey.userman.data.entity.*;
import com.huddey.userman.exception.AuthProviderNotFoundException;
import com.huddey.userman.exception.UserAlreadyExistsException;
import com.huddey.userman.exception.UserNotFoundException;
import com.huddey.userman.repository.AuthProviderRepository;
import com.huddey.userman.repository.RoleRepository;
import com.huddey.userman.repository.UserCredentialRepository;
import com.huddey.userman.repository.UserRepository;
import com.huddey.userman.utils.RequestUtils;

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

  @Value("${user.verification.token.expiry.hours:24}")
  private int verificationTokenExpiryHours;

  public CustomUserDetailsService(
      PasswordEncoder passwordEncoder,
      UserRepository userRepository,
      RoleRepository roleRepository,
      AuthProviderRepository authProviderRepository,
      UserCredentialRepository userCredentialRepository) {
    this.passwordEncoder = passwordEncoder;
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.authProviderRepository = authProviderRepository;
    this.userCredentialRepository = userCredentialRepository;
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

    existingUser.setFirstName(request.getFirstName());
    existingUser.setLastName(request.getLastName());
    existingUser.setCompanyName(request.getCompanyName());
    existingUser.setPhoneNumber(request.getPhoneNumber());
    existingUser.setUpdatedAt(OffsetDateTime.now());
    existingUser.setRegistrationIp(RequestUtils.getClientIp());
    existingUser.setEmailVerificationToken(generateVerificationToken());
    existingUser.setEmailVerificationTokenExpiresAt(
        OffsetDateTime.now().plusHours(verificationTokenExpiryHours));
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
    return User.builder()
        .email(request.getEmail())
        .roles(new HashSet<>())
        .credentials(new HashSet<>())
        .status(UserStatus.PENDING)
        .registrationIp(RequestUtils.getClientIp())
        .createdAt(OffsetDateTime.now())
        .emailVerificationToken(generateVerificationToken())
        .emailVerificationTokenExpiresAt(
            OffsetDateTime.now().plusHours(verificationTokenExpiryHours))
        .build();
  }

  /**
   * Build a new user from the registration request.
   *
   * @param request the user registration request
   * @return the user
   */
  private User buildUser(UserRegistrationRequest request) {
    return User.builder()
        .email(request.getEmail())
        .firstName(request.getFirstName())
        .lastName(request.getLastName())
        .companyName(request.getCompanyName())
        .phoneNumber(request.getPhoneNumber())
        .roles(new HashSet<>())
        .credentials(new HashSet<>())
        .status(UserStatus.PENDING)
        .registrationIp(RequestUtils.getClientIp())
        .emailVerificationToken(generateVerificationToken())
        .emailVerificationTokenExpiresAt(
            OffsetDateTime.now().plusHours(verificationTokenExpiryHours))
        .build();
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
    UserCredential credentials =
        UserCredential.builder()
            .user(user)
            .authProvider(emailProvider)
            .identifier(user.getEmail())
            .passwordHash(passwordEncoder.encode(password))
            .build();
    user.getCredentials().add(credentials);
    userCredentialRepository.save(credentials);
  }

  /**
   * Generate a verification token.
   *
   * @return the verification token
   */
  private String generateVerificationToken() {
    return UUID.randomUUID().toString();
  }
}
