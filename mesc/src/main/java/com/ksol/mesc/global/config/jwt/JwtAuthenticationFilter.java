package com.ksol.mesc.global.config.jwt;

import java.io.IOException;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
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
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_TYPE = "Bearer";
	private final JwtTokenProvider jwtTokenProvider;
	private final RedisTemplate<String, String> redisTemplate;

	// ThreadLocal 변수를 사용하여 AccessToken을 저장하기 위한 정적 변수
	private static final ThreadLocal<String> accessTokenThreadLocal = new ThreadLocal<>();

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain chain) throws ServletException, IOException {

		// Request Header 에서 JWT 토큰 추출 - token null => 로그아웃
		String token = resolveToken(request);
		if (token == null) {
			chain.doFilter(request, response);
			return;
		}

		// 토큰 검증 -> 유효한 경우 : Authentication 객체 SecurityContext 에 저장
		if (jwtTokenProvider.validateToken(token)) {
			setSecurityContextHolder(token, request);

			// AccessToken을 ThreadLocal 변수에 저장
			accessTokenThreadLocal.set(token);
		}

		chain.doFilter(request, response);

	}

	// Request Header 에서 토큰 정보 추출
	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_TYPE)) {
			return bearerToken.substring(7);
		}
		return null;
	}

	private void setSecurityContextHolder(String token, HttpServletRequest request) {
		Authentication authentication = jwtTokenProvider.getAuthentication(token, request);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	// AccessToken을 반환하는 메소드
	public String getAccessToken() {
		return accessTokenThreadLocal.get();
	}
}
