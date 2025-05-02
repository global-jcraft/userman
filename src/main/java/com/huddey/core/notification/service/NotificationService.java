package com.huddey.core.notification.service;

public interface NotificationService {
  void sendNotification(String recipient, String message, String username, String confirmationLink);
}
