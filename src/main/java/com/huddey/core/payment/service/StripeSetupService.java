package com.huddey.core.payment.service;

import static java.util.Locale.ROOT;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.huddey.core.payment.config.BillingProps;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.PriceCollection;
import com.stripe.model.Product;
import com.stripe.model.ProductCollection;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.PriceListParams;
import com.stripe.param.ProductCreateParams;
import com.stripe.param.ProductListParams;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeSetupService {

  private final BillingProps props;

  /** Make catalog exist & be correct. Safe to call on deploy. */
  public void upsertCatalog() throws StripeException {
    for (BillingProps.ProductDef p : props.getProducts()) {
      Product product = upsertProduct(p);
      for (BillingProps.RecurringDef r : p.getRecurring()) {
        for (String currency : props.getCurrencyCodes()) {
          Long amount = r.getAmounts().get(currency.toUpperCase(ROOT));
          if (amount == null) continue; // currency not configured for this interval
          String lookupKey = priceLookupKey(p.getKey(), r.getInterval(), currency);
          upsertPrice(product.getId(), lookupKey, amount, r.getInterval(), currency);
        }
      }
    }
  }

  private Product upsertProduct(BillingProps.ProductDef p) throws StripeException {
    // Try to find by name (or better: by metadata tag if you prefer)
    Product existing = findProductByName(p.getName());
    if (existing != null) {
      // Ensure active & metadata
      if (Boolean.FALSE.equals(existing.getActive()) || needsMetadataUpdate(existing, p)) {
        existing = existing.update(ProductUpdateParamsFactory.forProps(p));
        log.info("Updated product {}", existing.getId());
      }
      return existing;
    }

    ProductCreateParams params =
        ProductCreateParams.builder()
            .setName(p.getName())
            .setDescription(p.getDescription())
            .setType(ProductCreateParams.Type.SERVICE)
            .setActive(true)
            .putMetadata("key", p.getKey())
            .putMetadata("seat_based", Boolean.toString(p.isSeatBased()))
            .setTaxCode(props.getTaxCode())
            .build();

    Product created = Product.create(params);
    log.info("Created product {} ({})", created.getId(), p.getName());
    return created;
  }

  private boolean needsMetadataUpdate(Product product, BillingProps.ProductDef p) {
    Map<String, String> md = Optional.ofNullable(product.getMetadata()).orElse(Map.of());
    return !Objects.equals(md.get("key"), p.getKey())
        || !Objects.equals(md.get("seat_based"), Boolean.toString(p.isSeatBased()))
        || !Objects.equals(product.getTaxCode(), props.getTaxCode());
  }

  private Product findProductByName(String name) throws StripeException {
    ProductListParams params = ProductListParams.builder().setActive(true).setLimit(100L).build();
    ProductCollection list = Product.list(params);
    for (Product p : list.getData()) {
      if (name.equals(p.getName())) return p;
    }
    return null;
  }

  private void upsertPrice(
      String productId, String lookupKey, long unitAmount, String interval, String currency)
      throws StripeException {

    Price existing = findPriceByLookupKey(lookupKey);
    if (existing != null) {
      // If amount, currency, or interval changed, create a new price (Stripe forbids edits).
      boolean sameCurrency = currency.equalsIgnoreCase(existing.getCurrency());
      boolean sameInterval =
          interval.equalsIgnoreCase(
              existing.getRecurring().getInterval().toLowerCase(ROOT));
      boolean sameAmount = Objects.equals(existing.getUnitAmount(), unitAmount);

      if (sameCurrency && sameInterval && sameAmount && Boolean.TRUE.equals(existing.getActive())) {
        return; // nothing to do
      }

      // Deactivate old price and create a new one with the same lookup_key.
      existing = existing.update(PriceUpdateParamsFactory.deactivate());
      log.info("Deactivated price {} for {}", existing.getId(), lookupKey);
    }

    PriceCreateParams.Recurring recurring =
        PriceCreateParams.Recurring.builder()
            .setInterval(PriceCreateParams.Recurring.Interval.valueOf(interval.toUpperCase(ROOT)))
            .setUsageType(PriceCreateParams.Recurring.UsageType.LICENSED)
            .build();

    PriceCreateParams params =
        PriceCreateParams.builder()
            .setProduct(productId)
            .setCurrency(currency.toLowerCase(ROOT))
            .setUnitAmount(unitAmount)
            .setRecurring(recurring)
            .setTaxBehavior(PriceCreateParams.TaxBehavior.valueOf(props.getTaxBehavior()))
            .setActive(true)
            .setLookupKey(lookupKey)
            .build();

    Price created = Price.create(params);
    log.info("Created price {} {}", lookupKey, created.getId());
  }

  private Price findPriceByLookupKey(String lookupKey) throws StripeException {
    PriceListParams params =
        PriceListParams.builder().addLookupKey(lookupKey).setActive(true).setLimit(1L).build();
    PriceCollection pc = Price.list(params);
    return pc.getData().isEmpty() ? null : pc.getData().get(0);
  }

  private static String priceLookupKey(String productKey, String interval, String currency) {
    // Stable, readable keys, e.g. huddey_pro:month:EUR
    return String.join(":", productKey, interval.toLowerCase(ROOT), currency.toUpperCase(ROOT));
  }
}

/** Simple helpers to avoid clutter above. */
class ProductUpdateParamsFactory {
  private ProductUpdateParamsFactory() {}

  static com.stripe.param.ProductUpdateParams forProps(BillingProps.ProductDef p) {
    return com.stripe.param.ProductUpdateParams.builder()
        .setActive(true)
        .putMetadata("key", p.getKey())
        .putMetadata("seat_based", Boolean.toString(p.isSeatBased()))
        .setTaxCode("txcd_10103001")
        .build();
  }
}

class PriceUpdateParamsFactory {
  private PriceUpdateParamsFactory() {}

  static com.stripe.param.PriceUpdateParams deactivate() {
    return com.stripe.param.PriceUpdateParams.builder().setActive(false).build();
  }
}
