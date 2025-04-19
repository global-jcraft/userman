package com.huddey.userman.auth.oauth2;

import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;

import com.huddey.userman.data.entity.AuthProvider;
import com.huddey.userman.data.entity.SocialConnection;
import com.huddey.userman.data.entity.User;
import com.huddey.userman.data.oAuth2.OAuth2UserInfo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OAuthUtils {

  private OAuthUtils() {}

  public static void socialConnectionCheck(
      User user,
      AuthProvider authProvider,
      OAuth2UserRequest oAuth2UserRequest,
      OAuth2UserInfo oAuth2UserInfo) {
    log.debug(
        "Adding social connection for user: {}, with provider name: {}",
        user.getEmail(),
        authProvider.getName());
    boolean hasConnection =
        user.getSocialConnections().stream()
            .anyMatch(conn -> conn.getAuthProvider().getName().equals(authProvider.getName()));

    if (hasConnection) {
      // Update existing connection
      log.debug(
          "Updating social connection for user: {}, with provider name: {}",
          user.getEmail(),
          authProvider.getName());
      updateSocialConnection(user, authProvider, oAuth2UserRequest, oAuth2UserInfo);
    } else {
      // Add new social connection for existing user
      log.debug(
          "Adding social connection for user: {}, with provider name: {}",
          user.getEmail(),
          authProvider.getName());
      addSocialConnection(user, authProvider, oAuth2UserRequest, oAuth2UserInfo);
    }
  }

  protected static void updateSocialConnection(
      User user,
      AuthProvider authProvider,
      OAuth2UserRequest oAuth2UserRequest,
      OAuth2UserInfo oAuth2UserInfo) {

    user.getSocialConnections().stream()
        .filter(conn -> conn.getAuthProvider().getName().equals(authProvider.getName()))
        .findFirst()
        .ifPresent(
            conn -> {
              // Update token information
              conn.setAccessToken(oAuth2UserRequest.getAccessToken().getTokenValue());

              // Update expiration if available
              if (oAuth2UserRequest.getAccessToken().getExpiresAt() != null) {
                conn.setTokenExpiresAt(
                    OffsetDateTime.ofInstant(
                        oAuth2UserRequest.getAccessToken().getExpiresAt(), ZoneId.systemDefault()));
              }

              // Update provider data
              conn.setProviderRawData(oAuth2UserInfo.getAttributes());
            });
  }

  public static void addSocialConnection(
      User user,
      AuthProvider authProvider,
      OAuth2UserRequest oAuth2UserRequest,
      OAuth2UserInfo oAuth2UserInfo) {

    // Create social connection
    SocialConnection socialConnection =
        SocialConnection.builder()
            .user(user)
            .authProvider(authProvider)
            .providerUserId(String.valueOf(user.getId()))
            .providerEmail(user.getEmail())
            .accessToken(oAuth2UserRequest.getAccessToken().getTokenValue())
            .build();

    // Set expiration if available
    if (oAuth2UserRequest.getAccessToken().getExpiresAt() != null) {
      socialConnection.setTokenExpiresAt(
          OffsetDateTime.ofInstant(
              oAuth2UserRequest.getAccessToken().getExpiresAt(), ZoneId.systemDefault()));
    }

    // Store OAuth attributes directly
    socialConnection.setProviderRawData(oAuth2UserInfo.getAttributes());

    user.getSocialConnections().add(socialConnection);
  }
}
