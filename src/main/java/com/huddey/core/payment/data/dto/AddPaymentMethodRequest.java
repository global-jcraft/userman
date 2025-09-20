package com.huddey.core.payment.data.dto;

import com.huddey.core.payment.data.enums.PaymentMethodType;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddPaymentMethodRequest {
  @NotNull(message = "Payment method type is required")
  private PaymentMethodType type;

  private boolean setAsDefault = false;
}
