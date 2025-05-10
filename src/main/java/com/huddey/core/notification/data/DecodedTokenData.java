package com.huddey.core.notification.data;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DecodedTokenData {

  private final String email;
  private final UUID uuid;
}
