package com.huddey.core.payment.repository;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.huddey.core.payment.data.entity.WebhookEvent;

@Repository
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, String> {

  Optional<WebhookEvent> findByStripeEventId(String stripeEventId);

  @Modifying
  @Query("DELETE FROM WebhookEvent w WHERE w.processedAt < :cutoffDate")
  void deleteOldEvents(@Param("cutoffDate") OffsetDateTime cutoffDate);
}
