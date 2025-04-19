package com.huddey.userman.utils;

import static com.huddey.userman.constants.UsermanConstants.*;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.huddey.userman.auth.JwtTokenProvider;
import com.huddey.userman.data.SecurityUser;
import com.huddey.userman.data.dto.response.LoginResponse;
import com.huddey.userman.data.entity.User;
import com.huddey.userman.mapper.UserMapper;
import com.huddey.userman.security.token.MobileTokenGenerationStrategy;
import com.huddey.userman.security.token.TokenGenerationStrategy;
import com.huddey.userman.security.token.WebTokenGenerationStrategy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class RequestUtils {
  public static String getClientIp() {
    ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attributes != null) {
      HttpServletRequest request = attributes.getRequest();
      String ip = request.getHeader("X-Forwarded-For");
      if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
        ip = request.getHeader("Proxy-Client-IP");
      }
      if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
        ip = request.getHeader("WL-Proxy-Client-IP");
      }
      if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
        ip = request.getRemoteAddr();
      }
      return ip;
    }
    return "unknown";
  }

  public static String determineClientType(HttpServletRequest request) {
    String userAgent = request.getHeader("User-Agent");
    if (userAgent != null
        && (userAgent.equalsIgnoreCase(ANDROID_MOBILE_CLIENT_TYPE)
            || userAgent.equalsIgnoreCase((IOS_MOBILE_CLIENT_TYPE)))) {
      return MOBILE_CLIENT_TYPE;
    }
    return WEB_CLIENT_TYPE;
  }

  public static LoginResponse getLoginResponse(
      HttpServletResponse servletResponse,
      String clientType,
      SecurityUser securityUser,
      User user,
      JwtTokenProvider jwtTokenProvider,
      boolean rememberMe) {
    TokenGenerationStrategy tokenGenerationStrategy;
    if (clientType.equalsIgnoreCase(WEB_CLIENT_TYPE)) {
      log.debug("Client type is web");
      tokenGenerationStrategy = new WebTokenGenerationStrategy(jwtTokenProvider);
      tokenGenerationStrategy.generateAndSetToken(servletResponse, securityUser, rememberMe);
      return LoginResponse.builder()
          .user(UserMapper.toDto(user))
          .expiresIn(jwtTokenProvider.getAccessTokenValidity())
          .build();
    } else {
      log.debug("Client type is mobile");
      tokenGenerationStrategy = new MobileTokenGenerationStrategy(jwtTokenProvider);
      tokenGenerationStrategy.generateAndSetToken(servletResponse, securityUser, rememberMe);
      return LoginResponse.builder()
          .accessToken(tokenGenerationStrategy.getAccessToken())
          .refreshToken(tokenGenerationStrategy.getRefreshToken())
          .tokenType("Bearer")
          .expiresIn(jwtTokenProvider.getAccessTokenValidity())
          .user(UserMapper.toDto(user))
          .build();
    }
  }
}
