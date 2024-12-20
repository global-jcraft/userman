package com.huddey.core.userman.data.dto.token;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenData {
  private String accessToken;
  private String refreshToken;
  @Builder.Default private String tokenType = "Bearer";
  private long expiresIn;
}
