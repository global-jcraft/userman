package com.huddey.core.userman.config;

import java.time.Duration;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Configuration
@Profile("local")
public class LocalSqsConfig {
  @Value("${spring.cloud.aws.sqs.region}")
  private String awsRegion;

  @Value("${spring.cloud.aws.sqs.client.timeout:30}")
  private Integer clientTimeoutSeconds;

  @Value("${spring.cloud.aws.sqs.localstack.endpoint}")
  private String localstackEndpoint;

  private SqsAsyncClient sqsAsyncClient;

  @Bean
  public SqsAsyncClient sqsAsyncClient() {
    Duration clientTimeout = Duration.ofSeconds(clientTimeoutSeconds);
    
    AwsCredentialsProvider credentialsProvider = new AwsCredentialsProvider() {
      @Override
      public AwsCredentials resolveCredentials() {
        return AwsBasicCredentials.create("dummy", "dummy");
      }
    };

    sqsAsyncClient = SqsAsyncClient.builder()
        .endpointOverride(java.net.URI.create(localstackEndpoint))
        .region(Region.of(awsRegion))
        .credentialsProvider(credentialsProvider)
        .httpClient(createHttpClient())
        .overrideConfiguration(ClientOverrideConfiguration.builder()
            .apiCallTimeout(clientTimeout)
            .apiCallAttemptTimeout(clientTimeout)
            .addExecutionInterceptor(new software.amazon.awssdk.core.interceptor.ExecutionInterceptor() {
                @Override
                public software.amazon.awssdk.http.SdkHttpRequest modifyHttpRequest(
                        software.amazon.awssdk.core.interceptor.Context.ModifyHttpRequest context,
                        software.amazon.awssdk.core.interceptor.ExecutionAttributes executionAttributes) {
                    return context.httpRequest().toBuilder()
                        .putHeader("X-Amz-Target", "AmazonSQS.SendMessage")
                        .build();
                }
            })
            .build())
        .build();
    return sqsAsyncClient;
  }

  private SdkAsyncHttpClient createHttpClient() {
    return AwsCrtAsyncHttpClient.builder()
        .maxConcurrency(100)
        .build();
  }

  @Bean
  public LocalSqsMessagingTemplate messagingTemplate(SqsAsyncClient sqsAsyncClient, ObjectMapper objectMapper) {
    return new LocalSqsMessagingTemplate(sqsAsyncClient, objectMapper);
  }

  @PreDestroy
  public void cleanup() {
    if (sqsAsyncClient != null) {
      sqsAsyncClient.close();
    }
  }
}