package com.huddey.core.payment.data.enums;

public class StripeConstants {

  private StripeConstants() {}

  public static final String CHECKOUT_COMPLETED = "checkout.session.completed";
  public static final String SUBSCRIPTION_CREATED = "customer.subscription.created";
  public static final String SUBSCRIPTION_UPDATED = "customer.subscription.updated";
  public static final String SUBSCRIPTION_DELETED = "customer.subscription.deleted";
  public static final String INVOICE_PAYMENT_OK = "invoice.payment_succeeded";
  public static final String INVOICE_PAYMENT_KO = "invoice.payment_failed";
  public static final String INVOICE_CREATED = "invoice.created";
  public static final String INVOICE_PAID = "invoice.paid";
  public static final String INVOICE_PAYMENT_PAID = "invoice_payment.paid";
  public static final String INVOICE_FINALIZED = "invoice.finalized";
  public static final String CHARGE_SUCCEEDED = "charge.succeeded";
  public static final String PAYMENT_METHOD_ATTACHED = "payment_method.attached";
  public static final String PAYMENT_INTENT_SUCCEEDED = "payment_intent.succeeded";
  public static final String PAYMENT_INTENT_CREATED = "payment_intent.created";
  public static final String SETUP_INTENT_CREATED = "setup_intent.created";
  public static final String SETUP_INTENT_SUCCEEDED = "setup_intent.succeeded";
  public static final String FINANCIAL_CONNECTIONS_ACCOUNT_CREATED =
      "financial_connections.account.created";

  // Refund events
  public static final String CHARGE_REFUNDED = "charge.refunded";

  // Dispute events
  public static final String DISPUTE_CREATED = "charge.dispute.created";
  public static final String DISPUTE_UPDATED = "charge.dispute.updated";
  public static final String DISPUTE_CLOSED = "charge.dispute.closed";
  public static final String DISPUTE_FUNDS_WITHDRAWN = "charge.dispute.funds_withdrawn";
  public static final String DISPUTE_FUNDS_REINSTATED = "charge.dispute.funds_reinstated";
}
