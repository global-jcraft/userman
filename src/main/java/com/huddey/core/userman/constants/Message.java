package com.huddey.core.userman.constants;

@SuppressWarnings("all")
public class Message {

  // Error messages
  public static final String ERROR_OAUTH2_AUTH_FAIL = "error.oauth2.auth.fail";
  public static final String ERROR_AUTH_FAIL = "error.auth.fail";
  public static final String GLOBAL_AUTH_ERROR = "global.auth.error";
  public static final String GLOBAL_VALIDATION_ERROR = "global.validation.error";
  public static final String GLOBAL_REQUEST_ERROR = "global.request.error";
  public static final String GLOBAL_INTERNAL_ERROR = "global.internal.error";
  public static final String GLOBAL_INTERNAL_UNEXPECTED_ERROR = "global.internal.unexpected.error";
  public static final String GLOBAL_AUTH_SUCCESS = "global.auth.success";
  public static final String GLOBAL_USER_VERIFY_SUCCESS = "global.user.verify.success";
  public static final String SIMPLE_AUTH_REG_SUCCESS = "simple.auth.reg.success";
  public static final String SIMPLE_FULL_AUTH_REG_SUCCESS = "simple.auth.full.reg.success";
  public static final String SIMPLE_LOGIN_SUCCESS = "simple.login.success";
  public static final String SIMPLE_AUTH_LOGOUT = "simple.auth.logout";
  public static final String REFRESH_TOKEN_SUCCESS = "refresh.token.success";
  public static final String REFRESH_TOKEN_EXPIRED = "refresh.token.expired";
  public static final String PASSWORD_RESET_REQUEST = "password.reset.request";
  public static final String PASSWORD_RESET_REQUEST_SUCCESS = "password.reset.request.success";

  public static final String USER_ACCOUNT_ACTIVE_ERROR = "user.account.active.error";
  public static final String USER_INVALID_CREDENTIALS_ERROR = "user.invalid.credentials.error";
  public static final String USER_INVALID_REFRESH_TOKEN = "user.invalid.refresh.token";
  public static final String USER_ACCOUNT_ACTIVE = "user.account.active";
  public static final String USER_EXPIRED_VERIFY_TOKEN = "user.expired.verify.token";
  public static final String USER_VERIFY_INVALID_TOKEN = "user.invalid.verify.token";

  // Phone Verification Messages
  public static final String PHONE_VERIFICATION_SENT_SUCCESS = "phone.verification.sent.success";
  public static final String PHONE_VERIFICATION_SUCCESS = "phone.verification.success";
  public static final String PHONE_ALREADY_VERIFIED = "phone.already.verified";
  public static final String PHONE_NUMBER_REQUIRED = "phone.number.required";
  public static final String PHONE_VERIFICATION_INVALID_TOKEN = "phone.verification.invalid.token";
  public static final String PHONE_VERIFICATION_EXPIRED_TOKEN = "phone.verification.expired.token";

  // Profile Messages
  public static final String PROFILE_FETCH_SUCCESS = "profile.fetch.success";

  public static final String STRIPE_PRODUCT_LIST_SUCCESS = "stripe.product.list.success";
  public static final String STRIPE_PRODUCT_LIST_ERROR = "stripe.product.list.error";

  // Billing Messages
  public static final String BILLING_HISTORY_SUCCESS = "billing.history.success";
  public static final String BILLING_HISTORY_ERROR = "billing.history.error";
  public static final String INVOICE_RETRIEVE_SUCCESS = "invoice.retrieve.success";
  public static final String INVOICE_RETRIEVE_ERROR = "invoice.retrieve.error";
  public static final String INVOICE_PDF_ERROR = "invoice.pdf.error";

  // Payment Method Messages
  public static final String PAYMENT_METHOD_LIST_SUCCESS = "payment.method.list.success";
  public static final String PAYMENT_METHOD_ADD_SUCCESS = "payment.method.add.success";
  public static final String PAYMENT_METHOD_UPDATE_SUCCESS = "payment.method.update.success";
  public static final String PAYMENT_METHOD_REMOVE_SUCCESS = "payment.method.remove.success";
  public static final String PAYMENT_METHOD_DEFAULT_SET_SUCCESS =
      "payment.method.default.set.success";
  public static final String PAYMENT_METHOD_BACKUP_SET_SUCCESS =
      "payment.method.backup.set.success";
  public static final String PAYMENT_METHOD_ADD_ERROR = "payment.method.add.error";
  public static final String PAYMENT_METHOD_UPDATE_ERROR = "payment.method.update.error";
  public static final String PAYMENT_METHOD_REMOVE_ERROR = "payment.method.remove.error";
  public static final String PAYMENT_SERVICE_UNAVAILABLE = "payment.service.unavailable";
  public static final String PAYMENT_SERVICE_ERROR = "payment.service.error";
}
