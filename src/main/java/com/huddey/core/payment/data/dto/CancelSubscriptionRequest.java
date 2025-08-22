package com.huddey.core.payment.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelSubscriptionRequest {
  boolean cancelAtPeriodEnd = true; // Default to end of period
  String cancellationReason;
  boolean provideFeedback = false;
}
