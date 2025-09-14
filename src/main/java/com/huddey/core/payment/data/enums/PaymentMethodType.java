package com.huddey.core.payment.data.enums;

import lombok.Getter;

@Getter
public enum PaymentMethodType {
  CARD("card"),
  BANK_ACCOUNT("us_bank_account"),
  DIGITAL_WALLET("digital_wallet"),
  CRYPTO("crypto");

  private final String stripeType;

  PaymentMethodType(String stripeType) {
    this.stripeType = stripeType;
  }

  public static PaymentMethodType fromStripeType(String stripeType) {
    for (PaymentMethodType type : values()) {
      if (type.stripeType.equals(stripeType)) {
        return type;
      }
    }
    return CARD; // default fallback
  }
}
