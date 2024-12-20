package com.huddey.core.userman.data.dto;

import java.util.Set;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {
  private Long id;
  private String email;
  private String firstName;
  private String lastName;
  private String companyName;
  private String phoneNumber;
  private String profilePictureUrl;
  private String lastLoginAt;
  private Set<String> roles;
  private String status;
}
