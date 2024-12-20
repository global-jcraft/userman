package com.huddey.core.userman.data.dto.response;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
  private String timestamp;
  private int status;
  private String error;
  private String message;
  private String path;
  private Map<String, String> errors; // For validation errors
}
