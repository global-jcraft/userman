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
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "payment_methods", schema = "huddey_core")
public class PaymentMethod {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "stripe_payment_method_id", nullable = false)
  private String stripePaymentMethodId;

  @Column(name = "type", nullable = false)
  private String type;

  @Column(name = "last_four")
  private String lastFour;

  @Column(name = "brand")
  private String brand;

  @Column(name = "exp_month")
  private Integer expMonth;

  @Column(name = "exp_year")
  private Integer expYear;

  @Column(name = "is_default")
  private boolean isDefault;

  @Column(name = "is_backup")
  private boolean isBackup;

  @Column(name = "created_at")
  private OffsetDateTime createdAt;

  @Column(name = "updated_at")
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
