package com.huddey.core.userman.data.oAuth2;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FacebookOAuth2UserInfo extends OAuth2UserInfo {
  public FacebookOAuth2UserInfo(Map<String, Object> attributes) {
    if (attributes == null) {
      throw new OAuth2AuthenticationException("Facebook OAuth2 attributes cannot be null");
    }
    this.attributes = new HashMap<>(attributes);

    this.id = (String) attributes.get("id");
    if (this.id == null) {
      throw new OAuth2AuthenticationException("Facebook OAuth2 'id' attribute is required");
    }

    this.name = (String) attributes.get("name");
    if (this.name == null) {
      throw new OAuth2AuthenticationException("Facebook OAuth2 'name' attribute is required");
    }

    this.email = (String) attributes.get("email");
    if (this.email == null) {
      throw new OAuth2AuthenticationException("Facebook OAuth2 'email' attribute is required");
    }

    this.imageUrl = extractImageUrl(attributes);

    // Normalize name attributes for compatibility with success handler.
    String[] parts = this.name.split(" ", 2);
    if (parts.length > 1) {
      this.attributes.put("given_name", parts[0]);
      this.attributes.put("family_name", parts[1]);
    } else {
      this.attributes.put("given_name", this.name);
      this.attributes.put("family_name", "");
    }
  }

  @SuppressWarnings("unchecked")
  private String extractImageUrl(Map<String, Object> attributes) {
    Object picture = attributes.get("picture");
    if (!(picture instanceof Map)) {
      return null;
    }
    Map<String, Object> pictureMap = (Map<String, Object>) picture;
    Object data = pictureMap.get("data");
    if (!(data instanceof Map)) {
      return null;
    }
    Map<String, Object> dataMap = (Map<String, Object>) data;
    Object url = dataMap.get("url");
    return url != null ? url.toString() : null;
  }
}
