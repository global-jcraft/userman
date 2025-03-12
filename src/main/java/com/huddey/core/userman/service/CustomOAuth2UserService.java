package com.huddey.core.userman.service;

import static com.huddey.core.userman.constants.UsermanConstants.*;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huddey.core.userman.data.SecurityUser;
import com.huddey.core.userman.data.entity.*;
import com.huddey.core.userman.data.oAuth2.FacebookOAuth2UserInfo;
import com.huddey.core.userman.data.oAuth2.GoogleOAuth2UserInfo;
import com.huddey.core.userman.data.oAuth2.OAuth2UserInfo;
import com.huddey.core.userman.repository.AuthProviderRepository;
import com.huddey.core.userman.repository.RoleRepository;
import com.huddey.core.userman.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;
  private final AuthProviderRepository authProviderRepository;
  private final RoleRepository roleRepository;
  private final ObjectMapper objectMapper;

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest)
      throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
    try {
      return processOAuth2User(oAuth2UserRequest, oAuth2User);
    } catch (Exception ex) {
      log.error("Error processing OAuth2 user", ex);
      throw new OAuth2AuthenticationException(new OAuth2Error("processing_error"), ex.getMessage());
    }
  }

  @Transactional
  protected OAuth2User processOAuth2User(
      OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
    String providerName = oAuth2UserRequest.getClientRegistration().getRegistrationId();
    OAuth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(providerName, oAuth2User.getAttributes());

    if (ObjectUtils.isEmpty(oAuth2UserInfo.getEmail())) {
      throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
    }

    Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
    User user;

    if (userOptional.isPresent()) {
      user = userOptional.get();

      // Check if user has connected with this social provider before
      boolean hasConnection =
          user.getSocialConnections().stream()
              .anyMatch(conn -> conn.getAuthProvider().getName().equals(providerName));

      if (hasConnection) {
        // Update existing connection
        updateSocialConnection(user, providerName, oAuth2UserRequest, oAuth2UserInfo);
      } else {
        // Add new social connection for existing user
        addSocialConnection(user, providerName, oAuth2UserRequest, oAuth2UserInfo);
      }

      // Update user profile data
      user.setFirstName(oAuth2UserInfo.getName());
      user.setProfilePictureUrl(oAuth2UserInfo.getImageUrl());
      user = userRepository.save(user);
    } else {
      // Register new user
      user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
    }

    return SecurityUser.createOauthSecurityUser(user, oAuth2User.getAttributes());
  }

  private OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
    return switch (registrationId.toLowerCase()) {
      case SOCIAL_PROVIDER_GOOGLE -> new GoogleOAuth2UserInfo(attributes);
      case SOCIAL_PROVIDER_FACEBOOK -> new FacebookOAuth2UserInfo(attributes);
      default ->
          throw new OAuth2AuthenticationException(
              "Sorry! Login with " + registrationId + " is not supported yet.");
    };
  }

  @Transactional
  protected User registerNewUser(
      OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
    String providerName = oAuth2UserRequest.getClientRegistration().getRegistrationId();

    // Create new user
    User user =
        User.builder()
            .email(oAuth2UserInfo.getEmail())
            .firstName(oAuth2UserInfo.getName())
            .profilePictureUrl(oAuth2UserInfo.getImageUrl())
            .status(UserStatus.ACTIVE)
            .emailVerified(true)
            .build();

    // Add default USER role
    Role userRole =
        roleRepository
            .findByName(ROLE_USER)
            .orElseThrow(() -> new OAuth2AuthenticationException("Default role not found"));
    user.getRoles().add(userRole);

    // Save user first to generate ID
    user = userRepository.save(user);

    // Add social connection
    addSocialConnection(user, providerName, oAuth2UserRequest, oAuth2UserInfo);

    return userRepository.save(user);
  }

  @Transactional
  protected void addSocialConnection(
      User user,
      String providerName,
      OAuth2UserRequest oAuth2UserRequest,
      OAuth2UserInfo oAuth2UserInfo) {
    // Get the auth provider
    AuthProvider authProvider =
        authProviderRepository
            .findByName(providerName)
            .orElseThrow(() -> new OAuth2AuthenticationException("Auth provider not found"));

    // Create social connection
    SocialConnection socialConnection =
        SocialConnection.builder()
            .user(user)
            .authProvider(authProvider)
            .providerUserId(oAuth2UserInfo.getId())
            .providerEmail(oAuth2UserInfo.getEmail())
            .accessToken(oAuth2UserRequest.getAccessToken().getTokenValue())
            .build();

    // Set expiration if available
    if (oAuth2UserRequest.getAccessToken().getExpiresAt() != null) {
      socialConnection.setTokenExpiresAt(
          OffsetDateTime.from(oAuth2UserRequest.getAccessToken().getExpiresAt()));
    }

    // Store OAuth attributes as JSON
    try {
      socialConnection.setProviderRawData(
          objectMapper.writeValueAsString(oAuth2UserInfo.getAttributes()));
    } catch (JsonProcessingException e) {
      log.error("Error converting OAuth attributes to JSON", e);
    }

    user.getSocialConnections().add(socialConnection);
  }

  @Transactional
  protected void updateSocialConnection(
      User user,
      String providerName,
      OAuth2UserRequest oAuth2UserRequest,
      OAuth2UserInfo oAuth2UserInfo) {
    user.getSocialConnections().stream()
        .filter(conn -> conn.getAuthProvider().getName().equals(providerName))
        .findFirst()
        .ifPresent(
            conn -> {
              // Update token information
              conn.setAccessToken(oAuth2UserRequest.getAccessToken().getTokenValue());

              // Update expiration if available
              if (oAuth2UserRequest.getAccessToken().getExpiresAt() != null) {
                conn.setTokenExpiresAt(
                    OffsetDateTime.from(oAuth2UserRequest.getAccessToken().getExpiresAt()));
              }

              // Update provider data
              try {
                conn.setProviderRawData(
                    objectMapper.writeValueAsString(oAuth2UserInfo.getAttributes()));
              } catch (JsonProcessingException e) {
                log.error("Error converting OAuth attributes to JSON", e);
              }
            });
  }
}
