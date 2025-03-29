package com.huddey.core.userman;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.huddey.core.userman.config.TestConfig;

@SpringBootTest
@ActiveProfiles("test")
class UsermanApplicationTests extends TestConfig {}
