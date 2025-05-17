package com.huddey.core.notification.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

@Slf4j
@Service
public class SmsNotificationService implements NotificationService {

  private final SnsClient snsClient;

  @Autowired
  public SmsNotificationService(SnsClient snsClient) {
    this.snsClient = snsClient;
  }

  @Override
  public void sendNotification(
      String recipient, String message, String username, String confirmationLink) {
    // recipient is the phone number
    // message is the OTP or content of the SMS
    // username is typically the user's email or identifier, for logging/context
    // confirmationLink is not typically used for SMS OTPs

    log.debug(
        "Attempting to send SMS. To: {}, Message: \"{}\", ForUser: {}",
        recipient,
        message,
        username);

    try {
      PublishRequest publishRequest =
          PublishRequest.builder().phoneNumber(recipient).message(message).build();

      PublishResponse publishResponse = snsClient.publish(publishRequest);
      log.debug(
          "Successfully sent SMS to phone number {}. Message ID: {}",
          recipient,
          publishResponse.messageId());

    } catch (SnsException e) {
      log.error(
          "Failed to send SMS to phone number {} for user {}: {}. AWS Error: {}",
          recipient,
          username,
          e.getMessage(),
          e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : "N/A",
          e);
      // Depending on the exception, you might want to throw a custom exception
      // or handle it (e.g., retry, queue for later)
    } catch (Exception e) {
      log.error(
          "An unexpected error occurred while sending SMS to phone number {} for user {}: {}",
          recipient,
          username,
          e.getMessage(),
          e);
    }
  }
}
