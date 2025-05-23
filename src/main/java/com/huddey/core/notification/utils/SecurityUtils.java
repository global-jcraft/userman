package com.huddey.core.notification.utils;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static com.huddey.core.userman.utils.RequestUtils.determineClientType;

@Slf4j
public class SecurityUtils {

  private SecurityUtils() {}

  public static boolean constantTimeEquals(String a, String b) {
    if (a == null || b == null) {
      return false;
    }
    byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
    byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
    return MessageDigest.isEqual(aBytes, bBytes);
  }

  public static void logout(
          HttpServletRequest request, HttpServletResponse response)
          throws IOException, ServletException {
    if (request.getParameter("off") != null && request.getParameter("off").equals("true")) {
      log.debug("Processing logout request");
      String clientType = determineClientType(request);
      if (clientType.equals("web")) {
        log.debug("Client type is web");
        Cookie accessCookie = new Cookie("access_token", null);
        accessCookie.setMaxAge(0);
        accessCookie.setPath("/");
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("refresh_token", null);
        refreshCookie.setMaxAge(0);
        refreshCookie.setPath("/");
        response.addCookie(refreshCookie);
      } else {
        log.debug("Client type is mobile");
        response.setHeader("Authorization", null);
      }
    }
  }
}
