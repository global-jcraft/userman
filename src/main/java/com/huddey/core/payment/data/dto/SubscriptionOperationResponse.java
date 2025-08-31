package com.huddey.core.payment.data.dto;

import java.time.OffsetDateTime;

import com.huddey.core.payment.data.enums.SubscriptionPlan;
import com.huddey.core.payment.data.enums.SubscriptionStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionOperationResponse {

  private boolean success;
  private String message;
  private SubscriptionPlan currentPlan;
  private SubscriptionStatus status;
  private OffsetDateTime nextBillingDate;
  private Double amountCharged; // For plan changes
  private String operationType; // "update", "cancel", "create"
}
