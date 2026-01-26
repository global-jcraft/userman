package com.huddey.core.payment.data.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFeatureDto {
  private String featureName;
  private String featureCategory;
  private String displayValue; // displayName + featureValue concatenated

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<ProductFeatureDto> subFeatures;
}
