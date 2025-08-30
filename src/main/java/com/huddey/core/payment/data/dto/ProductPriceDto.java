package com.huddey.core.payment.data.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPriceDto {
  private String stripePriceId;
  private BigDecimal unitAmount;
  private String currency;
  private String recurringInterval;
  private Boolean active;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
}
