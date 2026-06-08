package com.huddey.core.payment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.huddey.core.payment.data.entity.EffectiveProductFeature;

@Repository
public interface EffectiveProductFeatureRepository
    extends JpaRepository<EffectiveProductFeature, Long> {

  @Query(
      "SELECT DISTINCT epf FROM EffectiveProductFeature epf JOIN FETCH epf.product JOIN FETCH epf.feature LEFT JOIN FETCH epf.feature.parent")
  List<EffectiveProductFeature> findAllWithProduct();
}
