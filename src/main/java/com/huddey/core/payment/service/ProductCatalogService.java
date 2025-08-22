package com.huddey.core.payment.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.huddey.core.payment.data.dto.ProductResponse;
import com.huddey.core.payment.data.entity.Product;
import com.huddey.core.payment.data.entity.ProductPrice;
import com.huddey.core.payment.repository.ProductCatalogRepository;
import com.huddey.core.payment.repository.ProductPriceRepository;
import com.huddey.core.payment.utils.PaymentUtils;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.ProductCollection;
import com.stripe.param.ProductListParams;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCatalogService {

  private final ProductCatalogRepository productCatalogRepository;
  private final ProductPriceRepository productPriceRepository;

  /**
   * Get all products from stripe
   *
   * @return
   */
  public List<ProductResponse> getAllProducts() throws StripeException {
    List<Product> products = productCatalogRepository.findAllActiveWithPrices();
    if (products.isEmpty()) {
      products = syncProductsFromStripe();
    }
    return PaymentUtils.convertToProductResponse(products);
  }

  public List<Product> syncProductsFromStripe() throws StripeException {
    ProductListParams params = ProductListParams.builder().setActive(true).setLimit(100L).build();

    ProductCollection stripeProducts = com.stripe.model.Product.list(params);
    List<Product> result = new ArrayList<>();

    for (com.stripe.model.Product stripeProduct : stripeProducts.autoPagingIterable()) {
      if (Boolean.TRUE.equals(stripeProduct.getActive())) {
        Product localProduct =
            productCatalogRepository
                .findByStripeProductId(stripeProduct.getId())
                .orElse(new Product());

        localProduct.setStripeProductId(stripeProduct.getId());
        localProduct.setName(stripeProduct.getName());
        localProduct.setDescription(stripeProduct.getDescription());
        localProduct.setActive(stripeProduct.getActive());

        localProduct = productCatalogRepository.save(localProduct);
        syncPricesForProduct(localProduct, stripeProduct.getId());
        result.add(localProduct);
      }
    }
    return result;
  }

  private void syncPricesForProduct(Product product, String stripeProductId)
      throws StripeException {
    var params =
        com.stripe.param.PriceListParams.builder()
            .setProduct(stripeProductId)
            .setActive(true)
            .build();

    var prices = Price.list(params);

    for (Price stripePrice : prices.getData()) {
      if (!productPriceRepository.findByStripePriceId(stripePrice.getId()).isPresent()) {
        ProductPrice productPrice = getProductPrice(product, stripePrice);
        productPriceRepository.save(productPrice);
      }
    }
  }

  private static ProductPrice getProductPrice(Product product, Price stripePrice) {
    ProductPrice productPrice = new ProductPrice();
    productPrice.setStripePriceId(stripePrice.getId());
    productPrice.setProduct(product);
    productPrice.setUnitAmount(
        BigDecimal.valueOf(stripePrice.getUnitAmount()).divide(BigDecimal.valueOf(100)));
    productPrice.setCurrency(stripePrice.getCurrency());
    productPrice.setRecurringInterval(
        stripePrice.getRecurring() != null ? stripePrice.getRecurring().getInterval() : null);
    productPrice.setActive(stripePrice.getActive());
    return productPrice;
  }
}
