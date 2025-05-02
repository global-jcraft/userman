package com.huddey.core.notification.data.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "notifications", schema = "huddey_core")
public class Notification {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private String userId;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "notification_type", columnDefinition = "notification_type_enum", nullable = false)
  private NotificationType notificationType;

  @Column(name = "recipient", nullable = false)
  private String recipient;

  @Column(name = "subject", nullable = false)
  private String subject;

  @Column(name = "message", nullable = false)
  private String message;

  @Column(name = "sent_at", nullable = false)
  private OffsetDateTime sentAt;

  @Column(name = "is_read", nullable = false)
  private Boolean isRead;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "status")
  private NotificationStatus status;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;
}
