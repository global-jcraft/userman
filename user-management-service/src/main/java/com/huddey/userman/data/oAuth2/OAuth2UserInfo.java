package com.huddey.userman.data.oAuth2;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2UserInfo {
  protected Map<String, Object> attributes;
  protected String id;
  protected String name;
  protected String email;
  protected String imageUrl;

  public OAuth2UserInfo(Map<String, Object> attributes) {
    this.attributes = attributes;
  }
}
