package com.huddey.core.payment.data.dto;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDto {

  private String invoiceId;
  private String invoiceNumber;
  private String status;
  private Long amountPaid;
  private Long amountDue;
  private String currency;
  private OffsetDateTime created;
  private OffsetDateTime dueDate;
  private OffsetDateTime paidAt;
  private String description;
  private String hostedInvoiceUrl;
  private String invoicePdf;
  private String subscriptionId;
}
