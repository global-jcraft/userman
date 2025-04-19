package com.huddey.userman.service;

import javax.management.relation.RoleNotFoundException;

import com.huddey.userman.data.dto.*;
import com.huddey.userman.data.dto.response.LoginResponse;
import com.huddey.userman.data.dto.response.UserRegistrationResponse;
import com.huddey.userman.data.dto.token.TokenRefreshResponse;
import com.huddey.userman.exception.UserAlreadyExistsException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

  UserRegistrationResponse registerBasicFlow(
      UserRegistrationBasicFlowRequest user,
      HttpServletRequest request,
      HttpServletResponse response)
      throws RoleNotFoundException, UserAlreadyExistsException;

  UserRegistrationResponse completeRegistration(
      UserRegistrationRequest user, HttpServletRequest request, HttpServletResponse response)
      throws RoleNotFoundException, UserAlreadyExistsException;

  LoginResponse login(
      LoginRequest request, HttpServletRequest servletRequest, HttpServletResponse servletResponse);

  TokenRefreshResponse refreshToken(
      RefreshTokenRequest request, HttpServletRequest servletRequest, HttpServletResponse response);

  ResetPasswordResponse resetPasswordRequest(
      ResetPasswordRequest request,
      HttpServletRequest servletRequest,
      HttpServletResponse servletResponse);

  void resetPasswordComplete(
      ResetPasswordCompleteRequest request,
      HttpServletRequest servletRequest,
      HttpServletResponse servletResponse);
}
