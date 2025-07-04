package com.huddey.core.userman.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.huddey.core.userman.data.entity.User;
import com.huddey.core.userman.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCacheService {

  private final UserRepository userRepository;

  @Cacheable(value = "user-cache", key = "#email", unless = "#result == null")
  public User getUserByEmail(String email) {
    log.debug("Loading user from database for email: {}", email);
    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
  }

  @Cacheable(value = "user-cache", key = "#userId", unless = "#result == null")
  public User getUserById(Long userId) {
    log.debug("Loading user from database for ID: {}", userId);
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
  }

  @CacheEvict(value = "user-cache", key = "#email")
  public void evictUserCache(String email) {
    log.debug("Evicting cache for user: {}", email);
  }

  @CacheEvict(value = "user-cache", key = "#userId")
  public void evictUserCacheById(Long userId) {
    log.debug("Evicting cache for user ID: {}", userId);
  }

  @CacheEvict(value = "user-cache", allEntries = true)
  public void evictAllUserCache() {
    log.debug("Evicting all user cache entries");
  }
}
