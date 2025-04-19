package com.huddey.userman;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.huddey.userman.config.TestConfig;

@SpringBootTest
@ActiveProfiles("test")
class UsermanApplicationTests extends TestConfig {}
