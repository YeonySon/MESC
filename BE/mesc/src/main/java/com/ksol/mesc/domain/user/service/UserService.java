package com.ksol.mesc.domain.user.service;

import com.ksol.mesc.domain.user.dto.LoginReq;
import com.ksol.mesc.domain.user.dto.LoginResponseDto;
import com.ksol.mesc.domain.user.dto.SendEmailReq;
import com.ksol.mesc.domain.user.dto.UserResponse;
import com.ksol.mesc.domain.user.entity.User;
import com.ksol.mesc.global.config.jwt.TokenInfo;

import jakarta.servlet.http.HttpServletRequest;

public interface UserService {
	LoginResponseDto forwardLoginRequest(LoginReq loginReq);

	User findByEmail(String email);

	void sendEmail(SendEmailReq sendEmailReq);

	TokenInfo recreateToken(HttpServletRequest request);

	Object selectAllUser();

	UserResponse findById(Integer userId);
}
