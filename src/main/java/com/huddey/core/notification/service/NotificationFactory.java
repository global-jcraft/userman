package com.huddey.core.notification.service;

import static com.huddey.core.notification.data.constants.NotificationConstants.EMAIL_NOTIFICATION;
import static com.huddey.core.notification.data.constants.NotificationConstants.SMS_NOTIFICATION;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificationFactory {

  private final EmailNotificationService emailNotificationService;
  private final SmsNotificationService smsNotificationService;
  private final Map<String, Supplier<NotificationService>> notificationMap = new HashMap<>();

  @Autowired
  public NotificationFactory(
      EmailNotificationService emailNotificationService,
      SmsNotificationService smsNotificationService) {
    this.emailNotificationService = emailNotificationService;
    this.smsNotificationService = smsNotificationService;
    notificationMap.put(EMAIL_NOTIFICATION, () -> emailNotificationService);
    notificationMap.put(SMS_NOTIFICATION, () -> smsNotificationService);
  }

  public NotificationService getNotificationService(String type) {
    Supplier<NotificationService> supplier = notificationMap.get(type.toLowerCase());
    if (supplier != null) {
      return supplier.get();
    }
    throw new IllegalArgumentException("Unsupported notification type: " + type);
  }
}
