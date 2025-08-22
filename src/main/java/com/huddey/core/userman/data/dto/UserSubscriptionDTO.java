package com.huddey.core.userman.data.dto;

import java.time.OffsetDateTime;

import com.huddey.core.payment.data.enums.SubscriptionPlan;
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
  private SubscriptionPlan plan;
  private SubscriptionStatus status;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
  private OffsetDateTime currentPeriodStart;
  private OffsetDateTime currentPeriodEnd;
  private boolean cancelAtPeriodEnd;
}
