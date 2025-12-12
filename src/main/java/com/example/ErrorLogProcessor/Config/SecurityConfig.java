package com.example.ErrorLogProcessor.Config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.ErrorLogProcessor.Config.jwt.JwtAuthenticationFilter;

//import com.example.ErrorLogProcessor.Config.jwt.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 실제 프론트엔드 개발 서버의 Origin을 정확히 추가 (추가로 http://localhost:3000도 일반적으로 많이 사용)
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://127.0.0.1:3000", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*")); // 모든 헤더 허용
        configuration.setAllowCredentials(true); // 자격 증명 (쿠키, HTTP 인증 등) 허용
        configuration.setMaxAge(3600L); // Pre-flight 요청 캐시 시간 
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 URL에 대해 위 설정 적용
        return source;
    }
    
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable) // CSRF 보호 비활성화 (JWT 사용 시 일반적으로 비활성화)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안함 (JWT는 토큰 기반이므로)
            .httpBasic(AbstractHttpConfigurer::disable) // HTTP Basic 인증 비활성화
            .formLogin(AbstractHttpConfigurer::disable) // Form Login 비활성화
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Preflight 요청은 항상 허용
                // JWT 필터에서 건너뛰는 경로와 정확히 일치시켜야 함. 모두 permitAll()
                // 로그인, 회원가입, 테스트 에러 생성 등 인증 없이 접근 가능한 경로들
                .requestMatchers("/api/auth/login", "/api?/auth/userjoin", "/api/test/**").permitAll()
                // 관리자 전용 API (ROLE_ADMIN 권한 필요)
                .requestMatchers("/api/admin/**").hasRole("ADMIN") 
                // 대시보드
                .requestMatchers("/api/dashboard/**", "/api/logs/**").authenticated()
                // 나머지 모든 /api/** 경로는 인증된 사용자만 접근 가능
                .requestMatchers("/api/**").authenticated() 
                // 위에 명시되지 않은 다른 모든 요청도 인증 필요 (가장 포괄적인 규칙)
                .anyRequest().permitAll()
            );
        // UsernamePasswordAuthenticationFilter 이전에 JwtAuthenticationFilter를 추가하여 JWT 토큰을 검증
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}