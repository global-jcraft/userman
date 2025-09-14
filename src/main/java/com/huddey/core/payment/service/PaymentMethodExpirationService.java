package com.huddey.core.payment.service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.huddey.core.payment.data.entity.PaymentMethod;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentMethodExpirationService {

  private final PaymentMethodService paymentMethodService;

  @Scheduled(cron = "0 0 9 * * ?") // Daily at 9 AM
  public void checkExpiredPaymentMethods() {
    log.info("Starting expired payment methods check");

    List<PaymentMethod> expiredMethods = paymentMethodService.getExpiredPaymentMethods();

    if (!expiredMethods.isEmpty()) {
      log.warn("Found {} expired payment methods", expiredMethods.size());

      for (PaymentMethod method : expiredMethods) {
        log.info(
            "Expired payment method found - User: {}, Method: {}, Expired: {}/{}",
            method.getUserId(),
            method.getId(),
            method.getExpMonth(),
            method.getExpYear());
      }
    }

    log.info("Completed expired payment methods check");
  }
}
