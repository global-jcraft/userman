package com.huddey.core.payment.data.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCheckoutSessionRequest {

  @NotNull(message = "Plan predefined key is required")
  private String planKey; // e.g. "huddey_teams" or "huddey_pro"

  @NotNull(message = "Interval is required")
  private String interval; // "month" | "year"

  @NotNull(message = "Currency is required")
  private String currency; // "EUR" | "USD" | "GBP"

  @NotNull(message = "Seats is required")
  private long seats; // 1 for individuals, N for Teams; client sends current seat count

  @NotBlank(message = "Success URL is required")
  private String successUrl;

  @NotBlank(message = "Cancel URL is required")
  private String cancelUrl;

  @Email(message = "Invalid email format")
  private String customerEmail;
}
