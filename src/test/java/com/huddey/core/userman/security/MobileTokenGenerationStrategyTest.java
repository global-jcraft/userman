package com.huddey.core.userman.security;

import com.huddey.core.userman.data.entity.Role;
import com.huddey.core.userman.data.entity.User;
import com.huddey.core.userman.security.token.MobileTokenGenerationStrategy;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import com.huddey.core.userman.auth.JwtTokenProvider;
import com.huddey.core.userman.data.SecurityUser;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MobileTokenGenerationStrategyTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private MobileTokenGenerationStrategy tokenStrategy;

    private SecurityUser testUser;
    private static final String TEST_ACCESS_TOKEN = "test.access.token";
    private static final String TEST_REFRESH_TOKEN = "test.refresh.token";

    @BeforeEach
    void setUp() {
        Role role = Role.builder()
                .id(1L)
                .name("ROLE_USER")
                .build();
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .roles(new HashSet<>(List.of(role)))
                .build();
        testUser = new SecurityUser(user);
    }

    @Test
    void generateAndSetToken_WithoutRememberMe_ShouldSetCorrectHeaders() {
        // Arrange
        when(jwtTokenProvider.generateAccessToken(testUser, false))
                .thenReturn(TEST_ACCESS_TOKEN);
        when(jwtTokenProvider.generateRefreshToken(testUser, false))
                .thenReturn(TEST_REFRESH_TOKEN);

        // Act
        tokenStrategy.generateAndSetToken(response, testUser, false);

        // Assert
        verify(response).setHeader("Access-Token", TEST_ACCESS_TOKEN);
        verify(response).setHeader("Refresh-Token", TEST_REFRESH_TOKEN);
        assertEquals(TEST_ACCESS_TOKEN, tokenStrategy.getAccessToken());
        assertEquals(TEST_REFRESH_TOKEN, tokenStrategy.getRefreshToken());
    }

    @Test
    void generateAndSetToken_WithRememberMe_ShouldSetCorrectHeaders() {
        // Arrange
        when(jwtTokenProvider.generateAccessToken(testUser, true))
                .thenReturn(TEST_ACCESS_TOKEN);
        when(jwtTokenProvider.generateRefreshToken(testUser, true))
                .thenReturn(TEST_REFRESH_TOKEN);

        // Act
        tokenStrategy.generateAndSetToken(response, testUser, true);

        // Assert
        verify(response).setHeader("Access-Token", TEST_ACCESS_TOKEN);
        verify(response).setHeader("Refresh-Token", TEST_REFRESH_TOKEN);
        assertEquals(TEST_ACCESS_TOKEN, tokenStrategy.getAccessToken());
        assertEquals(TEST_REFRESH_TOKEN, tokenStrategy.getRefreshToken());
    }

    @Test
    void generateAndSetToken_WhenAccessTokenGenerationFails_ShouldPropagateException() {
        // Arrange
        when(jwtTokenProvider.generateAccessToken(testUser, false))
                .thenThrow(new RuntimeException("Access token generation failed"));

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> tokenStrategy.generateAndSetToken(response, testUser, false));
    }

    @Test
    void generateAndSetToken_WhenRefreshTokenGenerationFails_ShouldPropagateException() {
        // Arrange
        when(jwtTokenProvider.generateAccessToken(testUser, false))
                .thenReturn(TEST_ACCESS_TOKEN);
        when(jwtTokenProvider.generateRefreshToken(testUser, false))
                .thenThrow(new RuntimeException("Refresh token generation failed"));

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> tokenStrategy.generateAndSetToken(response, testUser, false));
    }

    @Test
    void generateAndSetToken_WithNullResponse_ShouldThrowException() {
        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> tokenStrategy.generateAndSetToken(null, testUser, false));
    }

    @Test
    void getAccessToken_BeforeGeneration_ShouldReturnNull() {
        // Act & Assert
        assertNull(tokenStrategy.getAccessToken());
    }

    @Test
    void getRefreshToken_BeforeGeneration_ShouldReturnNull() {
        // Act & Assert
        assertNull(tokenStrategy.getRefreshToken());
    }

    @Test
    void generateAndSetToken_ShouldSetHeadersInCorrectOrder() {
        // Arrange
        when(jwtTokenProvider.generateAccessToken(testUser, false))
                .thenReturn(TEST_ACCESS_TOKEN);
        when(jwtTokenProvider.generateRefreshToken(testUser, false))
                .thenReturn(TEST_REFRESH_TOKEN);

        // Act
        tokenStrategy.generateAndSetToken(response, testUser, false);

        // Assert
        InOrder inOrder = inOrder(response);
        inOrder.verify(response).setHeader("Access-Token", TEST_ACCESS_TOKEN);
        inOrder.verify(response).setHeader("Refresh-Token", TEST_REFRESH_TOKEN);
    }

    @Test
    void generateAndSetToken_ShouldNotModifyOtherHeaders() {
        // Arrange
        when(jwtTokenProvider.generateAccessToken(testUser, false))
                .thenReturn(TEST_ACCESS_TOKEN);
        when(jwtTokenProvider.generateRefreshToken(testUser, false))
                .thenReturn(TEST_REFRESH_TOKEN);

        // Act
        tokenStrategy.generateAndSetToken(response, testUser, false);

        // Assert
        verify(response, times(1)).setHeader("Access-Token", TEST_ACCESS_TOKEN);
        verify(response, times(1)).setHeader("Refresh-Token", TEST_REFRESH_TOKEN);
        verify(response, times(2)).setHeader(any(), any()); // Only these two headers should be set
    }
}
