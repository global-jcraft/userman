package com.huddey.core.userman.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshResponse {
  private String accessToken;
  private String refreshToken;
  private String tokenType;
  private long expiresIn;
}
