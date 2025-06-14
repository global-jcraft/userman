package com.huddey.core.notification.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

/**
 * SES client configuration split by profile: - 'local' uses LocalStack endpoint with dummy
 * credentials. - '!local' (production) uses the default provider chain (env vars, IAM roles).
 */
@Configuration
public class AwsSesConfig {
  @Value("${aws.region}")
  private String awsRegion;

  @Value("${aws.endpoint.ses:}")
  private String sesEndpoint;

  // Local SES client: only active under 'local' profile
  @Bean
  @Profile("local")
  public SesClient localSesClient() {
    return SesClient.builder()
        .region(Region.of(awsRegion))
        .endpointOverride(URI.create(sesEndpoint))
        .credentialsProvider(
            StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
        .build();
  }

  // Production SES client: active under any profile except 'local'
  @Bean
  @Profile("!local")
  public SesClient prodSesClient() {
    return SesClient.builder()
        .region(Region.of(awsRegion))
        .credentialsProvider(DefaultCredentialsProvider.create())
        .build();
  }
}
