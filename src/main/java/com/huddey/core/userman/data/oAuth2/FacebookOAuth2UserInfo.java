package com.huddey.core.userman.data.oAuth2;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FacebookOAuth2UserInfo extends OAuth2UserInfo {
  public FacebookOAuth2UserInfo(Map<String, Object> attributes) {
    this.attributes = attributes;
    this.id = (String) attributes.get("id");
    this.name = (String) attributes.get("name");
    this.email = (String) attributes.get("email");
    this.imageUrl = extractImageUrl(attributes);
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
