package com.huddey.core.userman.data;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
  private boolean success;
  private String message;
  private Object data;
  private String error;
  private OffsetDateTime timestamp;
}
