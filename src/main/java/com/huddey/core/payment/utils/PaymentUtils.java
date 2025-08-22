package com.huddey.core.payment.utils;

import java.util.List;
import java.util.stream.Collectors;

import com.huddey.core.payment.data.dto.ProductResponse;
import com.huddey.core.payment.data.entity.Product;

public class PaymentUtils {

  private PaymentUtils() {}

  public static List<ProductResponse> convertToProductResponse(List<Product> productList) {
    return productList.stream()
        .map(
            product ->
                ProductResponse.builder()
                    .stripeProductId(product.getStripeProductId())
                    .name(product.getName())
                    .description(product.getDescription())
                    .active(product.getActive())
                    .prices(product.getPrices())
                    .build())
        .collect(Collectors.toList());
  }
}
