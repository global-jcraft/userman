package com.huddey.core.userman;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yaml")
class UsermanServiceTests extends UsermanTestConfig {

  @Test
  void contextLoads() {
    // Test the context loads
  }
}
