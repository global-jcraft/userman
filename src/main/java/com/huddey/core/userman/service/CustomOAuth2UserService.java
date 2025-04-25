package com.huddey.core.userman.service;

import static com.huddey.core.userman.constants.UsermanConstants.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.huddey.core.userman.auth.oauth2.OAuthUtils;
import com.huddey.core.userman.data.SecurityUser;
import com.huddey.core.userman.data.entity.AuthProvider;
import com.huddey.core.userman.data.entity.Role;
import com.huddey.core.userman.data.entity.User;
import com.huddey.core.userman.data.entity.UserStatus;
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

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest)
      throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
    try {
      return processOAuth2User(oAuth2UserRequest, oAuth2User);
    } catch (OAuth2AuthenticationException ex) {
      log.error("OAuth2 authentication error: {}", ex.getMessage());
      throw ex;
    } catch (Exception ex) {
      String errorMessage =
          String.format(
              "Error processing OAuth2 user from provider %s: %s",
              oAuth2UserRequest.getClientRegistration().getRegistrationId(), ex.getMessage());
      log.error(errorMessage, ex);
      throw new OAuth2AuthenticationException(
          new OAuth2Error("processing_error"), errorMessage, ex);
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

      AuthProvider authProvider =
          authProviderRepository
              .findByName(providerName)
              .orElseGet(
                  () -> {
                    AuthProvider newProvider = new AuthProvider();
                    newProvider.setName(providerName);
                    newProvider.setActive(true);
                    newProvider.setConfig("{\"type\": \"oauth2\"}");
                    return authProviderRepository.save(newProvider);
                  });

      // Check if user has connected with this social provider before
      OAuthUtils.socialConnectionCheck(user, authProvider, oAuth2UserRequest, oAuth2UserInfo);

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
    log.debug("Registering new user from OAuth2 provider");
    String providerName = oAuth2UserRequest.getClientRegistration().getRegistrationId();

    // Create new user
    User user = new User();
    user.setEmail(oAuth2UserInfo.getEmail());
    user.setFirstName(oAuth2UserInfo.getName());
    user.setProfilePictureUrl(oAuth2UserInfo.getImageUrl());
    user.setStatus(UserStatus.ACTIVE);
    user.setRoles(new HashSet<>());
    user.setSocialConnections(new HashSet<>());
    user.setEmailVerified(true);

    // Add default USER role
    Role userRole =
        roleRepository
            .findByName(ROLE_USER)
            .orElseThrow(() -> new OAuth2AuthenticationException("Default role not found"));
    user.getRoles().add(userRole);

    // Save user first to generate ID
    user = userRepository.save(user);

    AuthProvider authProvider =
        authProviderRepository
            .findByName(providerName)
            .orElseGet(
                () -> {
                  AuthProvider newProvider = new AuthProvider();
                  newProvider.setName(providerName);
                  newProvider.setActive(true);
                  newProvider.setConfig("{\"type\": \"oauth2\"}");
                  return authProviderRepository.save(newProvider);
                });

    // Add social connection
    OAuthUtils.addSocialConnection(user, authProvider, oAuth2UserRequest, oAuth2UserInfo);

    return userRepository.save(user);
  }
}
