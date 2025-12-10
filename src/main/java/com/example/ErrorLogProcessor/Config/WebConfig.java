package com.example.ErrorLogProcessor.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration // 스프링 설정 클래스임을 명시
public class WebConfig implements WebMvcConfigurer { // WebMvcConfigurer 인터페이스를 구현

    @Bean // 스프링 컨테이너에 CorsFilter 빈을 등록
    CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 허용할 Origin(출처) 설정
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://127.0.0.1:3000"));

        // 모든 HTTP 메서드 허용 (GET, POST, PUT, DELETE 등)
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 모든 헤더 허용 (Authorization, Content-Type 등)
        config.setAllowedHeaders(List.of("*"));

        // 쿠키, 인증 정보 등 자격 증명 허용
        config.setAllowCredentials(true);

        // 예비 요청(preflight) 결과 캐시 시간 설정 (30분)
        config.setMaxAge(1800L); // 초 단위 (30분 = 1800초)

        // 모든 경로(/**)에 대해 위 CORS 설정 적용
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
