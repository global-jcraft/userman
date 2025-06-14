package com.huddey.core.userman.data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPhoneNumberResponse {
  private String message;
  private boolean success;
  private String status; // e.g., "PHONE_VERIFIED", "ALREADY_VERIFIED", "INVALID_TOKEN"
}
