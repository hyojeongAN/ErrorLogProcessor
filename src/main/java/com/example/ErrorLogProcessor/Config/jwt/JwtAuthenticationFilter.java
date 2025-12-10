package com.example.ErrorLogProcessor.Config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter; // OncePerRequestFilter 임포트 확인!

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    // ✨✨✨ 토큰 검증을 건너뛸 public 경로 설정 (SecurityConfig의 permitAll() 경로와 맞춰줌!) ✨✨✨
    private static final List<String> EXCLUDE_URL_PATTERNS = Arrays.asList(
            "/api/auth/",       // 로그인, 회원가입 등 인증 관련 API
            "/api/dashboard/",  // 우리가 지금 만드는 대시보드 API (여기에 /api/dashboard/daily-error-counts 포함)
            "/favicon.ico"      // 파비콘
            // 필요한 경우 더 추가할 수 있어 (예: "/public/**")
    );

    // ✨✨✨ 요청 경로가 토큰 검증이 필요 없는 경로인지 확인하는 헬퍼 메소드 ✨✨✨
    private boolean isPublicPath(String requestURI) {
        return EXCLUDE_URL_PATTERNS.stream().anyMatch(requestURI::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI(); // 현재 요청 URI 가져오기

        // ✨✨✨✨ 토큰 검증이 필요 없는 public 경로일 경우 필터 스킵!!!! ✨✨✨✨
        // 이 로직 덕분에 /api/dashboard 로 시작하는 요청은 JWT 토큰 검증 없이 바로 통과돼!
        if (isPublicPath(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰이 필요 없는 경로가 아니라면, 아래부터 JWT 토큰 검증 로직 실행
        String jwt = getJwtFromRequest(request); // 요청 헤더에서 JWT 토큰 추출

        if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
            String loginId = jwtTokenProvider.getLoginIdFromToken(jwt); // 토큰에서 사용자 ID 추출
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(loginId); // 사용자 정보 로드

            // 인증 객체 생성
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // SecurityContext에 인증 정보 설정
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        // 토큰 검증 여부와 상관없이 다음 필터로 요청 전달
        // (단, isPublicPath()에서 이미 return 되었으므로 여기는 public 경로가 아닌 요청만 옴)
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization"); // Authorization 헤더에서 토큰 가져오기
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 접두사 제거
        }
        return null;
    }
}