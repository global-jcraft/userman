package com.huddey.core.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.huddey.core.common.utils.LocaleUtils;
import com.huddey.core.notification.service.NotificationHandler;
import com.huddey.core.userman.data.dto.ResetPasswordCompleteRequest;
import com.huddey.core.userman.data.dto.ResetPasswordRequest;
import com.huddey.core.userman.data.dto.ResetPasswordResponse;
import com.huddey.core.userman.data.entity.AuthProvider;
import com.huddey.core.userman.data.entity.User;
import com.huddey.core.userman.data.entity.UserCredential;
import com.huddey.core.userman.exception.InvalidTokenException;
import com.huddey.core.userman.exception.UserNotFoundException;
import com.huddey.core.userman.repository.UserCredentialRepository;
import com.huddey.core.userman.repository.UserRepository;
import com.huddey.core.userman.service.AuthServiceImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private HttpServletRequest servletRequest;

  @Mock private HttpServletResponse servletResponse;

  @Mock private NotificationHandler notificationHandler;

  @Mock private UserCredentialRepository userCredentialRepository;

  @InjectMocks private AuthServiceImpl authService;

  @BeforeEach
  void setUp() {
    MessageSource messageSource = Mockito.mock(MessageSource.class);
    Mockito.lenient()
        .when(messageSource.getMessage(Mockito.anyString(), Mockito.any(), Mockito.any()))
        .thenReturn("dummy-message");
    LocaleUtils.instance = new LocaleUtils(messageSource);
  }

  @Test
  void resetPasswordRequest_WithValidLocalUser_ShouldSucceed() {
    // Arrange
    String email = "test@example.com";
    ResetPasswordRequest request = new ResetPasswordRequest();
    request.setEmail(email);

    User user = new User();
    user.setEmail(email);

    AuthProvider localProvider = new AuthProvider();
    localProvider.setName("local");

    UserCredential localCredential = new UserCredential();
    localCredential.setAuthProvider(localProvider);

    user.setCredentials(Set.of(localCredential));

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(userRepository.save(any(User.class))).thenReturn(user);

    // Act
    ResetPasswordResponse response =
        authService.resetPasswordRequest(request, servletRequest, servletResponse);

    // Assert
    assertNotNull(response);
    assertTrue(response.isSuccess());
    assertNotNull(response.getToken());

    verify(userRepository).findByEmail(email);
    verify(userRepository).save(user);

    assertNotNull(localCredential.getPasswordResetToken());
    assertNotNull(localCredential.getPasswordResetTokenExpiresAt());
  }

  @Test
  void resetPasswordRequest_WithNonExistentUser_ShouldThrowUserNotFoundException() {
    // Arrange
    ResetPasswordRequest request = new ResetPasswordRequest();
    request.setEmail("nonexistent@example.com");

    when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        UserNotFoundException.class,
        () -> authService.resetPasswordRequest(request, servletRequest, servletResponse));
  }

  @Test
  void resetPasswordRequest_WithOAuth2User_ShouldThrowUnsupportedOperationException() {
    // Arrange
    String email = "oauth2user@example.com";
    ResetPasswordRequest request = new ResetPasswordRequest();
    request.setEmail(email);

    User user = new User();
    user.setEmail(email);

    AuthProvider oauthProvider = new AuthProvider();
    oauthProvider.setName("google");

    UserCredential oauthCredential = new UserCredential();
    oauthCredential.setAuthProvider(oauthProvider);

    user.setCredentials(Set.of(oauthCredential));

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

    // Act & Assert
    assertThrows(
        UnsupportedOperationException.class,
        () -> authService.resetPasswordRequest(request, servletRequest, servletResponse));
  }

  @Test
  void resetPasswordComplete_WithValidToken_ShouldSucceed() {
    // Arrange
    String token = "valid-token";
    String newPassword = "newPassword123";
    String encodedPassword = "encodedPassword";

    ResetPasswordCompleteRequest request = new ResetPasswordCompleteRequest();
    request.setToken(token);
    request.setNewPassword(newPassword);

    User user = new User();
    user.setEmail("test@example.com");

    UserCredential credential = new UserCredential();
    credential.setPasswordResetToken(token);
    credential.setPasswordResetTokenExpiresAt(OffsetDateTime.now().plusMinutes(10));
    credential.setUser(user);

    user.setCredentials(Set.of(credential));

    when(userCredentialRepository.findByPasswordResetToken(token))
        .thenReturn(Optional.of(credential));
    when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
    when(userRepository.save(any(User.class))).thenReturn(user);

    // Act
    authService.resetPasswordComplete(request, servletRequest, servletResponse);

    // Assert
    verify(userRepository).save(user);
    verify(passwordEncoder).encode(newPassword);

    assertEquals(encodedPassword, credential.getPasswordHash());
    assertNull(credential.getPasswordResetToken());
    assertNull(credential.getPasswordResetTokenExpiresAt());
  }

  @Test
  void resetPasswordComplete_WithExpiredToken_ShouldThrowInvalidTokenException() {
    // Arrange
    String token = "expired-token";
    ResetPasswordCompleteRequest request = new ResetPasswordCompleteRequest();
    request.setToken(token);
    request.setNewPassword("newPassword123");

    User user = new User();
    UserCredential credential = new UserCredential();
    credential.setPasswordResetToken(token);
    credential.setPasswordResetTokenExpiresAt(OffsetDateTime.now().minusMinutes(10));
    credential.setUser(user);
    user.setCredentials(Set.of(credential));

    when(userCredentialRepository.findByPasswordResetToken(token))
        .thenReturn(Optional.of(credential));

    // Act & Assert
    assertThrows(
        InvalidTokenException.class,
        () -> authService.resetPasswordComplete(request, servletRequest, servletResponse));
  }

  @Test
  void resetPasswordComplete_WithInvalidToken_ShouldThrowInvalidTokenException() {
    // Arrange
    ResetPasswordCompleteRequest request = new ResetPasswordCompleteRequest();
    request.setToken("invalid-token");
    request.setNewPassword("newPassword123");

    when(userCredentialRepository.findByPasswordResetToken("invalid-token"))
        .thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(
        InvalidTokenException.class,
        () -> authService.resetPasswordComplete(request, servletRequest, servletResponse));
  }
}
