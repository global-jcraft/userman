package com.huddey.core.notification.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
public class AwsSnsConfig {

  @Value("${aws.region}")
  private String awsRegion;

  @Bean
  @Profile("!local") // For any profile that is NOT 'local' (e.g., dev, staging, prod)
  public SnsClient snsClientProd() {
    return SnsClient.builder()
        .region(Region.of(awsRegion))
        // Uses default credentials provider chain (e.g., IAM roles, environment variables,
        // .aws/credentials)
        .build();
  }

  @Bean
  @Profile("local") // Specifically for 'local' Spring profile
  public SnsClient snsClientLocal(
      @Value("${aws.sns.localstack.endpoint}") String localstackEndpoint) {
    return SnsClient.builder()
        .region(Region.of(awsRegion)) // LocalStack still requires a region
        .endpointOverride(URI.create(localstackEndpoint))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create("test", "test"))) // Dummy credentials for LocalStack
        .build();
  }
}
