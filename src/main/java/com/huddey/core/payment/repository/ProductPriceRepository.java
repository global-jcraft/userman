package com.huddey.core.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.huddey.core.payment.data.entity.ProductPrice;

@Repository
public interface ProductPriceRepository extends JpaRepository<ProductPrice, Long> {

  Optional<ProductPrice> findByStripePriceId(String stripePriceId);

  Optional<ProductPrice> findByProductId(Long productId);

  Optional<List<ProductPrice>> findByPlanId(String planId);
}
