package com.huddey.core.payment.data.enums;

import lombok.Getter;

@Getter
public enum SubscriptionPlan {
  FREE("free_starter_plan", "Free", 00.00, "month"),
  STARTER_MONTHLY("price_starter_monthly", "Starter Monthly", 15.00, "month"),
  STARTER_YEARLY("price_starter_yearly", "Starter Yearly", 150.00, "year"),
  PRO_MONTHLY("price_pro_monthly", "Pro Monthly", 35.00, "month"),
  PRO_YEARLY("price_pro_yearly", "Pro Yearly", 350.00, "year"),
  ELITE_MONTHLY("price_elite_monthly", "Elite Monthly", 85.00, "month"),
  ELITE_YEARLY("price_elite_yearly", "Elite Yearly", 850.00, "year");

  private final String stripePriceId;
  private final String displayName;
  private final double price;
  private final String interval;

  SubscriptionPlan(String stripePriceId, String displayName, double price, String interval) {
    this.stripePriceId = stripePriceId;
    this.displayName = displayName;
    this.price = price;
    this.interval = interval;
  }

  public static SubscriptionPlan fromStripePriceId(String priceId) {
    for (SubscriptionPlan plan : values()) {
      if (plan.stripePriceId.equals(priceId)) {
        return plan;
      }
    }
    throw new IllegalArgumentException("Unknown price ID: " + priceId);
  }
}
