package com.huddey.core.payment.service;

import java.time.OffsetDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.huddey.core.payment.repository.WebhookEventRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WebhookCleanupService {

  private final WebhookEventRepository webhookEventRepository;

  public WebhookCleanupService(WebhookEventRepository webhookEventRepository) {
    this.webhookEventRepository = webhookEventRepository;
  }

  /** Cleanup old webhook events older than 30 days Runs daily at 2 AM */
  @Scheduled(cron = "0 0 2 * * ?")
  @Transactional
  public void cleanupOldWebhookEvents() {
    OffsetDateTime cutoffDate = OffsetDateTime.now().minusDays(30);

    try {
      webhookEventRepository.deleteOldEvents(cutoffDate);
      log.info("Cleaned up webhook events older than {}", cutoffDate);
    } catch (Exception e) {
      log.error("Failed to cleanup old webhook events: {}", e.getMessage(), e);
    }
  }
}
