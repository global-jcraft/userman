package com.huddey.core.payment.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCheckoutSessionResponse {
  private String checkoutUrl;
  private String sessionId;
  private boolean success;
  private String message;

  public static CreateCheckoutSessionResponse success(String checkoutUrl, String sessionId) {
    CreateCheckoutSessionResponse response = new CreateCheckoutSessionResponse();
    response.checkoutUrl = checkoutUrl;
    response.sessionId = sessionId;
    response.success = true;
    response.message = "Checkout session created successfully";
    return response;
  }

  public static CreateCheckoutSessionResponse error(String message) {
    CreateCheckoutSessionResponse response = new CreateCheckoutSessionResponse();
    response.success = false;
    response.message = message;
    return response;
  }
}
