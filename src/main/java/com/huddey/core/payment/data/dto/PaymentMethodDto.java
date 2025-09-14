package com.huddey.core.payment.data.dto;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.huddey.core.payment.data.enums.PaymentMethodType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentMethodDto {
  private Long id;
  private PaymentMethodType type;
  private String lastFour;
  private String brand;
  private Integer expMonth;
  private Integer expYear;
  private boolean isDefault;
  private boolean isBackup;
  private boolean isExpired;
  private OffsetDateTime createdAt;
  private String clientSecret;
  private String setupIntentId;
  private String setupUrl;
  private String status;
}
