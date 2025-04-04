package com.huddey.core.userman.auth.oauth2;

import static com.huddey.core.userman.constants.UsermanConstants.ROLE_USER;
import static com.huddey.core.userman.utils.RequestUtils.determineClientType;
import static com.huddey.core.userman.utils.RequestUtils.getLoginResponse;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huddey.core.userman.auth.JwtTokenProvider;
import com.huddey.core.userman.data.SecurityUser;
import com.huddey.core.userman.data.dto.response.LoginResponse;
import com.huddey.core.userman.data.entity.AuthProvider;
import com.huddey.core.userman.data.entity.Role;
import com.huddey.core.userman.data.entity.User;
import com.huddey.core.userman.data.entity.UserStatus;
import com.huddey.core.userman.data.oAuth2.OAuth2UserInfo;
import com.huddey.core.userman.repository.AuthProviderRepository;
import com.huddey.core.userman.repository.RoleRepository;
import com.huddey.core.userman.repository.UserRepository;
import com.huddey.core.userman.utils.RequestUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtTokenProvider tokenProvider;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final OAuth2AuthorizedClientService authorizedClientService;
  private final AuthProviderRepository authProviderRepository;

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

    if (principal instanceof OAuth2User oauth2UserInstance) {
      String email = oauth2UserInstance.getAttribute("email");
      Optional<User> userOptional = userRepository.findByEmail(email);

      if (userOptional.isPresent()) {
        user = userOptional.get();
        // Update last login information for existing users
        user.setLastLoginAt(OffsetDateTime.now());
        user.setLastLoginIp(RequestUtils.getClientIp());
        user = userRepository.save(user);
      } else {
        // Create new user for OAuth2 authentication
        user =
            User.builder()
                .email(email)
                .firstName(oauth2UserInstance.getAttribute("name"))
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>())
                .credentials(new HashSet<>())
                .socialConnections(new HashSet<>())
                .emailVerified(true)
                .profilePictureUrl(oauth2UserInstance.getAttribute("picture"))
                .registrationIp(RequestUtils.getClientIp())
                .lastLoginAt(OffsetDateTime.now())
                .build();

        // Add default USER role
        Role userRole =
            roleRepository
                .findByName(ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("Default role not found"));
        user.getRoles().add(userRole);
        user = userRepository.save(user);
        // add social connection check
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String providerType = oauthToken.getAuthorizedClientRegistrationId();

        // Get the authorized client
        OAuth2AuthorizedClient authorizedClient =
            authorizedClientService.loadAuthorizedClient(providerType, oauthToken.getName());

        // Create OAuth2UserRequest
        OAuth2UserRequest oAuth2UserRequest =
            new OAuth2UserRequest(
                authorizedClient.getClientRegistration(), authorizedClient.getAccessToken());

        // Create OAuth2UserInfo
        OAuth2UserInfo oAuth2UserInfo = new OAuth2UserInfo(oauth2UserInstance.getAttributes());
        AuthProvider authProvider =
            authProviderRepository
                .findByName(providerType)
                .orElseGet(
                    () -> {
                      AuthProvider newProvider = new AuthProvider();
                      newProvider.setName(providerType);
                      return authProviderRepository.save(newProvider);
                    });
        // Add social connection
        OAuthUtils.socialConnectionCheck(user, authProvider, oAuth2UserRequest, oAuth2UserInfo);
        user = userRepository.save(user);
      }

      userPrincipal =
          SecurityUser.createOauthSecurityUser(user, oauth2UserInstance.getAttributes());
    } else if (principal instanceof SecurityUser securityUser) {
      userPrincipal = securityUser;
      user = userPrincipal.getUser();
    } else {
      throw new IllegalArgumentException("Unsupported principal type: " + principal.getClass());
    }

    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    String clientType = determineClientType(request);
    LoginResponse loginResponse =
        getLoginResponse(response, clientType, userPrincipal, user, tokenProvider, false);

    new ObjectMapper().writeValue(response.getOutputStream(), loginResponse);
  }
}
