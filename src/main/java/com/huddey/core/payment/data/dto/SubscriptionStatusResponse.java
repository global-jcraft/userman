package com.huddey.core.payment.data.dto;

import java.time.OffsetDateTime;

import com.huddey.core.payment.data.entity.UserSubscription;
import com.huddey.core.payment.data.enums.SubscriptionPlan;
import com.huddey.core.payment.data.enums.SubscriptionStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionStatusResponse {

  private boolean hasActiveSubscription;
  private SubscriptionPlan currentPlan;
  private SubscriptionStatus status;
  private OffsetDateTime currentPeriodStart;
  private OffsetDateTime currentPeriodEnd;
  private boolean cancelAtPeriodEnd;
  private SubscriptionPlan pendingPlan;
  private OffsetDateTime pendingPlanEffectiveDate;
  private Double nextChargeAmount;
  private String stripeCustomerId;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;

  // Static factory method from UserSubscription entity
  public static SubscriptionStatusResponse fromUserSubscription(UserSubscription subscription) {
    if (subscription == null) {
      return noSubscription();
    }

    SubscriptionStatusResponse response = new SubscriptionStatusResponse();
    response.hasActiveSubscription = subscription.getStatus() == SubscriptionStatus.ACTIVE;
    response.currentPlan = subscription.getPlan();
    response.status = subscription.getStatus();
    response.currentPeriodStart = subscription.getCurrentPeriodStart();
    response.currentPeriodEnd = subscription.getCurrentPeriodEnd();
    response.cancelAtPeriodEnd = subscription.isCancelAtPeriodEnd();
    response.pendingPlan = subscription.getPendingPlan();
    response.pendingPlanEffectiveDate = subscription.getPendingPlanEffectiveDate();
    response.stripeCustomerId = subscription.getStripeCustomerId();
    response.createdAt = subscription.getCreatedAt();
    response.updatedAt = subscription.getUpdatedAt();

    // Calculate next charge amount
    if (subscription.getPlan() != null) {
      response.nextChargeAmount = subscription.getPlan().getPrice();
    }

    return response;
  }

  public static SubscriptionStatusResponse noSubscription() {
    SubscriptionStatusResponse response = new SubscriptionStatusResponse();
    response.hasActiveSubscription = false;
    return response;
  }
}
