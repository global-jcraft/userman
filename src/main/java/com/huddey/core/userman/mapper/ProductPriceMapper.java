package com.huddey.core.userman.mapper;

import com.huddey.core.payment.data.dto.ProductPriceDto;
import com.huddey.core.payment.data.entity.ProductPrice;

public class ProductPriceMapper {

  private ProductPriceMapper() {}

  public static ProductPriceDto toDto(ProductPrice productPrice) {
    return ProductPriceDto.builder()
        .currency(productPrice.getCurrency())
        .unitAmount(productPrice.getUnitAmount())
        .currency(productPrice.getCurrency())
        .recurringInterval(productPrice.getRecurringInterval())
        .active(productPrice.getActive())
        .build();
  }
}
