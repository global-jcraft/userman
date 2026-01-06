package com.huddey.core.payment.data.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SwitchPlanRequest {
  @NotNull(message = "Plan predefined key is required")
  private String planKey; // e.g. "huddey_teams" or "huddey_pro"

  @NotNull(message = "Interval is required")
  private String interval; // "month" | "year"

  @NotNull(message = "Currency is required")
  private String currency; // "EUR" | "USD" | "GBP"
}
