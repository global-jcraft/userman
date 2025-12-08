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

  @PostConstruct
  public void init() {
    Stripe.apiKey = stripeApiKey;
    log.debug("Stripe API key configured successfully");
  }
}
