package com.huddey.core.payment.data.dto;

import com.huddey.core.payment.data.enums.SubscriptionPlan;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCheckoutSessionRequest {

  @NotNull(message = "Plan is required")
  private SubscriptionPlan plan;

  @NotNull(message = "Price ID is required")
  private String priceId;

  @NotBlank(message = "Success URL is required")
  private String successUrl;

  @NotBlank(message = "Cancel URL is required")
  private String cancelUrl;

  // Optional: Override user email for checkout
  @Email(message = "Invalid email format")
  private String customerEmail;
}
