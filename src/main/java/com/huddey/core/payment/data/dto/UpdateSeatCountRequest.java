package com.huddey.core.payment.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSeatCountRequest {
  private long newSeatCount; // required: >= 1
  private String prorationBehavior; // optional: CREATE_PRORATIONS | NONE | ALWAYS_INVOICE
}
