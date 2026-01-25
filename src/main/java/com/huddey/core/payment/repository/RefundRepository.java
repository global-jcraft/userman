package com.huddey.core.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.huddey.core.payment.data.entity.Refund;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

  Optional<Refund> findByStripeRefundId(String stripeRefundId);

  List<Refund> findByUserId(Long userId);
}
