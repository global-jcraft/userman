package com.huddey.core.payment.service;

import org.springframework.stereotype.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.ProductCreateParams;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class StripeSetupService {

  public void createHuddeyProducts() throws StripeException {
    // Create Starter Plan Product
    Product starterProduct =
        createProduct(
            "Starter Plan",
            "Perfect for creators who are just starting out and want to focus on automating their social media presence.");

    createPrice(starterProduct.getId(), 1500L, "month", "Starter Monthly");
    createPrice(starterProduct.getId(), 15000L, "year", "Starter Yearly");

    // Create Pro Plan Product
    Product proProduct =
        createProduct(
            "Pro Plan",
            "Designed for creators seeking to expand their reach, optimize performance through advanced analytics, and receive personalized guidance.");

    createPrice(proProduct.getId(), 3500L, "month", "Pro Monthly");
    createPrice(proProduct.getId(), 35000L, "year", "Pro Yearly");

    // Create Elite Plan Product
    Product eliteProduct =
        createProduct(
            "Elite Plan",
            "For established content creators, influencers, and businesses with a large presence across multiple platforms.");

    createPrice(eliteProduct.getId(), 8500L, "month", "Elite Monthly");
    createPrice(eliteProduct.getId(), 85000L, "year", "Elite Yearly");
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

  private Price createPrice(String productId, Long unitAmount, String interval, String nickname)
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
            .build();

    Price price = Price.create(params);
    log.debug("Created price: {} - {} (€{})", price.getId(), nickname, unitAmount / 100.0);
    return price;
  }
}
