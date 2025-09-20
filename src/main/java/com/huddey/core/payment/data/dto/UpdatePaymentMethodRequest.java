package com.huddey.core.payment.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePaymentMethodRequest {
  private Integer expMonth;
  private Integer expYear;
}
