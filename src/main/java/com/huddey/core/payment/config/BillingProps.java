package com.huddey.core.payment.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "billing")
public class BillingProps {
  private List<String> currencyCodes = List.of("EUR");
  private String taxBehavior = "EXCLUSIVE"; // EXCLUSIVE or INCLUSIVE
  private String taxCode = "txcd_10103001"; // SaaS
  private List<ProductDef> products = new ArrayList<>();

  @Data
  public static class ProductDef {
    private String key; // e.g. huddey_pro
    private String name;
    private String description;
    private boolean seatBased; // if true, quantity=seats at subscription time
    private List<RecurringDef> recurring = new ArrayList<>();
  }

  @Data
  public static class RecurringDef {
    private String interval; // "month" | "year"
    private Map<String, Long> amounts = new HashMap<>(); // currency -> minor units
  }
}
