package com.huddey.core.userman.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.huddey.core.userman.config.TestConfig;
import com.huddey.core.userman.data.SecurityUser;
import com.huddey.core.userman.data.dto.LoginRequest;
import com.huddey.core.userman.data.dto.UserRegistrationRequest;
import com.huddey.core.userman.data.dto.response.UserRegistrationResponse;
import com.huddey.core.userman.data.entity.User;
import com.huddey.core.userman.data.entity.UserStatus;
import com.huddey.core.userman.exception.RoleNotFoundException;
import com.huddey.core.userman.exception.UserAlreadyExistsException;
import com.huddey.core.userman.repository.AuthProviderRepository;
import com.huddey.core.userman.repository.RoleRepository;
import com.huddey.core.userman.repository.UserRepository;
import com.huddey.core.userman.service.AuthServiceImpl;
import com.huddey.core.userman.service.CustomUserDetailsService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
@Import(TestConfig.class)
class AuthServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private AuthProviderRepository authProviderRepository;
  @Mock private RoleRepository roleRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtTokenProvider jwtTokenProvider;
  @Mock private AuthenticationManager authenticationManager;
  @Mock private UserDetailsService userDetailsService;
  @Mock private CustomUserDetailsService customUserDetailsService;
  @Mock private HttpServletRequest servletRequest;
  @Mock private HttpServletResponse servletResponse;

  @InjectMocks private AuthServiceImpl authService;
  private Authentication authentication;
  private User testUser;
  private LoginRequest loginRequest;

  private UserRegistrationRequest registrationRequest;
  private SecurityUser securityUser;
  private User user;

  @BeforeEach
  void setUp() {
    registrationRequest =
        UserRegistrationRequest.builder()
            .email("test@example.com")
            .password("Password123!")
            .firstName("John")
            .lastName("Doe")
            .companyName("Test Company")
            .phoneNumber("+1234567890")
            .build();

    user =
        User.builder()
            .id(1L)
            .email(registrationRequest.getEmail())
            .firstName(registrationRequest.getFirstName())
            .lastName(registrationRequest.getLastName())
            .status(UserStatus.PENDING)
            .roles(new HashSet<>())
            .build();

    securityUser = new SecurityUser(user);
    authentication = new UsernamePasswordAuthenticationToken(securityUser, null);

    // Initialize loginRequest and testUser
    loginRequest =
        LoginRequest.builder().email("test@example.com").password("Password123!").build();

    testUser =
        User.builder()
            .id(1L)
            .email("test@example.com")
            .firstName("John")
            .lastName("Doe")
            .status(UserStatus.ACTIVE)
            .roles(new HashSet<>())
            .build();
  }

  @Nested
  @DisplayName("Register Tests")
  class RegisterTests {

    @Test
    @DisplayName("Should successfully register web user")
    void register_WebUser_Success()
        throws RoleNotFoundException,
            UserAlreadyExistsException,
            javax.management.relation.RoleNotFoundException {
      // Arrange
      when(servletRequest.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
      when(customUserDetailsService.createNewUser(registrationRequest)).thenReturn(securityUser);

      // Act
      UserRegistrationResponse response =
          authService.register(registrationRequest, servletRequest, servletResponse);

      // Assert
      assertNotNull(response);
      assertEquals(user.getId(), response.getUserId());
      assertEquals(user.getEmail(), response.getEmail());
      assertEquals(user.getFirstName(), response.getFirstName());
      assertEquals(user.getLastName(), response.getLastName());
      assertEquals(user.getStatus().toString(), response.getStatus());
      assertNull(response.getTokenData());
      verify(customUserDetailsService).createNewUser(registrationRequest);
    }

    @Test
    @DisplayName("Should successfully register mobile user")
    void register_MobileUser_Success()
        throws RoleNotFoundException,
            UserAlreadyExistsException,
            javax.management.relation.RoleNotFoundException {
      // Arrange
      when(servletRequest.getHeader("User-Agent")).thenReturn("Android");
      when(customUserDetailsService.createNewUser(registrationRequest)).thenReturn(securityUser);
      when(jwtTokenProvider.getAccessTokenValidity()).thenReturn(3600L);
      when(jwtTokenProvider.generateAccessToken(any(SecurityUser.class)))
          .thenReturn("access-token");
      when(jwtTokenProvider.generateRefreshToken(any(SecurityUser.class)))
          .thenReturn("refresh-token");

      // Act
      UserRegistrationResponse response =
          authService.register(registrationRequest, servletRequest, servletResponse);

      // Assert
      assertNotNull(response);
      assertEquals(user.getId(), response.getUserId());
      assertEquals(user.getEmail(), response.getEmail());
      assertEquals(user.getFirstName(), response.getFirstName());
      assertEquals(user.getLastName(), response.getLastName());
      assertEquals(user.getStatus().toString(), response.getStatus());
      assertNotNull(response.getTokenData());
      assertEquals("access-token", response.getTokenData().getAccessToken());
      assertEquals("refresh-token", response.getTokenData().getRefreshToken());
      assertEquals(3600, response.getTokenData().getExpiresIn());
      verify(customUserDetailsService).createNewUser(registrationRequest);
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when user exists")
    void register_UserAlreadyExists() throws javax.management.relation.RoleNotFoundException {
      // Arrange
      when(customUserDetailsService.createNewUser(registrationRequest))
          .thenThrow(new UserAlreadyExistsException("User already exists"));

      // Act & Assert
      assertThrows(
          UserAlreadyExistsException.class,
          () -> authService.register(registrationRequest, servletRequest, servletResponse));
      verify(customUserDetailsService).createNewUser(registrationRequest);
    }

    @Test
    @DisplayName("Should throw RoleNotFoundException when role not found")
    void register_RoleNotFound() throws javax.management.relation.RoleNotFoundException {
      // Arrange
      when(customUserDetailsService.createNewUser(registrationRequest))
          .thenThrow(new RoleNotFoundException("Role not found"));

      // Act & Assert
      assertThrows(
          RoleNotFoundException.class,
          () -> authService.register(registrationRequest, servletRequest, servletResponse));
      verify(customUserDetailsService).createNewUser(registrationRequest);
    }
  }
}
