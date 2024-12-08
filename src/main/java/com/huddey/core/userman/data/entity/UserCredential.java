package com.huddey.core.userman.data.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_credentials", schema = "huddey_userman")
@Getter
@Setter
@NoArgsConstructor
public class UserCredential {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "auth_provider_id", nullable = false)
  private AuthProvider authProvider;

  @Column(nullable = false)
  private String identifier;

  @Column(name = "password_hash")
  private String passwordHash;

  @Column(name = "password_reset_token")
  private String passwordResetToken;

  @Column(name = "password_reset_token_expires_at")
  private OffsetDateTime passwordResetTokenExpiresAt;

  @Column(name = "created_at", nullable = false, updatable = false)
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
