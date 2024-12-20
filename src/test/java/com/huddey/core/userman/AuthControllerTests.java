package com.huddey.core.userman;

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
import com.huddey.core.userman.configuration.JwtTokenProvider;
import com.huddey.core.userman.controller.AuthController;
import com.huddey.core.userman.data.dto.UserRegistrationRequest;
import com.huddey.core.userman.data.dto.response.UserRegistrationResponse;
import com.huddey.core.userman.data.dto.token.TokenData;
import com.huddey.core.userman.service.AuthService;
import com.huddey.core.userman.service.CustomUserDetailsService;

@WebMvcTest(AuthController.class)
@Import(TestConfig.class)
class AuthControllerTests {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private AuthService authService;
  @MockBean private JwtTokenProvider jwtTokenProvider;
  @MockBean private CustomUserDetailsService customUserDetailsService;

  @Test
  @WithMockUser
  void testRegister_Success() throws Exception {
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

    Mockito.when(authService.register(any(UserRegistrationRequest.class))).thenReturn(response);

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
}
