package com.huddey.core.userman.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.huddey.core.userman.auth.CustomAuthenticationEntryPoint;
import com.huddey.core.userman.auth.JwtAuthenticationFilter;
import com.huddey.core.userman.auth.oauth2.OAuth2AuthenticationFailureHandler;
import com.huddey.core.userman.auth.oauth2.OAuth2AuthenticationSuccessHandler;
import com.huddey.core.userman.service.CustomOAuth2UserService;
import com.huddey.core.userman.service.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class UsermanSecurityConfig {

  private final CustomAuthenticationEntryPoint authEntryPoint;
  private final SecurityConfig securityConfig;

  private final CustomOAuth2UserService customOAuth2UserService;
  private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
  private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      JwtAuthenticationFilter jwtAuthFilter,
      CustomUserDetailsService userDetailsService)
      throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .headers(
            headers ->
                headers
                    .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                    .xssProtection(HeadersConfigurer.XXssConfig::disable)
                    .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                    .permissionsPolicy(
                        permissions ->
                            permissions.policy("camera=(), microphone=(), geolocation=()")))
        .exceptionHandling(exc -> exc.authenticationEntryPoint(authEntryPoint))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth -> {
              auth.requestMatchers(
                      "/api/v1/auth/**",
                      "/oauth2/**",
                      "/login/oauth2/code/*",
                      "/oauth2/authorization/**",
                      "/api/v1/auth/register",
                      "/api/v1/auth/login",
                      "/api/v1/auth/verify-email/**",
                      "/api/v1/auth/forgot-password",
                      "/api/v1/auth/reset-password",
                      "/v3/api-docs/**",
                      "/swagger-ui/**",
                      "/actuator/health")
                  .permitAll();
              auth.requestMatchers("/api/v1/admin/**").hasRole("ADMIN");
              auth.anyRequest().authenticated();
            })
        .oauth2Login(
            oauth2 ->
                oauth2
                    .authorizationEndpoint(endpoint -> endpoint.baseUri("/oauth2/authorize"))
                    .redirectionEndpoint(endpoint -> endpoint.baseUri("/login/oauth2/code/*"))
                    .userInfoEndpoint(endpoint -> endpoint.userService(customOAuth2UserService))
                    .successHandler(oAuth2AuthenticationSuccessHandler)
                    .failureHandler(oAuth2AuthenticationFailureHandler))
        .authenticationProvider(authenticationProvider(userDetailsService))
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of("*")); // Configure appropriately for production
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setExposedHeaders(List.of("Authorization"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public AuthenticationProvider authenticationProvider(
      CustomUserDetailsService userDetailsService) {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(securityConfig.passwordEncoder());
    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }
}
