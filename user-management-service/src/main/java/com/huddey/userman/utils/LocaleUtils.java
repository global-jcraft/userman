package com.huddey.userman.utils;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class LocaleUtils {

  public static LocaleUtils instance;

  private final MessageSource messageSource;

  public LocaleUtils(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @PostConstruct
  private void init() {
    instance = this;
  }

  // Static method for global access
  public static String getMessage(String key, Object... args) {
    Locale locale = LocaleContextHolder.getLocale();
    return instance.messageSource.getMessage(key, args, locale);
  }
}
