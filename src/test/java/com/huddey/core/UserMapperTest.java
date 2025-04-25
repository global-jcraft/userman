package com.huddey.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.huddey.core.userman.data.dto.UserDTO;
import com.huddey.core.userman.data.entity.Role;
import com.huddey.core.userman.data.entity.User;
import com.huddey.core.userman.data.entity.UserStatus;
import com.huddey.core.userman.mapper.UserMapper;

class UserMapperTest {

  @Test
  @DisplayName("toEntity should map all fields correctly")
  void toDto_ShouldMapAllFieldsCorrectly() {
    // Given
    OffsetDateTime lastLoginAt =
        OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, OffsetDateTime.now().getOffset());
    Set<Role> roles = new HashSet<>();
    Role role1 = new Role();
    role1.setName("ROLE_ADMIN");

    Role role2 = new Role();
    role2.setName("ROLE_USER");

    roles.add(role1);
    roles.add(role2);

    User user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");
    user.setFirstName("John");
    user.setLastName("Doe");
    user.setCompanyName("Test Company");
    user.setPhoneNumber("+1234567890");
    user.setProfilePictureUrl("https://example.com/profile.jpg");
    user.setRoles(roles);
    user.setStatus(UserStatus.ACTIVE);
    user.setLastLoginAt(lastLoginAt);

    // When
    UserDTO dto = UserMapper.toDto(user);

    // Then
    assertThat(dto.getId()).isEqualTo(1L);
    assertThat(dto.getEmail()).isEqualTo("test@example.com");
    assertThat(dto.getFirstName()).isEqualTo("John");
    assertThat(dto.getLastName()).isEqualTo("Doe");
    assertThat(dto.getCompanyName()).isEqualTo("Test Company");
    assertThat(dto.getPhoneNumber()).isEqualTo("+1234567890");
    assertThat(dto.getProfilePictureUrl()).isEqualTo("https://example.com/profile.jpg");
    assertThat(dto.getRoles()).containsExactlyInAnyOrder("ADMIN", "USER");
    assertThat(dto.getStatus()).isEqualTo("ACTIVE");
  }

  @Test
  @DisplayName("toDTO should map all fields correctly")
  void toDto_WithNullValues_ShouldMapCorrectly() {
    // Given
    User user = new User();
    user.setId(1L);
    user.setEmail("test@example.com");
    user.setRoles(new HashSet<>());
    user.setStatus(UserStatus.PENDING);

    // When
    UserDTO dto = UserMapper.toDto(user);

    // Then
    assertThat(dto.getId()).isEqualTo(1L);
    assertThat(dto.getEmail()).isEqualTo("test@example.com");
    assertThat(dto.getFirstName()).isNull();
    assertThat(dto.getLastName()).isNull();
    assertThat(dto.getCompanyName()).isNull();
    assertThat(dto.getPhoneNumber()).isNull();
    assertThat(dto.getProfilePictureUrl()).isNull();
    assertThat(dto.getRoles()).isEmpty();
    assertThat(dto.getStatus()).isEqualTo("PENDING");
    assertThat(dto.getLastLoginAt()).isNull();
  }
}
