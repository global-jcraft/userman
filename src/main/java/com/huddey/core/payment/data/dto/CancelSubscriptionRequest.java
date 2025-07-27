package com.huddey.core.payment.data.dto;

import lombok.Value;

@Value
public class CancelSubscriptionRequest {
  boolean cancelAtPeriodEnd = true; // Default to end of period
  String cancellationReason;
  boolean provideFeedback = false;
}
