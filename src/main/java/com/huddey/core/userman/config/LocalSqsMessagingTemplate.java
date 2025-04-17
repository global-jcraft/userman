package com.huddey.core.userman.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class LocalSqsMessagingTemplate {
    
    private final SqsAsyncClient sqsAsyncClient;
    private final ObjectMapper objectMapper;

    public CompletableFuture<SendMessageResponse> sendMessage(String queueUrl, Object payload, String groupId, String deduplicationId) {
        try {
            String messageBody;
            if (payload instanceof String) {
                messageBody = (String) payload;
            } else {
                messageBody = objectMapper.writeValueAsString(payload);
            }
            
            String actualQueueUrl = queueUrl.replace("localhost.localstack.cloud:4566", "localhost:4566");
            
            SendMessageRequest request = SendMessageRequest.builder()
                .queueUrl(actualQueueUrl)
                .messageBody(messageBody)
                .messageGroupId(groupId)
                .messageDeduplicationId(deduplicationId)
                .build();
            
            return sqsAsyncClient.sendMessage(request);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}