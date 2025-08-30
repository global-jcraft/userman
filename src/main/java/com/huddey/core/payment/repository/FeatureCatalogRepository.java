package com.huddey.core.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.huddey.core.payment.data.entity.FeatureCatalog;

@Repository
public interface FeatureCatalogRepository extends JpaRepository<FeatureCatalog, Long> {

  Optional<FeatureCatalog> findByFeatureName(String featureName);

  List<FeatureCatalog> findByFeatureCategoryAndIsActive(String featureCategory, Boolean isActive);

  @Query(
      "SELECT fc FROM FeatureCatalog fc WHERE fc.isActive = true ORDER BY fc.featureCategory, fc.featureName")
  List<FeatureCatalog> findAllActiveFeatures();

  List<FeatureCatalog> findByFeatureCategoryOrderByFeatureName(String featureCategory);
}
