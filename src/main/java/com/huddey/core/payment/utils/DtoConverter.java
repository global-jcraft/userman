package com.huddey.core.payment.utils;

import java.util.List;
import java.util.stream.Collectors;

import com.huddey.core.payment.data.dto.ProductFeatureDto;
import com.huddey.core.payment.data.dto.ProductPriceDto;
import com.huddey.core.payment.data.entity.ProductFeatureCatalog;
import com.huddey.core.payment.data.entity.ProductPrice;

public class DtoConverter {

  private DtoConverter() {}

  public static List<ProductPriceDto> convertToPriceDto(List<ProductPrice> prices) {
    return prices.stream()
        .map(
            price ->
                ProductPriceDto.builder()
                    .stripePriceId(price.getStripePriceId())
                    .unitAmount(price.getUnitAmount())
                    .currency(price.getCurrency())
                    .recurringInterval(price.getRecurringInterval())
                    .active(price.getActive())
                    .createdAt(price.getCreatedAt())
                    .updatedAt(price.getUpdatedAt())
                    .build())
        .collect(Collectors.toList());
  }

  public static List<ProductFeatureDto> convertToFeatureDtos(List<ProductFeatureCatalog> features) {
    return features.stream()
        .map(
            pfc ->
                ProductFeatureDto.builder()
                    .featureName(pfc.getFeature().getFeatureName())
                    .featureCategory(pfc.getFeature().getFeatureCategory())
                    .displayValue(pfc.getFeature().getDisplayName() + ": " + pfc.getFeatureValue())
                    .build())
        .collect(Collectors.toList());
  }
}
