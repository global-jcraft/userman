package com.huddey.core.payment.service;

import org.springframework.stereotype.Service;

import com.huddey.core.payment.data.enums.PaymentMethodType;
import com.stripe.exception.StripeException;
import com.stripe.model.SetupIntent;
import com.stripe.param.SetupIntentCreateParams;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class StripeSetupIntentService {

  public SetupIntent createSetupIntent(String customerId, PaymentMethodType paymentMethodType)
      throws StripeException {
    log.debug(
        "Creating SetupIntent for customer: {} with payment method type: {}",
        customerId,
        paymentMethodType);

    return SetupIntent.create(
        SetupIntentCreateParams.builder()
            .setCustomer(customerId)
            .addPaymentMethodType(paymentMethodType.getStripeType())
            .setUsage(SetupIntentCreateParams.Usage.OFF_SESSION)
            .build());
  }
}
