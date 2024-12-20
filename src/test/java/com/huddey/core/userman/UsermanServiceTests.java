package com.huddey.core.userman;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.huddey.core.userman.controller.AuthController;

@SpringBootTest
@ActiveProfiles("test")
class UsermanServiceTests {

  @Autowired AuthController authController;

  @Test
  void contextLoads() {
    assertNotNull(authController);
  }
}
