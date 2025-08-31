package com.huddey.core.payment.data.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "webhook_events")
@Data
@NoArgsConstructor
public class WebhookEvent {

  @Id
  @Column(name = "stripe_event_id", length = 255)
  private String stripeEventId;

  @Column(name = "event_type", nullable = false, length = 100)
  private String eventType;

  @Column(name = "processed_at", nullable = false)
  private OffsetDateTime processedAt;

  @Column(name = "status", nullable = false, length = 20)
  @Enumerated(EnumType.STRING)
  private ProcessingStatus status;

  @Column(name = "error_message", length = 1000)
  private String errorMessage;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  public WebhookEvent(String stripeEventId, String eventType) {
    this.stripeEventId = stripeEventId;
    this.eventType = eventType;
    this.processedAt = OffsetDateTime.now();
    this.status = ProcessingStatus.PROCESSING;
  }

  @PrePersist
  protected void onCreate() {
    createdAt = OffsetDateTime.now();
    updatedAt = createdAt;
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = OffsetDateTime.now();
  }

  public enum ProcessingStatus {
    PROCESSING,
    SUCCESS,
    FAILED
  }
}
