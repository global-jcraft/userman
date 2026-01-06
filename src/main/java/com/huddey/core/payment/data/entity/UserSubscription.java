package com.huddey.core.payment.data.entity;

import java.time.OffsetDateTime;

import com.huddey.core.payment.data.enums.SubscriptionStatus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_subscriptions", schema = "huddey_core")
public class UserSubscription {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "stripe_customer_id")
  private String stripeCustomerId;

  @Column(name = "stripe_subscription_id")
  private String stripeSubscriptionId;

  @Column(name = "plan_key")
  private String planKey;

  @Column(name = "pending_plan_key")
  private String pendingPlanKey;

  @Column(name = "interval")
  private String interval;

  @Column(name = "currency")
  private String currency;

  @Column(name = "billing_interval")
  private String billingInterval;

  @Column(name = "seat_count")
  private Long seatCount;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private SubscriptionStatus status;

  @Column(name = "created_at")
  private OffsetDateTime createdAt;

  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;

  @Column(name = "current_period_start")
  private OffsetDateTime currentPeriodStart;

  @Column(name = "current_period_end")
  private OffsetDateTime currentPeriodEnd;

  @Column(name = "cancel_at_period_end")
  private boolean cancelAtPeriodEnd;

  @Column(name = "pending_plan_effective_date")
  private OffsetDateTime pendingPlanEffectiveDate;

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
