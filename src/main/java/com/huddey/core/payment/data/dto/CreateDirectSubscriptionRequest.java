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
public class CreateDirectSubscriptionRequest {

  @NotNull(message = "Plan is required")
  private SubscriptionPlan plan;

  @NotNull(message = "Price ID is required")
  private String priceId;

  @NotBlank(message = "Payment method ID is required")
  private String paymentMethodId;

  @Email(message = "Valid email is required")
  private String customerEmail;

  private boolean confirmPayment = true; // Auto-confirm payment
  private Integer trialDays = 0; // Trial period in days
}
