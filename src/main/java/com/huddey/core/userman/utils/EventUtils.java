package com.huddey.core.userman.utils;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.huddey.core.userman.data.entity.User;
import com.huddey.core.userman.event.UserRegisteredEvent;

public class EventUtils {

  private EventUtils() {}

  public static UserRegisteredEvent eventBuilder(User user) {
    return UserRegisteredEvent.builder()
        .eventId(String.valueOf(user.getId()).concat("-").concat(UUID.randomUUID().toString()))
        .eventName("UserRegisteredEvent")
        .userId(user.getId().toString())
        .email(user.getEmail())
        .timestamp(user.getCreatedAt())
        .successRegistered(true)
        .correlationId(UUID.randomUUID())
        .timestamp(OffsetDateTime.now())
        .errorMessage(null)
        .version(1)
        .build();
  }
}
