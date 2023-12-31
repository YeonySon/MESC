package com.ksol.mes.global.config.jwt;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtTokenProvider jwtTokenProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain chain) throws ServletException, IOException {

		// Request Header 에서 JWT 토큰 추출 - token null => 로그아웃
		String token = jwtTokenProvider.resolveToken(request);
		if (token == null) {
			chain.doFilter(request, response);
			return;
		}

		// 토큰 검증 -> 유효한 경우 : Authentication 객체 SecurityContext 에 저장
		if (jwtTokenProvider.validateToken(token)) {
			// 토큰 재발급 요청 path인 경우 예외 처리
			if (!request.getRequestURI().equals("/api/mes/user/reissue"))
				setSecurityContextHolder(token);
		}
		chain.doFilter(request, response);
	}

	private void setSecurityContextHolder(String token) {
		Authentication authentication = jwtTokenProvider.getAuthentication(token);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
}
