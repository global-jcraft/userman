package com.huddey.core.payment.data.dto;

import com.huddey.core.payment.data.enums.PaymentMethodType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePaymentMethodCheckoutRequest {
  @NotNull(message = "Payment method type is required")
  private PaymentMethodType paymentMethodType;

  private boolean setAsDefault = false;

  @NotBlank(message = "Success URL is required")
  @Pattern(regexp = "^https?://.*", message = "Success URL must be a valid HTTP/HTTPS URL")
  private String successUrl;

  @NotBlank(message = "Cancel URL is required")
  @Pattern(regexp = "^https?://.*", message = "Cancel URL must be a valid HTTP/HTTPS URL")
  private String cancelUrl;
}
