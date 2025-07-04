package com.huddey.core.userman.config;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class VirtualThreadConfig {

  /** Configure Tomcat to use virtual threads for request processing */
  @Bean
  @ConditionalOnProperty(
      value = "spring.threads.virtual.enabled",
      havingValue = "true",
      matchIfMissing = false)
  public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
    return protocolHandler -> {
      protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    };
  }

  /** Configure async task executor to use virtual threads */
  @Bean
  @ConditionalOnProperty(
      value = "spring.threads.virtual.enabled",
      havingValue = "true",
      matchIfMissing = false)
  public AsyncTaskExecutor applicationTaskExecutor() {
    return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
  }

  /**
   * Alternative: Configure async task executor with bounded virtual threads Use this if you need
   * more control over virtual thread creation
   */
  @Bean
  @ConditionalOnProperty(
      value = "spring.threads.virtual.enabled",
      havingValue = "false",
      matchIfMissing = true)
  public AsyncTaskExecutor traditionalTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(50);
    executor.setMaxPoolSize(100);
    executor.setQueueCapacity(500);
    executor.setThreadNamePrefix("traditional-exec-");
    executor.setRejectedExecutionHandler(
        new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize();
    return executor;
  }

  /** Custom thread factory for virtual threads with naming */
  private ThreadFactory virtualThreadFactory(String prefix) {
    return Thread.ofVirtual().name(prefix, 0).factory();
  }
}
