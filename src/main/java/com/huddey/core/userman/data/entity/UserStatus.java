package com.huddey.core.userman.data.entity;

import lombok.Getter;

@Getter
public enum UserStatus {
  PENDING("PENDING"),
  ACTIVE("ACTIVE"),
  SUSPENDED("SUSPENDED"),
  DEACTIVATED("DEACTIVATED");

  private final String status;

  UserStatus(String status) {
    this.status = status;
  }
}
