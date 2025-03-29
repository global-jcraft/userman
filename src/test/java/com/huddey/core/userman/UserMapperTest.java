package com.huddey.core.userman;

import com.huddey.core.userman.data.dto.UserDTO;
import com.huddey.core.userman.data.entity.Role;
import com.huddey.core.userman.data.entity.User;
import com.huddey.core.userman.data.entity.UserStatus;
import com.huddey.core.userman.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    @Test
    @DisplayName("toEntity should map all fields correctly")
    void toDto_ShouldMapAllFieldsCorrectly() {
        // Given
        OffsetDateTime lastLoginAt =
                OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, OffsetDateTime.now().getOffset());
        Set<Role> roles = new HashSet<>();
        roles.add(Role.builder().name("ROLE_ADMIN").build());
        roles.add(Role.builder().name("ROLE_USER").build());

        User user =
                User.builder()
                        .id(1L)
                        .email("test@example.com")
                        .firstName("John")
                        .lastName("Doe")
                        .companyName("Test Company")
                        .phoneNumber("+1234567890")
                        .profilePictureUrl("https://example.com/profile.jpg")
                        .roles(roles)
                        .status(UserStatus.ACTIVE)
                        .lastLoginAt(lastLoginAt)
                        .build();

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
        User user =
                User.builder()
                        .id(1L)
                        .email("test@example.com")
                        .roles(new HashSet<>())
                        .status(UserStatus.PENDING)
                        .build();

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
