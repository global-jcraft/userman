package com.huddey.core.notification.service;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SmsNotificationService implements NotificationService {
  @Override
  public void sendNotification(
      String recipient, String message, String username, String confirmationLink) {
    log.debug("Sending SMS notification to: {}", username);
  }
}
