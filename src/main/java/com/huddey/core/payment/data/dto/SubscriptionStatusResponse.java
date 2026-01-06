package com.huddey.core.payment.data.dto;

import java.time.OffsetDateTime;

import com.huddey.core.payment.data.entity.UserSubscription;
import com.huddey.core.payment.data.enums.SubscriptionStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionStatusResponse {

  private String planKey;
  private String billingInterval;
  private String currency;
  private Long seatCount;
  private boolean hasActiveSubscription;
  private SubscriptionStatus status;
  private OffsetDateTime currentPeriodStart;
  private OffsetDateTime currentPeriodEnd;
  private boolean cancelAtPeriodEnd;
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
    response.status = subscription.getStatus();
    response.currentPeriodStart = subscription.getCurrentPeriodStart();
    response.currentPeriodEnd = subscription.getCurrentPeriodEnd();
    response.cancelAtPeriodEnd = subscription.isCancelAtPeriodEnd();
    response.planKey = subscription.getPlanKey();
    response.billingInterval = subscription.getBillingInterval();
    response.currency = subscription.getCurrency();
    response.seatCount = subscription.getSeatCount();
    response.pendingPlanEffectiveDate = subscription.getPendingPlanEffectiveDate();
    response.stripeCustomerId = subscription.getStripeCustomerId();
    response.createdAt = subscription.getCreatedAt();
    response.updatedAt = subscription.getUpdatedAt();

    return response;
  }

  public static SubscriptionStatusResponse noSubscription() {
    SubscriptionStatusResponse response = new SubscriptionStatusResponse();
    response.hasActiveSubscription = false;
    return response;
  }
}
