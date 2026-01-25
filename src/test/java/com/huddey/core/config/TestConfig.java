package com.huddey.core.config;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

public class TestConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  private static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
      new PostgreSQLContainer<>("postgres:14-alpine")
          .withDatabaseName("huddey_core")
          .withUsername("postgres")
          .withPassword("dev_password");

  static {
    POSTGRES_CONTAINER.start();
  }

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    TestPropertyValues.of(
            "spring.datasource.url=" + POSTGRES_CONTAINER.getJdbcUrl(),
            "spring.datasource.username=" + POSTGRES_CONTAINER.getUsername(),
            "spring.datasource.password=" + POSTGRES_CONTAINER.getPassword(),
            "spring.flyway.enabled=true",
            "spring.flyway.url=" + POSTGRES_CONTAINER.getJdbcUrl(),
            "spring.flyway.user=" + POSTGRES_CONTAINER.getUsername(),
            "spring.flyway.password=" + POSTGRES_CONTAINER.getPassword())
        .applyTo(applicationContext.getEnvironment());
  }
}
