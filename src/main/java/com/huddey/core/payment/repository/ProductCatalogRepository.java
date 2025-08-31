package com.huddey.core.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.huddey.core.payment.data.entity.Product;

@Repository
public interface ProductCatalogRepository extends JpaRepository<Product, Long> {

  Optional<Product> findByStripeProductId(String stripeProductId);

  @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.prices WHERE p.active = true")
  List<Product> findAllActiveWithPrices();
}
