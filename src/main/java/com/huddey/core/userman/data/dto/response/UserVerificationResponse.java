package com.huddey.core.userman.data.dto.response;

import java.util.Set;

import com.huddey.core.userman.data.dto.token.TokenData;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVerificationResponse {
  private Long userId;
  private String email;
  private String firstName;
  private String lastName;
  private String status;
  private String message;
  private Set<String> role;
  private TokenData tokenData;
}
