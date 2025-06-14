package com.huddey.core.userman.data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhoneNumberVerificationResponse {
  private String message;
  private String phoneNumber;
  // Potentially add a field here if you want to return the masked phone number
  // private String maskedPhoneNumber;
}
