package com.huddey.core.userman.data.entity;

import lombok.Getter;

@Getter
public enum UserRole {
  // Basic Roles
  USER("ROLE_USER", "Basic authenticated user role", RoleCategory.BASIC),
  ADMIN("ROLE_ADMIN", "System administrator role", RoleCategory.ADMIN),

  // Content Creator Roles
  CONTENT_CREATOR("ROLE_CONTENT_CREATOR", "Content creator role", RoleCategory.CREATOR),
  MENTOR("ROLE_MENTOR", "Creative Academy mentor role", RoleCategory.CREATOR),

  // System Roles
  SYSTEM("ROLE_SYSTEM", "System operations role", RoleCategory.SYSTEM);

  private final String roleName;
  private final String description;
  private final RoleCategory category;
  private static final String ROLE_PREFIX = "ROLE_";

  UserRole(String roleName, String description, RoleCategory category) {
    this.roleName = roleName;
    this.description = description;
    this.category = category;
  }

  @Getter
  public enum RoleCategory {
    BASIC("Basic roles"),
    ADMIN("Administrative roles"),
    CREATOR("Content creator roles"),
    SYSTEM("System roles");

    private final String description;

    RoleCategory(String description) {
      this.description = description;
    }
  }

  /**
   * Checks if this role has administrative privileges
   *
   * @return true if the role is an admin role
   */
  public boolean isAdminRole() {
    return this.category == RoleCategory.ADMIN;
  }

  /**
   * Checks if this role is a creator role
   *
   * @return true if the role is a creator role
   */
  public boolean isCreatorRole() {
    return this.category == RoleCategory.CREATOR;
  }

  /**
   * Checks if this role is a system role
   *
   * @return true if the role is a system role
   */
  public boolean isSystemRole() {
    return this.category == RoleCategory.SYSTEM;
  }

  /**
   * Gets the Spring Security compatible role name
   *
   * @return the role name with ROLE_ prefix
   */
  public String getSpringSecurityRoleName() {
    return this.roleName;
  }

  /**
   * Gets the role name without the ROLE_ prefix
   *
   * @return the simplified role name
   */
  public String getSimpleName() {
    return this.roleName.replace(ROLE_PREFIX, "");
  }

  /**
   * Finds a Role by its name (case-insensitive)
   *
   * @param roleName the name to search for (with or without ROLE_ prefix)
   * @return the matching Role or null if not found
   */
  public static UserRole fromString(String roleName) {
    String normalizedName = roleName.toUpperCase();
    if (!normalizedName.startsWith(ROLE_PREFIX)) {
      normalizedName = ROLE_PREFIX + normalizedName;
    }
    for (UserRole role : UserRole.values()) {
      if (role.getRoleName().equals(normalizedName)) {
        return role;
      }
    }
    return null;
  }
}
