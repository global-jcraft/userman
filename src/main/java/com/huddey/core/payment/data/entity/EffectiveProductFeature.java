package com.huddey.core.payment.data.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "effective_product_features", schema = "huddey_core")
public class EffectiveProductFeature {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "feature_id", nullable = false)
  private FeatureCatalog feature;

  @Column(name = "feature_value")
  private String featureValue;

  @Column(name = "is_enabled", nullable = false)
  private Boolean isEnabled = true;
}
