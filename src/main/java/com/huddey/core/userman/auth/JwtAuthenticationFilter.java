package com.huddey.core.userman.auth;

import static com.huddey.core.userman.utils.RequestUtil.determineClientType;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    logout(request, response, filterChain);

    try {
      String clientType = determineClientType(request);
      String token = extractJwtFromRequest(request, clientType);
      if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
        String username = jwtTokenProvider.getUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
    } catch (Exception ex) {
      log.error("Could not set user authentication in security context", ex);
    }
    log.debug("Completed processing request: {}", request.getRequestURI());
    filterChain.doFilter(request, response);
  }

  private static void logout(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    if (request.getParameter("off") != null) {
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

        filterChain.doFilter(request, response);
      } else {
        log.debug("Client type is mobile");
        response.setHeader("Authorization", null);
        filterChain.doFilter(request, response);
      }
    }
  }

  private String extractJwtFromRequest(HttpServletRequest request, String clientType) {
    if (clientType.equals("web")) {
      log.debug("Client type is web");
      return extractJwtFromCookie(request);
    } else {
      log.debug("Client type is mobile");
      return extractJwtFromHeader(request);
    }
  }

  private String extractJwtFromCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals("access_token")) {
          return cookie.getValue();
          /*if (cookie.getSecure() && cookie.isHttpOnly()) {
            return cookie.getValue();
          }
          log.warn("Found access_token cookie without proper security flags");*/
        }
      }
    }
    return null;
  }

  private String extractJwtFromHeader(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      log.debug("Found Bearer token in Authorization header");
      return bearerToken.substring(7);
    }
    return null;
  }
}
