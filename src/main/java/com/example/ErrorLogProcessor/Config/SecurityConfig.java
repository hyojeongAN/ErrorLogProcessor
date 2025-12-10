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

import com.example.ErrorLogProcessor.Config.jwt.CustomUserDetailsService;
import com.example.ErrorLogProcessor.Config.jwt.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final CustomUserDetailsService customUserDetailsService;
	private final JwtAuthenticationFilter jwtAuthenticationFilter; // ✨✨ 이 필터가 다시 활성화될 거야! ✨✨
	
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	// CustomUserDetailsService와 PasswordEncoder를 사용하여 인증 처리를 위임
	@Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}
    
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
    	CorsConfiguration configuration = new CorsConfiguration();
    	configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://127.0.0.1:3000"));
    	configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    	configuration.setAllowedHeaders(List.of("*")); // 모든 헤더 허용
    	configuration.setAllowCredentials(true); // 자격 증명 허용 (쿠키, HTTP 인증 등)
    	configuration.setMaxAge(3600L); // Pre-flight 요청 캐시 시간 (초)

    	
    	UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 URL에 대해 위 설정 적용
        return source;
    }
    
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    	http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
    		.csrf(AbstractHttpConfigurer::disable)
    		.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
    		.httpBasic(AbstractHttpConfigurer::disable)
    		.formLogin(AbstractHttpConfigurer::disable)
    		.authorizeHttpRequests(authorize -> authorize
    		.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//    		.requestMatchers(HttpMethod.GET, "/api/logs", "api/logs/**").permitAll()
//    		.requestMatchers("/api/auth/**", "/test-log").permitAll()
    		.requestMatchers("/api/**").permitAll()
    		.anyRequest().authenticated());
    	
    	// UsernamePasswordAuthenticationFilter 전에 JwtAuthenticationFilter를 추가하여
        // 매 요청마다 JWT 토큰을 검증하도록 한다.
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    	
    	return http.build();
    }
}