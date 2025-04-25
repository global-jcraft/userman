package com.huddey.core.userman.utils;

import java.time.OffsetDateTime;

import com.huddey.core.userman.data.ApiResponse;

public class ApiUtils {

  private ApiUtils() {}

  public static ApiResponse buildApiResponse(
      boolean success, String successMessage, Object data, String errorMessage) {
    return ApiResponse.builder()
        .success(success)
        .message(successMessage)
        .error(errorMessage)
        .data(data)
        .timestamp(OffsetDateTime.now())
        .build();
  }
}
