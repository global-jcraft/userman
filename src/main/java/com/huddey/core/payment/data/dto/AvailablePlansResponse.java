package com.huddey.core.payment.data.dto;

import java.util.Map;

import com.huddey.core.payment.data.enums.SubscriptionPlan;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailablePlansResponse {
  private Map<String, PlanDetails> plans;

  @Data
  @AllArgsConstructor
  public static class PlanDetails {
    private String name;
    private String displayName;
    private Double price;
    private String interval;
    private String description;
    private String[] features;
    private boolean popular = false;
    private Double yearlyDiscount; // Percentage saved with yearly billing

    // Constructors
    public PlanDetails() {}

    public PlanDetails(SubscriptionPlan plan) {
      this.name = plan.name();
      this.displayName = plan.getDisplayName();
      this.price = plan.getPrice();
      this.interval = plan.getInterval();

      // Set descriptions and features based on plan
      setDescriptionAndFeatures(plan);

      // Calculate yearly discount
      if (plan.name().contains("YEARLY")) {
        calculateYearlyDiscount(plan);
      }
    }

    private void setDescriptionAndFeatures(SubscriptionPlan plan) {
      if (plan.name().startsWith("STARTER")) {
        this.description = "Perfect for creators who are just starting out";
        this.features =
            new String[] {
              "Connect up to 3 social networks",
              "Basic Analytics",
              "Creative Academy Access (group consultations)",
              "Content scheduling feature"
            };
      } else if (plan.name().startsWith("PRO")) {
        this.description = "For growing creators seeking advanced insights";
        this.features =
            new String[] {
              "Connect up to 7 social networks",
              "Advanced Analytics",
              "Creative Academy Access (group + 1-on-1)",
              "Trend Pulse with industry insights",
              "Priority support"
            };
        this.popular = true; // Mark Pro as popular
      } else if (plan.name().startsWith("ELITE")) {
        this.description = "For established creators and businesses";
        this.features =
            new String[] {
              "Unlimited social networks",
              "Advanced + Predictive Analytics",
              "Full Trend Pulse with real-time updates",
              "Exclusive Creative Academy Access",
              "Team collaboration features",
              "VIP Support"
            };
      }
    }

    private void calculateYearlyDiscount(SubscriptionPlan yearlyPlan) {
      try {
        String monthlyPlanName = yearlyPlan.name().replace("YEARLY", "MONTHLY");
        SubscriptionPlan monthlyPlan = SubscriptionPlan.valueOf(monthlyPlanName);

        double yearlyPrice = yearlyPlan.getPrice();
        double monthlyPrice = monthlyPlan.getPrice() * 12;

        this.yearlyDiscount = ((monthlyPrice - yearlyPrice) / monthlyPrice) * 100;
      } catch (Exception e) {
        this.yearlyDiscount = 17.0; // Default discount
      }
    }
  }
}
