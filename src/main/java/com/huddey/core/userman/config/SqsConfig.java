package com.huddey.core.userman.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.awspring.cloud.sqs.operations.SqsTemplate;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Slf4j
@Configuration
@Profile("!local")
public class SqsConfig {
  private final String awsRegion;
  private final Duration clientTimeout;
  private SqsAsyncClient sqsAsyncClient;

  public SqsConfig(
      @Value("${spring.cloud.aws.sqs.region}") String awsRegion,
      @Value("${spring.cloud.aws.sqs.client.timeout:30}") Integer clientTimeoutSeconds) {
    this.awsRegion = awsRegion;
    this.clientTimeout = Duration.ofSeconds(clientTimeoutSeconds);
  }

  @Bean
  public SqsAsyncClient sqsAsyncClient() {
    this.sqsAsyncClient =
        SqsAsyncClient.builder()
            .region(Region.of(awsRegion))
            .overrideConfiguration(createClientOverrideConfiguration())
            .httpClient(createHttpClient())
            .build();
    return this.sqsAsyncClient;
  }

  private ClientOverrideConfiguration createClientOverrideConfiguration() {
    return ClientOverrideConfiguration.builder()
        .apiCallTimeout(clientTimeout)
        .apiCallAttemptTimeout(clientTimeout)
        .build();
  }

  private SdkAsyncHttpClient createHttpClient() {
    return AwsCrtAsyncHttpClient.builder()
        .maxConcurrency(50)
        .connectionTimeout(Duration.ofSeconds(5))
        .build();
  }

  @Bean
  public SqsTemplate sqsTemplate(SqsAsyncClient sqsAsyncClient) {
    return SqsTemplate.builder().sqsAsyncClient(sqsAsyncClient).build();
  }

  @PreDestroy
  public void cleanup() {
    try {
      if (sqsAsyncClient != null) {
        sqsAsyncClient.close();
      }
    } catch (Exception e) {
      log.error("Error closing SQS client", e);
    }
  }
}
