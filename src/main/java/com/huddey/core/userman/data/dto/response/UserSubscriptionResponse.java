package com.huddey.core.userman.data.dto.response;

import java.time.OffsetDateTime;

import com.huddey.core.payment.data.enums.SubscriptionPlan;
import com.huddey.core.payment.data.enums.SubscriptionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserSubscriptionResponse {
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
