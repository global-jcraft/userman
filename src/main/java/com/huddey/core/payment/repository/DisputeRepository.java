package com.huddey.core.payment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.huddey.core.payment.data.entity.Dispute;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {

  Optional<Dispute> findByStripeDisputeId(String stripeDisputeId);

  List<Dispute> findByUserId(Long userId);

  List<Dispute> findByStatus(String warningNeedsResponse);
}
