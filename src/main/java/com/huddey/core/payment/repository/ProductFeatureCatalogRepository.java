package com.huddey.core.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.huddey.core.payment.data.entity.ProductFeatureCatalog;

@Repository
public interface ProductFeatureCatalogRepository
    extends JpaRepository<ProductFeatureCatalog, Long> {

  List<ProductFeatureCatalog> findByProductIdAndIsEnabled(Long productId, Boolean isEnabled);

  @Query(
      "SELECT pfc FROM ProductFeatureCatalog pfc JOIN FETCH pfc.feature WHERE pfc.product.id = :productId AND pfc.isEnabled = true")
  List<ProductFeatureCatalog> findEnabledFeaturesByProductId(@Param("productId") Long productId);

  @Query(
      "SELECT pfc FROM ProductFeatureCatalog pfc JOIN FETCH pfc.feature WHERE pfc.product.id = :productId AND pfc.feature.featureCategory = :category AND pfc.isEnabled = true")
  List<ProductFeatureCatalog> findEnabledFeaturesByProductIdAndCategory(
      @Param("productId") Long productId, @Param("category") String category);

  Optional<ProductFeatureCatalog> findByProductIdAndFeatureId(Long productId, Long featureId);

  @Query(
      "SELECT DISTINCT pfc FROM ProductFeatureCatalog pfc JOIN FETCH pfc.product JOIN FETCH pfc.feature LEFT JOIN FETCH pfc.feature.parent")
  List<ProductFeatureCatalog> findAllWithProduct();

  @Query(
      "SELECT pfc FROM ProductFeatureCatalog pfc JOIN FETCH pfc.feature WHERE pfc.product.name = :productName AND pfc.isEnabled = true")
  List<ProductFeatureCatalog> findFeaturesByProductName(@Param("productName") String productName);

  List<ProductFeatureCatalog> findByProductIdAndFeatureIdAndIsEnabled(
      Long productId, Long featureId, Boolean isEnabled);
}
