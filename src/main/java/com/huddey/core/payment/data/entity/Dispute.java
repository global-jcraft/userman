package com.huddey.core.payment.data.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "disputes", schema = "huddey_core")
public class Dispute {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "stripe_dispute_id", unique = true, nullable = false)
  private String stripeDisputeId;

  @Column(name = "stripe_charge_id", nullable = false)
  private String stripeChargeId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "amount", nullable = false)
  private Long amount;

  @Column(name = "currency", nullable = false, length = 3)
  private String currency;

  @Column(name = "reason", length = 50)
  private String reason;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "evidence_due_by")
  private OffsetDateTime evidenceDueBy;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

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
