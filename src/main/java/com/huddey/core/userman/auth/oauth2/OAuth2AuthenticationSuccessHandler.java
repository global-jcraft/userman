package com.huddey.core.userman.auth.oauth2;

import static com.huddey.core.userman.constants.UsermanConstants.ROLE_USER;
import static com.huddey.core.userman.utils.RequestUtil.determineClientType;
import static com.huddey.core.userman.utils.RequestUtil.getLoginResponse;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huddey.core.userman.auth.JwtTokenProvider;
import com.huddey.core.userman.data.SecurityUser;
import com.huddey.core.userman.data.dto.response.LoginResponse;
import com.huddey.core.userman.data.entity.Role;
import com.huddey.core.userman.data.entity.User;
import com.huddey.core.userman.data.entity.UserStatus;
import com.huddey.core.userman.repository.RoleRepository;
import com.huddey.core.userman.repository.UserRepository;
import com.huddey.core.userman.utils.RequestUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtTokenProvider tokenProvider;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {

    String targetUrl = determineTargetUrl(request, response, authentication);

    if (response.isCommitted()) {
      logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
      return;
    }

    Object principal = authentication.getPrincipal();
    SecurityUser userPrincipal;
    User user;

    if (principal instanceof SecurityUser securityuser) {
      userPrincipal = securityuser;
      user = userPrincipal.getUser();
    } else if (principal instanceof OAuth2User oauth2UserInstance) {
      String email = oauth2UserInstance.getAttribute("email");
      Optional<User> userOptional = userRepository.findByEmail(email);

      if (userOptional.isPresent()) {
        user = userOptional.get();
        // Update last login information for existing users
        user.setLastLoginAt(OffsetDateTime.now());
        user.setLastLoginIp(RequestUtil.getClientIp());
        user = userRepository.save(user);
      } else {
        // Create new user for OAuth2 authentication
        user =
            User.builder()
                .email(email)
                .firstName(oauth2UserInstance.getAttribute("given_name"))
                .lastName(oauth2UserInstance.getAttribute("family_name"))
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>())
                .credentials(new HashSet<>())
                .emailVerified(true)
                .profilePictureUrl(oauth2UserInstance.getAttribute("picture"))
                .registrationIp(RequestUtil.getClientIp())
                .lastLoginAt(OffsetDateTime.now())
                .build();

        // Add default USER role
        Role userRole =
            roleRepository
                .findByName(ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("Default role not found"));
        user.getRoles().add(userRole);

        user = userRepository.save(user);
      }

      userPrincipal =
          SecurityUser.createOauthSecurityUser(user, oauth2UserInstance.getAttributes());
    } else {
      throw new IllegalArgumentException("Unsupported principal type: " + principal.getClass());
    }

    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    String clientType = determineClientType(request);
    LoginResponse loginResponse =
        getLoginResponse(response, clientType, userPrincipal, user, tokenProvider);

    new ObjectMapper().writeValue(response.getOutputStream(), loginResponse);
  }
}
