package com.huddey.core.notification.service;

import org.springframework.stereotype.Service;

@Service
public class SmsNotificationService implements NotificationService {
  @Override
  public void sendNotification(
      String recipient, String message, String username, String confirmationLink) {}
}
