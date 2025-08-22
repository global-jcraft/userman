package com.huddey.core.payment.data.enums;

import lombok.Getter;

@Getter
public enum SubscriptionPlan {
  FREE("free_starter_plan", "Free", 00.00, "month"),
  STARTER_MONTHLY("price_starter_monthly", "Starter Monthly", 15.00, "month"),
  STARTER_YEARLY("price_starter_yearly", "Starter Yearly", 153.00, "year"),
  PRO_MONTHLY("price_pro_monthly", "Pro Monthly", 39.00, "month"),
  PRO_YEARLY("price_pro_yearly", "Pro Yearly", 374.00, "year"),
  ELITE_MONTHLY("price_elite_monthly", "Elite Monthly", 99.00, "month"),
  ELITE_YEARLY("price_elite_yearly", "Elite Yearly", 891.00, "year"),
  TEAM_MONTHLY_10("price_team_monthly_10", "Team Monthly 10 seats", 249.00, "month"),
  TEAM_MONTHLY_20("price_team_monthly_20", "Team Monthly 20 seats", 429.00, "month"),
  TEAM_MONTHLY_30("price_team_monthly_30", "Team Monthly 30 seats", 599.00, "month"),
  TEAM_MONTHLY_50("price_team_monthly_50", "Team Monthly 50 seats", 899.00, "month"),
  TEAM_YEARLY_10("price_team_yearly_10", "Team Yearly 10 seats", 2539.00, "year"),
  TEAM_YEARLY_20("price_team_yearly_20", "Team Yearly 20 seats", 4375.00, "year"),
  TEAM_YEARLY_30("price_team_yearly_30", "Team Yearly 30 seats", 6109.00, "year"),
  TEAM_YEARLY_50("price_team_yearly_50", "Team Yearly 50 seats", 9169.00, "year"),

  TEAM_PRO_MONTHLY_10("price_team_pro_monthly_10", "Team Pro Monthly 10 seats", 425.00, "month"),
  TEAM_PRO_MONTHLY_20("price_team_pro_monthly_20", "Team Pro Monthly 20 seats", 645.00, "month"),
  TEAM_PRO_MONTHLY_30("price_team_pro_monthly_30", "Team Pro Monthly 30 seats", 845.00, "month"),
  TEAM_PRO_MONTHLY_50("price_team_pro_monthly_50", "Team Pro Monthly 50 seats", 1185.00, "month"),
  TEAM_PRO_YEARLY_10("price_team_pro_yearly_10", "Team Pro Yearly 10 seats", 4233.00, "year"),
  TEAM_PRO_YEARLY_20("price_team_pro_yearly_20", "Team Pro Yearly 20 seats", 6424.00, "year"),
  TEAM_PRO_YEARLY_30("price_team_pro_yearly_30", "Team Pro Yearly 30 seats", 8416.00, "year"),
  TEAM_PRO_YEARLY_50("price_team_pro_yearly_50", "Team Pro Yearly 50 seats", 11802.00, "year");

  private final String internalPriceId;
  private final String displayName;
  private final double price;
  private final String interval;

  SubscriptionPlan(String internalPriceId, String displayName, double price, String interval) {
    this.internalPriceId = internalPriceId;
    this.displayName = displayName;
    this.price = price;
    this.interval = interval;
  }

  public static SubscriptionPlan fromStripePriceId(String priceId) {
    for (SubscriptionPlan plan : values()) {
      if (plan.internalPriceId.equals(priceId)) {
        return plan;
      }
    }
    throw new IllegalArgumentException("Unknown price ID: " + priceId);
  }
}
