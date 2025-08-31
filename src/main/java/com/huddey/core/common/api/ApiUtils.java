package com.huddey.core.common.api;

import java.time.OffsetDateTime;

import com.huddey.core.userman.auth.JwtTokenProvider;
import com.huddey.core.userman.data.dto.token.TokenData;
import com.huddey.core.userman.token.TokenGenerationStrategy;

public class ApiUtils {

  private ApiUtils() {}

  public static ApiResponse buildApiResponse(
      boolean success, String successMessage, Object data, String errorMessage) {
    return ApiResponse.builder()
        .success(success)
        .message(successMessage)
        .error(errorMessage)
        .data(data)
        .timestamp(OffsetDateTime.now())
        .build();
  }

  public static TokenData buildTokenResponse(
      TokenGenerationStrategy tokenGenerationStrategy, JwtTokenProvider jwtTokenProvider) {
    return TokenData.builder()
        .accessToken(tokenGenerationStrategy.getAccessToken())
        .refreshToken(tokenGenerationStrategy.getRefreshToken())
        .expiresIn(jwtTokenProvider.getAccessTokenValidity())
        .tokenType("Bearer")
        .build();
  }
}
