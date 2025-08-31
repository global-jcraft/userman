package com.huddey.core.payment.service;

import org.springframework.stereotype.Service;

import com.huddey.core.payment.data.enums.SubscriptionPlan;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.ProductCreateParams;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class StripeSetupService {

  private static final String MONTH = "month";
  private static final String YEAR = "year";
  private static final String TEAMS_PLAN = "Teams Plan";
  private static final String TEAMS_PRO_PLAN = "Teams Pro Plan";

  public void createHuddeyProducts() throws StripeException {

    // Create Starter Plan Product
    Product starterProduct =
        createProduct(
            "Starter Plan",
            "Perfect for creators who are just starting out and want to focus on automating their social media presence.");

    createPrice(
        starterProduct.getId(),
        1500L,
        MONTH,
        "Starter Monthly",
        SubscriptionPlan.STARTER_MONTHLY.getInternalPriceId());
    createPrice(
        starterProduct.getId(),
        15300L,
        YEAR,
        "Starter Yearly",
        SubscriptionPlan.STARTER_YEARLY.getInternalPriceId());

    // Create Pro Plan Product
    Product proProduct =
        createProduct(
            "Pro Plan",
            "Designed for creators seeking to expand their reach, optimize performance through advanced analytics, and receive personalized guidance.");

    createPrice(
        proProduct.getId(),
        3900L,
        MONTH,
        "Pro Monthly",
        SubscriptionPlan.PRO_MONTHLY.getInternalPriceId());
    createPrice(
        proProduct.getId(),
        37400L,
        YEAR,
        "Pro Yearly",
        SubscriptionPlan.PRO_YEARLY.getInternalPriceId());

    // Create Elite Plan Product
    Product eliteProduct =
        createProduct(
            "Elite Plan",
            "For established content creators, influencers, and businesses with a large presence across multiple platforms.");

    createPrice(
        eliteProduct.getId(),
        9900L,
        MONTH,
        "Elite Monthly",
        SubscriptionPlan.ELITE_MONTHLY.getInternalPriceId());
    createPrice(
        eliteProduct.getId(),
        89100L,
        YEAR,
        "Elite Yearly",
        SubscriptionPlan.ELITE_YEARLY.getInternalPriceId());

    // Create Teams Plan Product
    Product teamsProduct10Seats =
        createProduct(
            TEAMS_PLAN.concat(" 10 seats"),
            "Ideal for agencies & brand teams that need collaboration & client management up to 10 seats.");

    createPrice(
        teamsProduct10Seats.getId(),
        24900L,
        MONTH,
        "Teams Monthly for 10 seats",
        SubscriptionPlan.TEAM_MONTHLY_10.getInternalPriceId());
    createPrice(
        teamsProduct10Seats.getId(),
        253900L,
        YEAR,
        "Teams Yearly for 10 seats",
        SubscriptionPlan.TEAM_YEARLY_10.getInternalPriceId());

    Product teamsProduct20Seats =
        createProduct(
            TEAMS_PLAN.concat(" 20 seats"),
            "Ideal for agencies & brand teams that need collaboration & client management up to 20 seats.");

    createPrice(
        teamsProduct20Seats.getId(),
        42900L,
        MONTH,
        "Teams Monthly for 20 seats",
        SubscriptionPlan.TEAM_MONTHLY_20.getInternalPriceId());
    createPrice(
        teamsProduct20Seats.getId(),
        437500L,
        YEAR,
        "Teams Yearly for 20 seats",
        SubscriptionPlan.TEAM_YEARLY_20.getInternalPriceId());

    Product teamsProduct30Seats =
        createProduct(
            TEAMS_PLAN.concat(" 30 seats"),
            "Ideal for agencies & brand teams that need collaboration & client management up to 30 seats.");

    createPrice(
        teamsProduct30Seats.getId(),
        59900L,
        MONTH,
        "Teams Monthly for 30 seats",
        SubscriptionPlan.TEAM_MONTHLY_30.getInternalPriceId());
    createPrice(
        teamsProduct30Seats.getId(),
        610900L,
        YEAR,
        "Teams Yearly for 30 seats",
        SubscriptionPlan.TEAM_YEARLY_30.getInternalPriceId());

    Product teamsProduct50Seats =
        createProduct(
            TEAMS_PLAN.concat(" 50 seats"),
            "Ideal for agencies & brand teams that need collaboration & client management up to 50 seats.");

    createPrice(
        teamsProduct50Seats.getId(),
        89900L,
        MONTH,
        "Teams Monthly for 50 seats",
        SubscriptionPlan.TEAM_MONTHLY_50.getInternalPriceId());
    createPrice(
        teamsProduct50Seats.getId(),
        916900L,
        YEAR,
        "Teams Yearly for 50 seats",
        SubscriptionPlan.TEAM_YEARLY_50.getInternalPriceId());

    // Create Teams Pro Plan Product
    Product teamsProProduct10Seats =
        createProduct(
            TEAMS_PRO_PLAN.concat(" 10 seats"),
            "Ideal for large agencies & enterprises managing multiple clients & regions up to 10 seats.");

    createPrice(
        teamsProProduct10Seats.getId(),
        42900L,
        MONTH,
        "Teams Monthly for 10 seats",
        SubscriptionPlan.TEAM_PRO_MONTHLY_10.getInternalPriceId());
    createPrice(
        teamsProProduct10Seats.getId(),
        423300L,
        YEAR,
        "Teams Yearly for 10 seats",
        SubscriptionPlan.TEAM_PRO_YEARLY_10.getInternalPriceId());

    Product teamsProProduct20Seats =
        createProduct(
            TEAMS_PRO_PLAN.concat(" 20 seats"),
            "Ideal for large agencies & enterprises managing multiple clients & regions up to 20 seats.");

    createPrice(
        teamsProProduct20Seats.getId(),
        64500L,
        MONTH,
        "Teams Monthly for 20 seats",
        SubscriptionPlan.TEAM_PRO_MONTHLY_20.getInternalPriceId());
    createPrice(
        teamsProProduct20Seats.getId(),
        642400L,
        YEAR,
        "Teams Yearly for 20 seats",
        SubscriptionPlan.TEAM_PRO_YEARLY_20.getInternalPriceId());

    Product teamsProProduct30Seats =
        createProduct(
            TEAMS_PRO_PLAN.concat(" 30 seats"),
            "Ideal for large agencies & enterprises managing multiple clients & regions up to 30 seats.");

    createPrice(
        teamsProProduct30Seats.getId(),
        84500L,
        MONTH,
        "Teams Monthly for 30 seats",
        SubscriptionPlan.TEAM_PRO_MONTHLY_30.getInternalPriceId());
    createPrice(
        teamsProProduct30Seats.getId(),
        841600L,
        YEAR,
        "Teams Yearly for 30 seats",
        SubscriptionPlan.TEAM_PRO_YEARLY_30.getInternalPriceId());

    Product teamsProProduct50Seats =
        createProduct(
            TEAMS_PRO_PLAN.concat(" 50 seats"),
            "Ideal for large agencies & enterprises managing multiple clients & regions up to 50 seats.");

    createPrice(
        teamsProProduct50Seats.getId(),
        118500L,
        MONTH,
        "Teams Monthly for 50 seats",
        SubscriptionPlan.TEAM_PRO_MONTHLY_50.getInternalPriceId());
    createPrice(
        teamsProProduct50Seats.getId(),
        1180200L,
        YEAR,
        "Teams Yearly for 50 seats",
        SubscriptionPlan.TEAM_PRO_YEARLY_50.getInternalPriceId());
  }

  private Product createProduct(String name, String description) throws StripeException {
    ProductCreateParams params =
        ProductCreateParams.builder()
            .setName(name)
            .setDescription(description)
            .setType(ProductCreateParams.Type.SERVICE)
            .build();

    return Product.create(params);
  }

  private Price createPrice(
      String productId, Long unitAmount, String interval, String nickname, String lookupKey)
      throws StripeException {
    PriceCreateParams.Recurring recurring =
        PriceCreateParams.Recurring.builder()
            .setInterval(PriceCreateParams.Recurring.Interval.valueOf(interval.toUpperCase()))
            .build();

    PriceCreateParams params =
        PriceCreateParams.builder()
            .setProduct(productId)
            .setUnitAmount(unitAmount)
            .setCurrency("eur")
            .setRecurring(recurring)
            .setNickname(nickname)
            .setActive(true)
            .setLookupKey(lookupKey)
            .build();

    Price price = Price.create(params);
    log.debug("Created price: {} - {} (€{})", price.getId(), nickname, unitAmount / 100.0);
    return price;
  }
}
