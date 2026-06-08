package com.huddey.core.payment.utils;

import java.util.List;
import java.util.Map;

import com.huddey.core.payment.data.dto.ProductFeatureDto;
import com.huddey.core.payment.data.dto.ProductPriceDto;
import com.huddey.core.payment.data.entity.EffectiveProductFeature;
import com.huddey.core.payment.data.entity.FeatureCatalog;
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
                    .build())
        .toList();
  }

  public static List<ProductFeatureDto> convertToFeatureDtos(List<ProductFeatureCatalog> features) {
    java.util.Map<Long, ProductFeatureDto> dtoMap = new java.util.HashMap<>();
    java.util.Map<Long, Long> childToParentMap = new java.util.HashMap<>();

    // 1. Initialize with explicit features
    for (ProductFeatureCatalog pfc : features) {
      ProductFeatureDto dto =
          ProductFeatureDto.builder()
              .featureName(pfc.getFeature().getFeatureName())
              .featureCategory(pfc.getFeature().getFeatureCategory())
              .displayValue(pfc.getFeature().getDisplayName() + ": " + pfc.getFeatureValue())
              .subFeatures(new java.util.ArrayList<>())
              .build();

      dtoMap.put(pfc.getFeature().getId(), dto);
    }

    // 2. Build Hierarchy (traverse up)
    for (ProductFeatureCatalog pfc : features) {
      Long childId = pfc.getFeature().getId();
      ProductFeatureDto childDto = dtoMap.get(childId);

      FeatureCatalog parent = pfc.getFeature().getParent();

      while (parent != null) {
        Long parentId = parent.getId();
        // If parent DTO doesn't exist, create it (Successor/Group node)
        var finalParent = parent;
        ProductFeatureDto parentDto =
            dtoMap.computeIfAbsent(
                parentId,
                k ->
                    ProductFeatureDto.builder()
                        .featureName(finalParent.getFeatureName())
                        .featureCategory(finalParent.getFeatureCategory())
                        .displayValue(finalParent.getDisplayName()) // Use name as value for groups
                        .subFeatures(new java.util.ArrayList<>())
                        .build());

        // Link Child to Parent
        if (!parentDto.getSubFeatures().contains(childDto)) {
          parentDto.getSubFeatures().add(childDto);
        }

        // Track relationship
        childToParentMap.put(childId, parentId);

        // Move one level up
        childId = parentId;
        childDto = parentDto;
        parent = parent.getParent();
      }
    }

    // 3. Collect Roots
    java.util.List<ProductFeatureDto> roots = new java.util.ArrayList<>();
    for (java.util.Map.Entry<Long, ProductFeatureDto> entry : dtoMap.entrySet()) {
      if (!childToParentMap.containsKey(entry.getKey())) {
        roots.add(entry.getValue());
      }
    }

    return roots;
  }

  public static List<ProductFeatureDto> convertToFeatureDtosFromEffective(
      List<EffectiveProductFeature> features) {
    Map<Long, ProductFeatureDto> dtoMap = new java.util.HashMap<>();
    Map<Long, Long> childToParentMap = new java.util.HashMap<>();

    // 1. Initialize with explicit features
    for (EffectiveProductFeature epf : features) {
      ProductFeatureDto dto =
          ProductFeatureDto.builder()
              .featureName(epf.getFeature().getFeatureName())
              .featureCategory(epf.getFeature().getFeatureCategory())
              .displayValue(epf.getFeature().getDisplayName() + ": " + epf.getFeatureValue())
              .subFeatures(new java.util.ArrayList<>())
              .build();

      dtoMap.put(epf.getFeature().getId(), dto);
    }

    // 2. Build Hierarchy (traverse up)
    for (EffectiveProductFeature epf : features) {
      Long childId = epf.getFeature().getId();
      ProductFeatureDto childDto = dtoMap.get(childId);

      FeatureCatalog parent = epf.getFeature().getParent();

      while (parent != null) {
        Long parentId = parent.getId();
        // If parent DTO doesn't exist, create it (Successor/Group node)
        var finalParent = parent;
        ProductFeatureDto parentDto =
            dtoMap.computeIfAbsent(
                parentId,
                k ->
                    ProductFeatureDto.builder()
                        .featureName(finalParent.getFeatureName())
                        .featureCategory(finalParent.getFeatureCategory())
                        .displayValue(finalParent.getDisplayName()) // Use name as value for groups
                        .subFeatures(new java.util.ArrayList<>())
                        .build());

        // Link Child to Parent
        if (!parentDto.getSubFeatures().contains(childDto)) {
          parentDto.getSubFeatures().add(childDto);
        }

        // Track relationship
        childToParentMap.put(childId, parentId);

        // Move one level up
        childId = parentId;
        childDto = parentDto;
        parent = parent.getParent();
      }
    }

    // 3. Collect Roots
    java.util.List<ProductFeatureDto> roots = new java.util.ArrayList<>();
    for (java.util.Map.Entry<Long, ProductFeatureDto> entry : dtoMap.entrySet()) {
      if (!childToParentMap.containsKey(entry.getKey())) {
        roots.add(entry.getValue());
      }
    }

    return roots;
  }
}
