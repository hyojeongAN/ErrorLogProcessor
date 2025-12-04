package com.example.ErrorLogProcessor.Config.jwt;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter  {

	private final JwtTokenProvider jwtTokenProvider;
	private final CustomUserDetailsService customUserDetailsService;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		// 1. 요청 헤더에서 JWT 토큰 추출
		String jwt = getJwtFromRequest(request);
		
		 // 2. JWT 토큰 유효성 검사 및 사용자 인증
        // 토큰이 유효하고, 현재 SecurityContextHolder에 인증 정보가 없는 경우에만 인증 처리
		if (jwt != null && jwtTokenProvider.validateToken(jwt) && SecurityContextHolder.getContext().getAuthentication() == null) {
			String loginId = jwtTokenProvider.getLoginIdFromToken(jwt); // 토큰에서 loginId 추출
			UserDetails userDetails = customUserDetailsService.loadUserByUsername(loginId); // loginId로 UserDetails 로드

			UsernamePasswordAuthenticationToken authentication = 
					new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		
		filterChain.doFilter(request, response);
	}
	
	 // HTTP 요청 헤더에서 JWT 토큰 추출 (Bearer 스킴)
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization"); // Authorization 헤더에서 토큰 가져오기
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 접두사 제거
        }
        return null;
	}
}
