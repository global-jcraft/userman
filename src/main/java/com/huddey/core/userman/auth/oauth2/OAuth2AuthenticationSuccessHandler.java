package com.huddey.core.userman.auth.oauth2;

import static com.huddey.core.userman.utils.RequestUtil.determineClientType;
import static com.huddey.core.userman.utils.RequestUtil.getLoginResponse;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huddey.core.userman.auth.JwtTokenProvider;
import com.huddey.core.userman.data.SecurityUser;
import com.huddey.core.userman.data.dto.response.LoginResponse;
import com.huddey.core.userman.data.entity.User;
import com.huddey.core.userman.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtTokenProvider tokenProvider;
  private final UserRepository userRepository;

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
      user =
          userRepository
              .findByEmail(email)
              .orElseThrow(
                  () -> new UsernameNotFoundException("User not found with email: " + email));
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
