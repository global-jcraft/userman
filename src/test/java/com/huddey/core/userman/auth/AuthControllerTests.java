package com.huddey.core.userman.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huddey.core.userman.config.TestConfig;
import com.huddey.core.userman.controller.AuthController;
import com.huddey.core.userman.data.dto.LoginRequest;
import com.huddey.core.userman.data.dto.TokenRefreshRequest;
import com.huddey.core.userman.data.dto.UserDTO;
import com.huddey.core.userman.data.dto.UserRegistrationRequest;
import com.huddey.core.userman.data.dto.response.LoginResponse;
import com.huddey.core.userman.data.dto.response.UserRegistrationResponse;
import com.huddey.core.userman.data.dto.token.TokenData;
import com.huddey.core.userman.data.dto.token.TokenRefreshResponse;
import com.huddey.core.userman.exception.AuthenticationException;
import com.huddey.core.userman.service.AuthServiceImpl;
import com.huddey.core.userman.service.CustomUserDetailsService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebMvcTest(AuthController.class)
@Import(TestConfig.class)
class AuthControllerTests {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private AuthServiceImpl authService;
  @MockBean private JwtTokenProvider jwtTokenProvider;
  @MockBean private CustomUserDetailsService customUserDetailsService;

  @Test
  @WithMockUser
  void test_register_success() throws Exception {
    // Given: Create a sample UserRegistrationRequest
    UserRegistrationRequest request =
        UserRegistrationRequest.builder()
            .email("john.doe@example.com")
            .password("Password@123")
            .firstName("John")
            .lastName("Doe")
            .companyName("Example Corp")
            .phoneNumber("+12345678901")
            .build();

    TokenData tokenData =
        TokenData.builder().accessToken("access-token").refreshToken("refresh-token").build();

    UserRegistrationResponse response =
        UserRegistrationResponse.builder()
            .userId(1L)
            .email(request.getEmail())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .status("ACTIVE")
            .message("User registered successfully")
            .tokenData(tokenData)
            .build();

    Mockito.when(
            authService.register(
                any(UserRegistrationRequest.class),
                any(HttpServletRequest.class),
                any(HttpServletResponse.class)))
        .thenReturn(response);

    ResultActions result =
        mockMvc.perform(
            post("/api/v1/auth/register")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    result
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.userId").value(response.getUserId()))
        .andExpect(jsonPath("$.email").value(response.getEmail()))
        .andExpect(jsonPath("$.firstName").value(response.getFirstName()))
        .andExpect(jsonPath("$.lastName").value(response.getLastName()))
        .andExpect(jsonPath("$.status").value(response.getStatus()))
        .andExpect(jsonPath("$.message").value(response.getMessage()))
        .andExpect(jsonPath("$.tokenData.accessToken").value(tokenData.getAccessToken()))
        .andExpect(jsonPath("$.tokenData.refreshToken").value(tokenData.getRefreshToken()));
  }

  @Test
  @WithMockUser
  void register_invalid_email() throws Exception {
    UserRegistrationRequest request =
        UserRegistrationRequest.builder()
            .email("invalid-email")
            .password("Password@123")
            .firstName("John")
            .lastName("Doe")
            .companyName("Example Corp")
            .phoneNumber("+12345678901")
            .build();

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void register_missing_required_fields() throws Exception {
    UserRegistrationRequest request =
        UserRegistrationRequest.builder()
            .email("john.doe@example.com")
            .password("Password@123")
            .build();

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void register_weak_password() throws Exception {
    UserRegistrationRequest request =
        UserRegistrationRequest.builder()
            .email("john.doe@example.com")
            .password("123")
            .firstName("John")
            .lastName("Doe")
            .companyName("Example Corp")
            .phoneNumber("+12345678901")
            .build();

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void login_success() throws Exception {
    LoginRequest request =
        LoginRequest.builder().email("john.doe@example.com").password("Password@123").build();

    LoginResponse response =
        LoginResponse.builder()
            .accessToken("access-token")
            .refreshToken("refresh-token")
            .tokenType("Bearer")
            .expiresIn(3600)
            .user(
                UserDTO.builder()
                    .id(1L)
                    .email(request.getEmail())
                    .firstName("John")
                    .lastName("Doe")
                    .build())
            .build();

    Mockito.when(
            authService.login(
                any(LoginRequest.class),
                any(HttpServletRequest.class),
                any(HttpServletResponse.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.accessToken").value(response.getAccessToken()))
        .andExpect(jsonPath("$.refreshToken").value(response.getRefreshToken()))
        .andExpect(jsonPath("$.tokenType").value(response.getTokenType()))
        .andExpect(jsonPath("$.expiresIn").value(response.getExpiresIn()))
        .andExpect(jsonPath("$.user.id").value(response.getUser().getId()))
        .andExpect(jsonPath("$.user.email").value(response.getUser().getEmail()))
        .andExpect(jsonPath("$.user.firstName").value(response.getUser().getFirstName()))
        .andExpect(jsonPath("$.user.lastName").value(response.getUser().getLastName()));
  }

  @Test
  @WithMockUser
  void login_invalid_credentials() throws Exception {
    LoginRequest request =
        LoginRequest.builder().email("john.doe@example.com").password("wrong-password").build();

    Mockito.when(
            authService.login(
                any(LoginRequest.class),
                any(HttpServletRequest.class),
                any(HttpServletResponse.class)))
        .thenThrow(new AuthenticationException("Invalid credentials"));

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void login_missing_required_fields() throws Exception {
    LoginRequest request = LoginRequest.builder().email("john.doe@example.com").build();

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void refresh_token_success() throws Exception {
    TokenRefreshRequest request =
        TokenRefreshRequest.builder().refreshToken("valid-refresh-token").build();

    TokenRefreshResponse response =
        TokenRefreshResponse.builder()
            .accessToken("new-access-token")
            .refreshToken("new-refresh-token")
            .tokenType("Bearer")
            .expiresIn(3600)
            .build();

    Mockito.when(
            authService.refreshToken(
                any(TokenRefreshRequest.class),
                any(HttpServletRequest.class),
                any(HttpServletResponse.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            post("/api/v1/auth/refresh-token")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.accessToken").value(response.getAccessToken()))
        .andExpect(jsonPath("$.refreshToken").value(response.getRefreshToken()))
        .andExpect(jsonPath("$.tokenType").value(response.getTokenType()))
        .andExpect(jsonPath("$.expiresIn").value(response.getExpiresIn()));
  }

  @Test
  @WithMockUser
  void refresh_token_invalid_token() throws Exception {
    TokenRefreshRequest request =
        TokenRefreshRequest.builder().refreshToken("invalid-refresh-token").build();

    Mockito.when(
            authService.refreshToken(
                any(TokenRefreshRequest.class),
                any(HttpServletRequest.class),
                any(HttpServletResponse.class)))
        .thenThrow(new AuthenticationException("Invalid refresh token"));

    mockMvc
        .perform(
            post("/api/v1/auth/refresh-token")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void refresh_token_missing_token() throws Exception {
    TokenRefreshRequest request = TokenRefreshRequest.builder().build();

    mockMvc
        .perform(
            post("/api/v1/auth/refresh-token")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}
