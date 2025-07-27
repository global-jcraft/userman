package com.huddey.core.payment.data.enums;

public class StripeConstants {

  public static final String CHECKOUT_COMPLETED = "checkout.session.completed";
  public static final String SUBSCRIPTION_CREATED = "customer.subscription.created";
  public static final String SUBSCRIPTION_UPDATED = "customer.subscription.updated";
  public static final String SUBSCRIPTION_DELETED = "customer.subscription.deleted";
  public static final String INVOICE_PAYMENT_OK = "invoice.payment_succeeded";
  public static final String INVOICE_PAYMENT_KO = "invoice.payment_failed";
}
