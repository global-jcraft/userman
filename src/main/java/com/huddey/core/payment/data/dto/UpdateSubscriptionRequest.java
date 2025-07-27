package com.huddey.core.payment.data.dto;

import com.huddey.core.payment.data.enums.SubscriptionPlan;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSubscriptionRequest {

  @NotNull(message = "New plan is required")
  private SubscriptionPlan newPlan;

  private boolean prorationBehavior = true; // Default: create prorations
  private String changeReason; // Optional reason for the change
}
