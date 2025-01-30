package com.huddey.core.userman;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Optional;

import javax.management.relation.RoleNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.huddey.core.userman.data.SecurityUser;
import com.huddey.core.userman.data.dto.UserRegistrationRequest;
import com.huddey.core.userman.data.entity.*;
import com.huddey.core.userman.exception.AuthProviderNotFoundException;
import com.huddey.core.userman.exception.UserAlreadyExistsException;
import com.huddey.core.userman.repository.*;
import com.huddey.core.userman.service.CustomUserDetailsService;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private RoleRepository roleRepository;
  @Mock private AuthProviderRepository authProviderRepository;
  @Mock private UserCredentialRepository userCredentialRepository;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private CustomUserDetailsService customUserDetailsService;

  private User user;
  private UserRegistrationRequest request;

  @BeforeEach
  public void setUp() {
    user = User.builder().email("test@example.com").firstName("John").lastName("Doe").build();

    request =
        UserRegistrationRequest.builder()
            .email("test@example.com")
            .firstName("John")
            .lastName("Doe")
            .password("password")
            .build();
  }

  @Test
  @DisplayName("Test load user by username when user exists")
  void testLoadUserByUsername_UserExists() {
    User userWithRoles =
        User.builder()
            .email("test@example.com")
            .firstName("John")
            .lastName("Doe")
            .roles(new HashSet<>())
            .build();

    Role userRole = Role.builder().id(1L).name("ROLE_USER").build();
    userWithRoles.getRoles().add(userRole);

    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userWithRoles));

    UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

    assertNotNull(userDetails);
    assertEquals("test@example.com", userDetails.getUsername());
    assertFalse(userDetails.getAuthorities().isEmpty());
    assertTrue(
        userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
  }

  @Test
  @DisplayName("Test load user by username when user not found")
  void testLoadUserByUsername_UserNotFound() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

    assertThrows(
        UsernameNotFoundException.class,
        () -> {
          customUserDetailsService.loadUserByUsername("test@example.com");
        });
  }

  @Test
  @DisplayName("Test create new user success")
  void testCreateNewUser_Success() throws RoleNotFoundException {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
    Role userRole =
        Role.builder()
            .id(1L)
            .name("ROLE_USER") // Make sure the role name has the ROLE_ prefix
            .build();
    when(roleRepository.findByName(UserRole.USER.getRoleName())).thenReturn(Optional.of(userRole));
    AuthProvider authProvider = AuthProvider.builder().id(1L).name("local").build();
    when(authProviderRepository.findByName("local")).thenReturn(Optional.of(authProvider));
    when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

    SecurityUser securityUser = customUserDetailsService.createNewUser(request);

    assertNotNull(securityUser);
    assertEquals("test@example.com", securityUser.getUsername());
    assertFalse(securityUser.getAuthorities().isEmpty(), "User should have authorities");
    assertTrue(
        securityUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")),
        "User should have ROLE_USER authority");
    verify(userRepository, times(1)).save(any(User.class));
    verify(userCredentialRepository, times(1)).save(any(UserCredential.class));
  }

  @Test
  @DisplayName("Test create new user when user already exists")
  void testCreateNewUser_UserAlreadyExistsException() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

    assertThrows(
        UserAlreadyExistsException.class,
        () -> {
          customUserDetailsService.createNewUser(request);
        });
  }

  @Test
  @DisplayName("Test create new user when role not found")
  void testCreateNewUser_UserAlreadyExists() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

    assertThrows(
        UserAlreadyExistsException.class,
        () -> {
          customUserDetailsService.createNewUser(request);
        });
  }

  @Test
  @DisplayName("Test create new user when role not found")
  void testCreateNewUser_RoleNotFound() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
    when(roleRepository.findByName(UserRole.USER.getRoleName())).thenReturn(Optional.empty());

    assertThrows(
        RoleNotFoundException.class,
        () -> {
          customUserDetailsService.createNewUser(request);
        });
  }

  @Test
  @DisplayName("Test create new user when auth provider not found")
  void testCreateNewUser_AuthProviderNotFound() {
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
    Role userRole = Role.builder().id(1L).name("ROLE_USER").build();
    when(roleRepository.findByName(UserRole.USER.getRoleName())).thenReturn(Optional.of(userRole));
    when(authProviderRepository.findByName("local")).thenReturn(Optional.empty());

    assertThrows(
        AuthProviderNotFoundException.class,
        () -> {
          customUserDetailsService.createNewUser(request);
        });
  }
}
