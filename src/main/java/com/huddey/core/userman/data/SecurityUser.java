package com.huddey.core.userman.data;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.huddey.core.userman.data.entity.User;
import com.huddey.core.userman.data.entity.UserCredential;
import com.huddey.core.userman.data.entity.UserStatus;

import lombok.Getter;

@Getter
public class SecurityUser implements UserDetails {
  @Getter private final User user; // Our database entity
  private final Collection<? extends GrantedAuthority> authorities;

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
  public boolean isCredentialsNonExpired() {
    // Credential expiration logic
    return true;
  }

  @Override
  public boolean isEnabled() {
    // Check if user is active and email verified
    return UserStatus.ACTIVE.equals(user.getStatus()) && user.isEmailVerified();
  }
}
