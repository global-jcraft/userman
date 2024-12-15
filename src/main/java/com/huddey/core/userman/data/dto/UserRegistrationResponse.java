package com.huddey.core.userman.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationResponse {
  private Long userId;
  private String email;
  private String firstName;
  private String lastName;
  private String status;
  private String message;
}
