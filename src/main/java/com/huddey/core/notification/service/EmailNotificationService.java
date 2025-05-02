package com.huddey.core.notification.service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

/**
 * EmailNotificationService is a service that handles sending email notifications. It implements the
 * NotificationService interface.
 */
@Slf4j
@Service
public class EmailNotificationService implements NotificationService {

  @Autowired private SesClient sesClient;
  @Autowired private SpringTemplateEngine templateEngine;

  @Value("${aws.ses.from}")
  private String senderEmail;


  @Override
  public void sendNotification(
      String recipient, String message, String username, String confirmationLink) {
    log.debug(
        "EmailNotificationService.sendNotification() -> Sending email to {} - Start time: {}",
        recipient,
        OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

    Context context = new Context();
    context.setVariable("username", username);
    context.setVariable("confirmationLink", confirmationLink);

    String htmlBody;
    try {
      htmlBody =
          templateEngine.process("welcome-email", context); // Corresponds to welcome-email.html
    } catch (Exception e) {
      log.error("Error processing email template for user: {}", recipient, e);
      // Depending on requirements, you might want to re-throw or handle differently
      return; // Don't proceed if template processing fails
    }

    SendEmailRequest sendEmailRequest =
        buildSendEmailRequest(recipient, "Welcome to Our Service!", htmlBody);

    try {
      SendEmailResponse response = sesClient.sendEmail(sendEmailRequest);
      log.info(
          "Successfully sent welcome email to: {}. Message ID: {}",
          recipient,
          response.messageId());
      // Consider storing the messageId for tracking/auditing if needed

    } catch (MessageRejectedException e) {
      // Specific SES exception: often related to blacklisted addresses, unverified sender/recipient
      // (in sandbox)
      log.error(
          "SES rejected the email to {}: {}. Check sender/recipient verification, blacklists, or content.",
          recipient,
          e.awsErrorDetails().errorMessage(),
          e);
      // Potentially add this email to a suppression list in your system
    } catch (MailFromDomainNotVerifiedException e) {
      log.error(
          "SES sending failed: Sender domain for '{}' is not verified. Details: {}",
          senderEmail,
          e.awsErrorDetails().errorMessage(),
          e);
      // This is a configuration error that needs fixing in AWS SES console.
    } catch (SesException e) {
      // Catch broader SES exceptions (e.g., throttling, temporary failures)
      log.error(
          "SES Exception occurred sending email to {}: {}. Status Code: {}",
          recipient,
          e.awsErrorDetails().errorMessage(),
          e.statusCode(),
          e);
      // Implement retry logic here if appropriate for temporary failures (e.g., using Spring Retry)
    } catch (Exception e) {
      // Catch any other unexpected exceptions during the send process
      log.error("Unexpected error sending email to {}: {}", recipient, e.getMessage(), e);
    }
  }

  private SendEmailRequest buildSendEmailRequest(
      String recipient, String subject, String htmlBody) {
    Destination destination = Destination.builder().toAddresses(recipient).build();

    Content subjectContent = Content.builder().data(subject).charset("UTF-8").build();
    Content htmlContent = Content.builder().data(htmlBody).charset("UTF-8").build();

    Body body = Body.builder().html(htmlContent).build();

    Message message = Message.builder().subject(subjectContent).body(body).build();

    return SendEmailRequest.builder()
        .destination(destination)
        .message(message)
        .source(senderEmail)
        // Optional: Specify Configuration Set for tracking events (opens, clicks, bounces)
        // .configurationSetName("your-config-set-name")
        .build();
  }
}
