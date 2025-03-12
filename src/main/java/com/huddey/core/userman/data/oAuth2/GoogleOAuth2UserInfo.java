package com.huddey.core.userman.data.oAuth2;

import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GoogleOAuth2UserInfo extends OAuth2UserInfo {
  public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
    this.attributes = attributes;
    this.id = (String) attributes.get("sub");
    this.name = (String) attributes.get("name");
    this.email = (String) attributes.get("email");
    this.imageUrl = (String) attributes.get("picture");
  }
}
