package com.huddey.core.userman.data.dto.response;

import com.huddey.core.userman.data.dto.UserDTO;
import com.huddey.core.userman.data.dto.token.TokenData;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LoginResponse {
  private String accessToken;
  private String refreshToken;
  @Builder.Default private String tokenType = "Bearer";
  private long expiresIn;
  private UserDTO user;
  private TokenData tokenData;
}
