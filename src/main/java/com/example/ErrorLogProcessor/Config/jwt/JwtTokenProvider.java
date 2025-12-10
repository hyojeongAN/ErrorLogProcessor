package com.example.ErrorLogProcessor.Config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.logging.Logger;

@Component // 스프링 빈으로 등록
public class JwtTokenProvider {

    private static final Logger logger = Logger.getLogger(JwtTokenProvider.class.getName());

    @Value("${jwt.secret}") // application.yml에서 secret 키를 주입
    private String secretKey;

    @Value("${jwt.expiration-ms}") // application.yml에서 만료 시간을 주입
    private long expirationMs; // 밀리초 단위

    private Key key; // JWT 서명에 사용할 키

    // 객체 생성 및 의존성 주입 후 초기화 작업을 수행
    @PostConstruct
    public void init() {
        // base64로 인코딩된 문자열 형태의 secretKey를 Key 객체로 변환
        // Keys.hmacShaKeyFor()는 바이트 배열로부터 Key를 생성
    	byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * JWT 토큰 생성
     * @param loginId 토큰에 포함할 사용자 로그인 아이디
     * @return 생성된 JWT 토큰 문자열
     */
    public String generateToken(String loginId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs); // 현재 시간 + 만료 시간

        return Jwts.builder()
                .setSubject(loginId) // 토큰의 주체 (여기서는 로그인 아이디)
                .setIssuedAt(now)    // 발행 시간
                .setExpiration(expiryDate) // 만료 시간
                .signWith(key, SignatureAlgorithm.HS512) // 서명에 사용할 키와 알고리즘
                .compact(); // 토큰 압축 및 문자열로 반환
    }

    /**
     * JWT 토큰에서 사용자 로그인 아이디 추출
     * @param token JWT 토큰
     * @return 사용자 로그인 아이디
     */
    public String getLoginIdFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key) // 토큰 서명 검증에 사용할 키
                .build()
                .parseClaimsJws(token) // 토큰 파싱
                .getBody()             // 토큰 본문 (Payload)
                .getSubject();         // 주체 (로그인 아이디) 추출
    }

    /**
     * JWT 토큰 유효성 검증
     * @param token JWT 토큰
     * @return 토큰이 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true; // 유효한 토큰
        } catch (io.jsonwebtoken.security.SignatureException ex) {
            logger.warning("Invalid JWT signature"); // 잘못된 JWT 서명
        } catch (MalformedJwtException ex) {
            logger.warning("Invalid JWT token"); // 유효하지 않은 JWT 토큰
        } catch (ExpiredJwtException ex) {
            logger.warning("Expired JWT token"); // 만료된 JWT 토큰
        } catch (UnsupportedJwtException ex) {
            logger.warning("Unsupported JWT token"); // 지원되지 않는 JWT 토큰
        } catch (IllegalArgumentException ex) {
            logger.warning("JWT claims string is empty."); // JWT 토큰 비어있음
        }
        return false; // 유효하지 않은 토큰
    }
}