package com.huddey.core.payment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.stripe.Stripe;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "stripe")
public class StripeConfig {

  @Value("${stripe.api.key}")
  private String stripeApiKey;

  @Value("${stripe.publishable.key}")
  private String publishableKey;

  @Value("${stripe.webhook.secret}")
  private String webhookSecret;

  @Value("${stripe.products.free_plan}")
  private String freePlanProductId;

  @Value("${stripe.products.starter_plan}")
  private String starterPlanProductId;

  @Value("${stripe.products.pro_plan}")
  private String proPlanProductId;

  @Value("${stripe.products.elite_plan}")
  private String elitePlanProductId;

  @Value("${stripe.products.team_plan_10}")
  private String teamPlan10ProductId;

  @Value("${stripe.products.team_plan_20}")
  private String teamPlan20ProductId;

  @Value("${stripe.products.team_plan_30}")
  private String teamPlan30ProductId;

  @Value("${stripe.products.team_plan_50}")
  private String teamPlan50ProductId;

  @Value("${stripe.products.team_pro_plan_10}")
  private String teamProPlan10ProductId;

  @Value("${stripe.products.team_pro_plan_20}")
  private String teamProPlan20ProductId;

  @Value("${stripe.products.team_pro_plan_30}")
  private String teamProPlan30ProductId;

  @Value("${stripe.products.team_pro_plan_50}")
  private String teamProPlan50ProductId;

  @Value("${stripe.products.free_plan_price.monthly}")
  private String freePlanMonthlyPriceId;

  @Value("${stripe.products.starter_plan_price.monthly}")
  private String starterPlanMonthlyPriceId;

  @Value("${stripe.products.starter_plan_price.yearly}")
  private String starterPlanYearlyPriceId;

  @Value("${stripe.products.pro_plan_price.monthly}")
  private String proPlanMonthlyPriceId;

  @Value("${stripe.products.pro_plan_price.yearly}")
  private String proPlanYearlyPriceId;

  @Value("${stripe.products.elite_plan_price.monthly}")
  private String elitePlanMonthlyPriceId;

  @Value("${stripe.products.elite_plan_price.yearly}")
  private String elitePlanYearlyPriceId;

  @Value("${stripe.products.team_plan_10_price.monthly}")
  private String teamPlan10MonthlyPriceId;

  @Value("${stripe.products.team_plan_10_price.yearly}")
  private String teamPlan10YearlyPriceId;

  @Value("${stripe.products.team_plan_20_price.monthly}")
  private String teamPlan20MonthlyPriceId;

  @Value("${stripe.products.team_plan_20_price.yearly}")
  private String teamPlan20YearlyPriceId;

  @Value("${stripe.products.team_plan_30_price.monthly}")
  private String teamPlan30MonthlyPriceId;

  @Value("${stripe.products.team_plan_30_price.yearly}")
  private String teamPlan30YearlyPriceId;

  @Value("${stripe.products.team_plan_50_price.monthly}")
  private String teamPlan50MonthlyPriceId;

  @Value("${stripe.products.team_plan_50_price.yearly}")
  private String teamPlan50YearlyPriceId;

  @Value("${stripe.products.team_pro_plan_10_price.monthly}")
  private String teamProPlan10MonthlyPriceId;

  @Value("${stripe.products.team_pro_plan_10_price.yearly}")
  private String teamProPlan10YearlyPriceId;

  @Value("${stripe.products.team_pro_plan_20_price.monthly}")
  private String teamProPlan20MonthlyPriceId;

  @Value("${stripe.products.team_pro_plan_20_price.yearly}")
  private String teamProPlan20YearlyPriceId;

  @Value("${stripe.products.team_pro_plan_30_price.monthly}")
  private String teamProPlan30MonthlyPriceId;

  @Value("${stripe.products.team_pro_plan_30_price.yearly}")
  private String teamProPlan30YearlyPriceId;

  @Value("${stripe.products.team_pro_plan_50_price.monthly}")
  private String teamProPlan50MonthlyPriceId;

  @Value("${stripe.products.team_pro_plan_50_price.yearly}")
  private String teamProPlan50YearlyPriceId;

  @PostConstruct
  public void init() {
    Stripe.apiKey = stripeApiKey;
    log.debug("Stripe API key configured successfully");
  }
}
