package com.huddey.core.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import com.huddey.core.userman.config.I18nConfig;

import jakarta.servlet.http.HttpServletRequest;

class I18nConfigTest {

  @Test
  void messageSourceDefaultsToMessageKeyWhenNotFound() {
    I18nConfig config = new I18nConfig();
    MessageSource messageSource = config.messageSource();
    String nonExistentKey = "non.existent.key";

    String result = messageSource.getMessage(nonExistentKey, null, Locale.ENGLISH);

    assertEquals(nonExistentKey, result);
  }

  @Test
  void localeResolverHandlesNonDefaultLocale() {
    I18nConfig config = new I18nConfig();
    SessionLocaleResolver resolver = (SessionLocaleResolver) config.localeResolver();

    resolver.setDefaultLocale(Locale.FRENCH);
    Locale resolvedLocale = resolver.resolveLocale(mock(HttpServletRequest.class));

    assertEquals(Locale.FRENCH, resolvedLocale);
  }

  @Test
  void messageSourceLoadsExistingTranslations() {
    I18nConfig config = new I18nConfig();
    MessageSource messageSource = config.messageSource();
    String existingKey = "test.message";
    Object[] args = new Object[] {"test"};

    String result = messageSource.getMessage(existingKey, args, Locale.ENGLISH);

    assertNotNull(result);
    assertEquals(existingKey, result);
  }

  @Test
  void localeChangeInterceptorAcceptsCustomParamName() {
    I18nConfig config = new I18nConfig();
    LocaleChangeInterceptor interceptor = config.localeChangeInterceptor();
    String customParam = "language";

    interceptor.setParamName(customParam);

    assertEquals(customParam, interceptor.getParamName());
  }

  @Test
  void addInterceptorsRegistersLocaleChangeInterceptor() {
    I18nConfig config = new I18nConfig();
    InterceptorRegistry registry = mock(InterceptorRegistry.class);

    config.addInterceptors(registry);

    verify(registry).addInterceptor(any(LocaleChangeInterceptor.class));
  }
}
