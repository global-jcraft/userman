package com.huddey.core.payment.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.huddey.core.payment.data.entity.WebhookEvent;
import com.huddey.core.payment.repository.WebhookEventRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WebhookIdempotencyService {

  private final WebhookEventRepository webhookEventRepository;

  public WebhookIdempotencyService(WebhookEventRepository webhookEventRepository) {
    this.webhookEventRepository = webhookEventRepository;
  }

  /**
   * Checks if webhook event has already been processed successfully
   *
   * @param eventId Stripe event ID
   * @return true if already processed successfully, false if new or failed
   */
  public boolean isAlreadyProcessed(String eventId) {
    Optional<WebhookEvent> existing = webhookEventRepository.findByStripeEventId(eventId);
    if (existing.isPresent()) {
      WebhookEvent event = existing.get();
      if (event.getStatus() == WebhookEvent.ProcessingStatus.SUCCESS) {
        log.debug("Webhook event {} already processed successfully", eventId);
        return true;
      }
      log.debug(
          "Webhook event {} exists but not successful, status: {}", eventId, event.getStatus());
    }
    return false;
  }

  /**
   * Atomically tries to mark webhook as processing if not already processed successfully
   *
   * @param eventId Stripe event ID
   * @param eventType Stripe event type
   * @return true if should process, false if already processed successfully
   */
  @Transactional
  public boolean markAsProcessing(String eventId, String eventType) {
    Optional<WebhookEvent> existing = webhookEventRepository.findByStripeEventId(eventId);

    if (existing.isPresent()) {
      WebhookEvent event = existing.get();
      if (event.getStatus() == WebhookEvent.ProcessingStatus.SUCCESS) {
        log.debug("Webhook event {} already processed successfully", eventId);
        return false;
      }
      // Update existing failed/processing event to processing
      event.setStatus(WebhookEvent.ProcessingStatus.PROCESSING);
      event.setProcessedAt(java.time.OffsetDateTime.now());
      event.setErrorMessage(null);
      webhookEventRepository.save(event);
      log.debug("Updated webhook event {} to processing status", eventId);
      return true;
    }

    // Create new processing event
    try {
      WebhookEvent webhookEvent = new WebhookEvent(eventId, eventType);
      webhookEventRepository.save(webhookEvent);
      log.debug("Created new webhook event {} as processing", eventId);
      return true;
    } catch (Exception e) {
      // Race condition - another thread created it
      log.warn("Race condition creating webhook event {}: {}", eventId, e.getMessage());
      return markAsProcessing(eventId, eventType); // Retry once
    }
  }

  /** Marks webhook event as successfully processed */
  @Transactional
  public void markAsSuccess(String eventId) {
    webhookEventRepository
        .findByStripeEventId(eventId)
        .ifPresent(
            event -> {
              event.setStatus(WebhookEvent.ProcessingStatus.SUCCESS);
              webhookEventRepository.save(event);
              log.debug("Marked webhook event {} as success", eventId);
            });
  }

  /** Marks webhook event as failed with error message */
  @Transactional
  public void markAsFailed(String eventId, String errorMessage) {
    webhookEventRepository
        .findByStripeEventId(eventId)
        .ifPresent(
            event -> {
              event.setStatus(WebhookEvent.ProcessingStatus.FAILED);
              event.setErrorMessage(errorMessage);
              webhookEventRepository.save(event);
              log.debug("Marked webhook event {} as failed: {}", eventId, errorMessage);
            });
  }
}
