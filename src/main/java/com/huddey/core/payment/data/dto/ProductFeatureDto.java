package com.huddey.core.payment.data.dto;

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
}
