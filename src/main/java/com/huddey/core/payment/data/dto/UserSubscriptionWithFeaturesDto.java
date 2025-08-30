package com.huddey.core.payment.data.dto;

import java.time.OffsetDateTime;
import java.util.List;

import com.huddey.core.payment.data.enums.SubscriptionPlan;
import com.huddey.core.payment.data.enums.SubscriptionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSubscriptionWithFeaturesDto {
  private String stripeCustomerId;
  private String stripeSubscriptionId;
  private SubscriptionPlan plan;
  private SubscriptionStatus status;
  private boolean cancelAtPeriodEnd;
  private OffsetDateTime currentPeriodStart;
  private OffsetDateTime currentPeriodEnd;
  private List<ProductFeatureDto> features;
}
