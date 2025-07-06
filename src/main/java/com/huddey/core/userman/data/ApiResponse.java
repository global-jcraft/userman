package com.huddey.core.userman.data;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Generic API Response wrapper */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
  private boolean success;
  private String message;
  private T data;
  private String error;
  private OffsetDateTime timestamp;

  public ApiResponse(boolean success, String message, T data, OffsetDateTime timestamp) {
    this();
    this.success = success;
    this.message = message;
    this.data = data;
    this.timestamp = timestamp;
  }

  public ApiResponse(boolean success, String message, OffsetDateTime timestamp) {
    this();
    this.success = success;
    this.message = message;
    this.timestamp = timestamp;
  }

  // Static factory methods
  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, "Success", data, OffsetDateTime.now());
  }

  public static <T> ApiResponse<T> success(String message) {
    return new ApiResponse<>(true, message, OffsetDateTime.now());
  }

  public static <T> ApiResponse<T> success(String message, T data) {
    return new ApiResponse<>(true, message, data, OffsetDateTime.now());
  }

  public static <T> ApiResponse<T> error(String message) {
    ApiResponse<T> response = new ApiResponse<>();
    response.success = false;
    response.message = message;
    return response;
  }

  public static <T> ApiResponse<T> error(String message, String error) {
    ApiResponse<T> response = new ApiResponse<>();
    response.success = false;
    response.message = message;
    response.error = error;
    return response;
  }
}
