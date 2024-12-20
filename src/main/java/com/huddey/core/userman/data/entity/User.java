package com.huddey.core.userman.data.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users", schema = "huddey_userman")
@Getter
@Setter
@NoArgsConstructor
public class User implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(name = "email_verified")
  private boolean emailVerified;

  @Column(name = "email_verification_token")
  private String emailVerificationToken;

  @Column(name = "email_verification_token_expires_at")
  private OffsetDateTime emailVerificationTokenExpiresAt;

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "last_name")
  private String lastName;

  @Column(name = "profile_picture_url")
  private String profilePictureUrl;

  @Column(name = "company_name")
  private String companyName;

  @Column(name = "phone_number")
  private String phoneNumber;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private UserStatus status = UserStatus.PENDING;

  @Column(name = "registration_ip")
  private String registrationIp;

  @Column(name = "last_login_ip")
  private String lastLoginIp;

  @Column(name = "last_login_at")
  private OffsetDateTime lastLoginAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      schema = "huddey_userman",
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles = new HashSet<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<UserCredential> credentials = new HashSet<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<SocialConnection> socialConnections = new HashSet<>();

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
