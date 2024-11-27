package com.huddey.core.userman.configuration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class CustomEnvironmentPostProcessor implements EnvironmentPostProcessor {

  @Override
  public void postProcessEnvironment(
      ConfigurableEnvironment environment, SpringApplication application) {
    String activeProfile = System.getenv("SPRING_PROFILES_ACTIVE");
    if (activeProfile == null || activeProfile.isEmpty()) {
      activeProfile = "local";
    }
    Map<String, Object> map = new HashMap<>();
    map.put("spring.profiles.active", activeProfile);
    environment.getPropertySources().addFirst(new MapPropertySource("customPropertySource", map));
  }
}
