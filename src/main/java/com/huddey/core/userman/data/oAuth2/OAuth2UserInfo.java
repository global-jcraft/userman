package com.huddey.core.userman.data.oAuth2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2UserInfo {
  private String id;
  private String email;
  private String name;
  private String imageUrl;
  private String provider;
}
