package com.huddey.core.notification.service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.huddey.core.notification.data.entity.Notification;
import com.huddey.core.notification.data.entity.NotificationStatus;
import com.huddey.core.notification.data.entity.NotificationType;
import com.huddey.core.notification.repository.NotificationRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NotificationHandler {

  private final NotificationFactory notificationFactory;
  private final NotificationRepository notificationRepository;

  @Autowired
  public NotificationHandler(
      NotificationFactory notificationFactory, NotificationRepository notificationRepository) {
    this.notificationFactory = notificationFactory;
    this.notificationRepository = notificationRepository;
  }

  @Async
  public void notify(
      String type, String recipient, String message, String username, String confirmationLink) {
    log.debug(
        "NotificationHandler.notify() -> Sending sms notification to {} - Start time: {}",
        recipient,
        OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    NotificationService service = notificationFactory.getNotificationService(type);
    service.sendNotification(recipient, message, username, confirmationLink);

    Notification notification = new Notification();
    notification.setUserId(username);
    notification.setRecipient(recipient);
    if (type.equals("email")) {
      notification.setSubject("new_user_email_registration_notification");
    } else if (type.equals("sms")) {
      notification.setSubject("new_user_mobile_registration_notification");
    } else {
      throw new IllegalArgumentException("Invalid notification type");
    }
    notification.setMessage("Welcome to Huddey, " + username);
    notification.setSentAt(OffsetDateTime.now());
    notification.setIsRead(false);
    notification.setStatus(NotificationStatus.SENT);
    notification.setCreatedAt(OffsetDateTime.now());
    notification.setNotificationType(NotificationType.EMAIL);
    notificationRepository.save(notification);
  }
}
