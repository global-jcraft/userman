package com.huddey.core.payment.data.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingHistoryResponse {

  private List<InvoiceDto> invoices;
  private int totalCount;
  private boolean hasMore;
  private String nextCursor;
}
