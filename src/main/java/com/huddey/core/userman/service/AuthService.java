package com.huddey.core.userman.service;

import javax.management.relation.RoleNotFoundException;

import com.huddey.core.userman.data.dto.LoginRequest;
import com.huddey.core.userman.data.dto.TokenRefreshRequest;
import com.huddey.core.userman.data.dto.UserRegistrationRequest;
import com.huddey.core.userman.data.dto.response.LoginResponse;
import com.huddey.core.userman.data.dto.response.UserRegistrationResponse;
import com.huddey.core.userman.data.dto.token.TokenRefreshResponse;
import com.huddey.core.userman.exception.UserAlreadyExistsException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

  UserRegistrationResponse register(
      UserRegistrationRequest user, HttpServletRequest request, HttpServletResponse response)
      throws RoleNotFoundException, UserAlreadyExistsException;

  LoginResponse login(LoginRequest request);

  TokenRefreshResponse refreshToken(TokenRefreshRequest request);
}
