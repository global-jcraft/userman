package com.huddey.main.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@EnableJpaRepositories(basePackages = {"com.huddey.userman.repository"})
@EntityScan(basePackages = "com.huddey.userman.data.entity")
@ComponentScan(basePackages = {"com.huddey.userman", "com.huddey.userman.auth"})
public class BootstrapApplication {

  public static void main(String[] args) {
    SpringApplication.run(BootstrapApplication.class, args);
  }
}
