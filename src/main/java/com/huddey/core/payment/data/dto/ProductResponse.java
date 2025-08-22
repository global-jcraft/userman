package com.huddey.core.payment.data.dto;

import java.time.OffsetDateTime;
import java.util.List;

import com.huddey.core.payment.data.entity.ProductPrice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

  private String stripeProductId;
  private String name;
  private String description;
  private Boolean active = true;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
  private List<ProductPrice> prices;
}
