package com.huddey.core.userman.data;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.huddey.core.userman.data.entity.User;
import com.huddey.core.userman.data.entity.UserCredential;
import com.huddey.core.userman.data.entity.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@AllArgsConstructor
public class SecurityUser implements UserDetails, OAuth2User {
  @Getter private final User user; // Our database entity
  private final Collection<? extends GrantedAuthority> authorities;
  @Setter private Map<String, Object> attributes;

  public SecurityUser(User user) {
    this.user = user;
    // Convert our database roles to Spring Security authorities
    this.authorities =
        user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName())).toList();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    // Get password from our user credentials
    return user.getCredentials().stream()
        .filter(c -> c.getAuthProvider().getName().equals("local"))
        .map(UserCredential::getPasswordHash)
        .findFirst()
        .orElse("");
  }

  @Override
  public String getUsername() {
    return user.getEmail(); // We use email as username
  }

  @Override
  public boolean isAccountNonExpired() {
    // Account expiration logic
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    // Check if account is not suspended
    return !UserStatus.SUSPENDED.equals(user.getStatus());
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public String getName() {
    return String.valueOf(user.getId());
  }

  @Override
  public boolean isCredentialsNonExpired() {
    // Credential expiration logic
    return true;
  }

  @Override
  public boolean isEnabled() {
    // Check if user is active and email verified
    return UserStatus.PENDING.equals(user.getStatus())
        || user.isEmailVerified()
        || UserStatus.ACTIVE.equals(user.getStatus());
  }

  // Helper method for OAuth2 authentication
  public static SecurityUser createOauthSecurityUser(User user, Map<String, Object> attributes) {
    SecurityUser securityUser = new SecurityUser(user);
    securityUser.setAttributes(attributes);
    return securityUser;
  }
}
