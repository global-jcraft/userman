package com.huddey.core.userman.event;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.huddey.core.userman.config.LocalSqsMessagingTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventPublisher {

  private final LocalSqsMessagingTemplate messagingTemplate;
  private final ObjectMapper objectMapper;

  @Value("${spring.cloud.aws.sqs.localstack.queue.url}")
  private String queueUrl;

  public void publishUserRegisteredEvent(UserRegisteredEvent event) {
    try {
        String messageBody = objectMapper.writeValueAsString(event);
        log.debug("Sending message to queue {} with body: {}", queueUrl, messageBody);

        var response = messagingTemplate.sendMessage(
            queueUrl,
            messageBody,
            "default-group",
            UUID.randomUUID().toString()
        ).get(10, TimeUnit.SECONDS);
        log.debug("Message sent successfully with id: {}", response.messageId());
    } catch (Exception e) {
        log.error("Failed to publish message to SQS: {}", e.getMessage(), e);
        throw new RuntimeException("Failed to send message to SQS", e);
    }
  }
}