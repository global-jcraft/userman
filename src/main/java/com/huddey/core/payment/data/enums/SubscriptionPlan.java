/*
package com.huddey.core.payment.data.enums;


@Getter
public enum SubscriptionPlan {
  FREE_MONTHLY("price_1SDtutCsPWgUOsg2TEJg6VVE", "Free", 00.00, "month"),
  STARTER_MONTHLY("price_1Ryw3LCsPWgUOsg20ShZ4uLn", "Starter Monthly", 15.00, "month"),
  STARTER_YEARLY("price_1Ryw3MCsPWgUOsg21bcOiCkR", "Starter Yearly", 153.00, "year"),
  PRO_MONTHLY("price_1Ryw3MCsPWgUOsg2PuITEfyW", "Pro Monthly", 39.00, "month"),
  PRO_YEARLY("price_1Ryw3NCsPWgUOsg214wC4gpo", "Pro Yearly", 374.00, "year"),
  ELITE_MONTHLY("price_1Ryw3NCsPWgUOsg2lgsmS3p1", "Elite Monthly", 99.00, "month"),
  ELITE_YEARLY("price_1Ryw3OCsPWgUOsg2UXvFZdJS", "Elite Yearly", 891.00, "year"),
  TEAM_MONTHLY_10("price_1Ryw3OCsPWgUOsg2TESdXzdA", "Team Monthly 10 seats", 249.00, "month"),
  TEAM_MONTHLY_20("price_1Ryw3PCsPWgUOsg2NlBBcYfp", "Team Monthly 20 seats", 429.00, "month"),
  TEAM_MONTHLY_30("price_1Ryw3QCsPWgUOsg2QKEghbhv", "Team Monthly 30 seats", 599.00, "month"),
  TEAM_MONTHLY_50("price_1Ryw3RCsPWgUOsg2TZ2ZN0wg", "Team Monthly 50 seats", 899.00, "month"),
  TEAM_YEARLY_10("price_1Ryw3PCsPWgUOsg27rbRJzOr", "Team Yearly 10 seats", 2539.00, "year"),
  TEAM_YEARLY_20("price_1Ryw3QCsPWgUOsg2OObTQ13i", "Team Yearly 20 seats", 4375.00, "year"),
  TEAM_YEARLY_30("price_1Ryw3RCsPWgUOsg2uYDc6YRD", "Team Yearly 30 seats", 6109.00, "year"),
  TEAM_YEARLY_50("price_1Ryw3SCsPWgUOsg2FwapBiQI", "Team Yearly 50 seats", 9169.00, "year"),

  TEAM_PRO_MONTHLY_10("price_1Ryw3SCsPWgUOsg25mrojs1g", "Team Pro Monthly 10 seats", 425.00, "month"),
  TEAM_PRO_MONTHLY_20("price_1Ryw3TCsPWgUOsg2haaW4Gfs", "Team Pro Monthly 20 seats", 645.00, "month"),
  TEAM_PRO_MONTHLY_30("price_1Ryw3UCsPWgUOsg2pCXs99GV", "Team Pro Monthly 30 seats", 845.00, "month"),
  TEAM_PRO_MONTHLY_50("price_1Ryw3VCsPWgUOsg2WGKsNinm", "Team Pro Monthly 50 seats", 1185.00, "month"),
  TEAM_PRO_YEARLY_10("price_1Ryw3SCsPWgUOsg2lE3tsuIq", "Team Pro Yearly 10 seats", 4233.00, "year"),
  TEAM_PRO_YEARLY_20("price_1Ryw3TCsPWgUOsg2jwKWDPR9", "Team Pro Yearly 20 seats", 6424.00, "year"),
  TEAM_PRO_YEARLY_30("price_1Ryw3UCsPWgUOsg2Gn8ZqToD", "Team Pro Yearly 30 seats", 8416.00, "year"),
  TEAM_PRO_YEARLY_50("price_1Ryw3VCsPWgUOsg2efo5hQZ9", "Team Pro Yearly 50 seats", 11802.00, "year");

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
*/
