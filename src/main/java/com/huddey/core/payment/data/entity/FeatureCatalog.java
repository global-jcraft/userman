package com.huddey.core.payment.data.entity;

import java.time.OffsetDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "feature_catalog", schema = "huddey_core")
public class FeatureCatalog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "feature_name", unique = true, nullable = false)
  private String featureName;

  @Column(name = "feature_category", nullable = false)
  private String featureCategory;

  @Column(name = "display_name")
  private String displayName;

  @Column(name = "description")
  private String description;

  @Column(name = "feature_type")
  private String featureType;

  @Column(name = "default_value")
  private String defaultValue;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @Column(name = "created_at")
  private OffsetDateTime createdAt;

  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private FeatureCatalog parent;

  @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
  private List<FeatureCatalog> children;

  @PrePersist
  protected void onCreate() {
    createdAt = OffsetDateTime.now();
    updatedAt = createdAt;
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = OffsetDateTime.now();
  }
}
