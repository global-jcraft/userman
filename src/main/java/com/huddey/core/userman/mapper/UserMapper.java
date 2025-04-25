package com.huddey.core.userman.mapper;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import com.huddey.core.userman.data.dto.UserDTO;
import com.huddey.core.userman.data.entity.User;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public static UserDTO toDto(User user) {
    return UserDTO.builder()
        .id(user.getId())
        .email(user.getEmail())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .companyName(user.getCompanyName())
        .phoneNumber(user.getPhoneNumber())
        .profilePictureUrl(user.getProfilePictureUrl())
        .roles(
            user.getRoles().stream()
                .map(role -> role.getName().replace("ROLE_", ""))
                .collect(Collectors.toSet()))
        .status(user.getStatus().toString())
        .lastLoginAt(
            user.getLastLoginAt() != null ? user.getLastLoginAt().format(DATE_FORMATTER) : null)
        .build();
  }
}
