package com.example.ErrorLogProcessor.Config.jwt;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

	private final Key key;
	
	// application.properties에서 secret 키를 주입받아 사용
	public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		this.key = Keys.hmacShaKeyFor(keyBytes);
	}
	
	 // 유저 정보를 받아서 JWT 토큰을 생성하는 메서드
	public String generateToken(Authentication authentication) {
		String authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(","));

		long now = (new Date()).getTime();
		Date accessTokenExpiresIn = new Date(now + 86400000); // 1일 (24시간) 토큰 유효 시간

		// Access Token 생성
		String accessToken = Jwts.builder().setSubject(authentication.getName()) // 유저 loginId
				.claim("auth", authorities) // 유저 권한 (ROLE_USER, ROLE_ADMIN 등)
				.setExpiration(accessTokenExpiresIn) // 만료 시간
				.signWith(key, SignatureAlgorithm.HS256) // 서명에 사용할 키와 알고리즘
				.compact();

		return accessToken;
	}
	
	 // JWT 토큰을 복호화하여 토큰에 들어있는 정보를 꺼내서 인증 객체 (Authentication)를 만드는 메서드
	public Authentication getAuthentication(String accessToken) {

		Claims claims = parseClaims(accessToken);
		
		if(claims.get("auth") == null) {
			throw new RuntimeException("권한 정보가 없는 토큰입니다.");
		}
		
		Collection<? extends GrantedAuthority> authorities =
				Arrays.stream(claims.get("auth").toString().split(","))
						.map(SimpleGrantedAuthority::new)
						.collect(Collectors.toList());
		
		return null;
	}
	
	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
			return true;
		} catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
			// "잘못된 JWT 서명입니다."
            // 에러 로깅은 실제 프로젝트에서 추가 (Logger.info("잘못된 JWT 서명입니다.", e);)
		} catch (ExpiredJwtException e) {
			// "만료된 JWT 토큰입니다.
		} catch (UnsupportedJwtException e) {
			// "지원되지 않는 JWT 토큰입니다."
		} catch (IllegalArgumentException e) {
			// "JWT 토큰이 잘못되었습니다."
		}
		return false;
	}
	
	private Claims parseClaims(String accessToken) {
		try {
			return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
		} catch (ExpiredJwtException e) {
			return e.getClaims();
		}
	}
	
}
