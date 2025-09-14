package com.huddey.core.payment.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SetupIntentResponse {
  private String clientSecret;
  private String setupIntentId;
  private String setupUrl;
  private String status;

  public SetupIntentResponse(String clientSecret, String setupIntentId) {
    this.clientSecret = clientSecret;
    this.setupIntentId = setupIntentId;
    this.setupUrl = "https://js.stripe.com/v3/";
    this.status = "requires_payment_method";
  }
}
