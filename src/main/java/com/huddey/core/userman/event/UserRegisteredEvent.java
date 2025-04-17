package com.huddey.core.userman.event;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRegisteredEvent {
  private String eventId;
  private String eventName;
  private String userId;
  private String email;
  private OffsetDateTime timestamp;
  private boolean successRegistered;
  private UUID correlationId;
  private String errorMessage;
  private int version;
}
