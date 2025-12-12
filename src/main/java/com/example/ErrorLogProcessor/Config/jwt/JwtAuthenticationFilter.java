package com.example.ErrorLogProcessor.Config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI(); 
        String method = request.getMethod(); 

        List<String> excludeSpecificPaths = Arrays.asList(
                "/api/auth/login",
                "/api/auth/userjoin" 
        );
        
        boolean isTestPath = path.startsWith("/api/test/"); // /api/test/ 로 시작하는 모든 경로
        
        // excludeSpecificPaths 목록에 있는 URL 중 하나와 현재 요청 경로가 정확히 일치하는지 확인
        boolean isPermitAllAuthPath = excludeSpecificPaths.stream()
                                  .anyMatch(url -> path.equals(url));

        boolean isOptionsRequest = HttpMethod.OPTIONS.matches(method); // CORS Preflight 요청

        // JWT 필터가 건너뛰어야 할 조건: 인증/테스트 permitAll() 경로이거나 OPTIONS 요청일 때
        return isPermitAllAuthPath || isTestPath || isOptionsRequest; 
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String jwt = getJwtFromRequest(request);

        if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
            String loginId = jwtTokenProvider.getLoginIdFromToken(jwt);
            
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(loginId); 

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}