package com.huddey.core.userman.data.dto;

import java.time.OffsetDateTime;

import com.huddey.core.payment.data.dto.ProductPriceDto;
import com.huddey.core.payment.data.enums.SubscriptionStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSubscriptionDTO {
  private Long id;
  private Long userId;
  private String stripeCustomerId;
  private String stripeSubscriptionId;
  private String planKey;
  private String billingInterval;
  private String currency;
  private Long seatCount;
  private ProductPriceDto productPrice;
  private SubscriptionStatus status;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
  private OffsetDateTime currentPeriodStart;
  private OffsetDateTime currentPeriodEnd;
  private boolean cancelAtPeriodEnd;
}
