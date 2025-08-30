package com.huddey.core.payment.utils;

import java.util.List;
import java.util.stream.Collectors;

import com.huddey.core.payment.data.dto.ProductResponse;
import com.huddey.core.payment.data.entity.Product;
import com.huddey.core.payment.data.entity.ProductFeatureCatalog;

public class PaymentUtils {

  private PaymentUtils() {}

  public static List<ProductResponse> convertToProductResponse(
      List<ProductFeatureCatalog> productList) {
    return productList.stream()
        .map(
            productFeature -> {
              Product product = productFeature.getProduct();
              return ProductResponse.builder()
                  .stripeProductId(product.getStripeProductId())
                  .name(product.getName())
                  .description(product.getDescription())
                  .active(product.getActive())
                  .createdAt(product.getCreatedAt())
                  .updatedAt(product.getUpdatedAt())
                  // .prices(new ArrayList<>(product.getPrices()))
                  .build();
            })
        .collect(Collectors.toList());
  }
}
