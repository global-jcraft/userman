package com.huddey.core.payment.service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.huddey.core.payment.data.dto.BillingHistoryResponse;
import com.huddey.core.payment.data.dto.InvoiceDto;
import com.huddey.core.payment.data.entity.UserSubscription;
import com.huddey.core.payment.repository.UserSubscriptionRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Invoice;
import com.stripe.model.InvoiceCollection;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BillingService {

  private final UserSubscriptionRepository subscriptionRepository;

  public BillingService(UserSubscriptionRepository subscriptionRepository) {
    this.subscriptionRepository = subscriptionRepository;
  }

  public BillingHistoryResponse getBillingHistory(Long userId, Integer limit, String startingAfter)
      throws StripeException {
    log.debug(
        "Retrieving billing history for user: {}, limit: {}, startingAfter: {}",
        userId,
        limit,
        startingAfter);

    Optional<UserSubscription> userSub = subscriptionRepository.findByUserId(userId);

    if (userSub.isEmpty()) {
      log.debug("No subscription found for user: {}", userId);
      return BillingHistoryResponse.builder()
          .invoices(new ArrayList<>())
          .totalCount(0)
          .hasMore(false)
          .build();
    }

    if (userSub.get().getStripeCustomerId() == null) {
      log.debug("No Stripe customer ID found for user: {}", userId);
      return BillingHistoryResponse.builder()
          .invoices(new ArrayList<>())
          .totalCount(0)
          .hasMore(false)
          .build();
    }

    String customerId = userSub.get().getStripeCustomerId();
    log.debug("Found Stripe customer ID: {} for user: {}", customerId, userId);

    Map<String, Object> params = new HashMap<>();
    params.put("customer", customerId);
    params.put("limit", limit != null ? limit : 10);

    if (startingAfter != null) {
      params.put("starting_after", startingAfter);
      log.debug("Using pagination cursor: {}", startingAfter);
    } else {
      log.debug("No pagination cursor provided, starting from the beginning");
    }

    log.debug("Calling Stripe API to list invoices with params: {}", params);
    InvoiceCollection invoices = Invoice.list(params);

    log.debug(
        "Retrieved {} invoices from Stripe, hasMore: {}",
        invoices.getData().size(),
        invoices.getHasMore());

    List<InvoiceDto> invoiceDtos = invoices.getData().stream().map(this::convertToDto).toList();

    BillingHistoryResponse response =
        BillingHistoryResponse.builder()
            .invoices(invoiceDtos)
            .totalCount(invoices.getData().size())
            .hasMore(invoices.getHasMore())
            .nextCursor(
                invoices.getHasMore() && !invoices.getData().isEmpty()
                    ? invoices.getData().get(invoices.getData().size() - 1).getId()
                    : null)
            .build();

    log.debug(
        "Successfully built billing history response for user: {} with {} invoices",
        userId,
        invoiceDtos.size());
    return response;
  }

  public InvoiceDto getInvoice(Long userId, String invoiceId) throws StripeException {
    log.debug("Retrieving invoice: {} for user: {}", invoiceId, userId);

    Optional<UserSubscription> userSub = subscriptionRepository.findByUserId(userId);

    if (userSub.isEmpty()) {
      log.debug(
          "No subscription found for user: {} when retrieving invoice: {}", userId, invoiceId);
      throw new IllegalArgumentException("User has no subscription");
    }

    String customerId = userSub.get().getStripeCustomerId();
    log.debug("Found customer ID: {} for user: {}", customerId, userId);

    log.debug("Calling Stripe API to retrieve invoice: {}", invoiceId);
    Invoice invoice = Invoice.retrieve(invoiceId);

    log.debug(
        "Retrieved invoice: {} with customer: {}, status: {}",
        invoiceId,
        invoice.getCustomer(),
        invoice.getStatus());

    // Verify invoice belongs to user
    if (!customerId.equals(invoice.getCustomer())) {
      log.debug(
          "Invoice {} belongs to customer: {} but user {} has customer: {}",
          invoiceId,
          invoice.getCustomer(),
          userId,
          customerId);
      throw new IllegalArgumentException("Invoice does not belong to user");
    }

    InvoiceDto result = convertToDto(invoice);
    log.debug("Successfully converted invoice: {} to DTO for user: {}", invoiceId, userId);
    return result;
  }

  public String getInvoicePdfUrl(Long userId, String invoiceId) throws StripeException {
    log.debug("Retrieving PDF URL for invoice: {} and user: {}", invoiceId, userId);

    InvoiceDto invoice = getInvoice(userId, invoiceId);
    String pdfUrl = invoice.getInvoicePdf();

    if (pdfUrl != null) {
      log.debug("Found PDF URL for invoice: {}", invoiceId);
    } else {
      log.debug("No PDF URL available for invoice: {}", invoiceId);
    }

    return pdfUrl;
  }

  private InvoiceDto convertToDto(Invoice invoice) {
    return InvoiceDto.builder()
        .invoiceId(invoice.getId())
        .invoiceNumber(invoice.getNumber())
        .status(invoice.getStatus())
        .amountPaid(invoice.getAmountPaid())
        .amountDue(invoice.getAmountDue())
        .currency(invoice.getCurrency())
        .created(
            Instant.ofEpochSecond(invoice.getCreated())
                .atZone(ZoneId.systemDefault())
                .toOffsetDateTime())
        .dueDate(
            invoice.getDueDate() != null
                ? Instant.ofEpochSecond(invoice.getDueDate())
                    .atZone(ZoneId.systemDefault())
                    .toOffsetDateTime()
                : null)
        .paidAt(
            invoice.getStatusTransitions() != null
                    && invoice.getStatusTransitions().getPaidAt() != null
                ? Instant.ofEpochSecond(invoice.getStatusTransitions().getPaidAt())
                    .atZone(ZoneId.systemDefault())
                    .toOffsetDateTime()
                : null)
        .description(getInvoiceDescription(invoice))
        .hostedInvoiceUrl(invoice.getHostedInvoiceUrl())
        .invoicePdf(invoice.getInvoicePdf())
        // .subscriptionId(invoice.getSubscription())
        .build();
  }

  private String getInvoiceDescription(Invoice invoice) {
    if (invoice.getLines() != null && !invoice.getLines().getData().isEmpty()) {
      return invoice.getLines().getData().get(0).getDescription();
    }
    return "Subscription payment";
  }
}
