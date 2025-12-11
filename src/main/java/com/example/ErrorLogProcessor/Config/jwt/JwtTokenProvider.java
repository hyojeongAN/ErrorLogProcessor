package com.example.ErrorLogProcessor.Config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys; 
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.logging.Logger;

@Component
public class JwtTokenProvider {

    private static final Logger logger = Logger.getLogger(JwtTokenProvider.class.getName());

    @Value("${jwt.expiration-ms}") 
    private long expirationMs; 

    private Key key; 

    @PostConstruct
    public void init() {
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS512); 
        logger.info("JWT Secret Key generated using Keys.secretKeyFor(HS512),");
    }

    public String generateToken(String loginId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs); 

        return Jwts.builder()
                .setSubject(loginId) 
                .setIssuedAt(now)    
                .setExpiration(expiryDate) 
                .signWith(key, SignatureAlgorithm.HS512) 
                .compact(); 
    }

    public String getLoginIdFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key) 
                .build()
                .parseClaimsJws(token) 
                .getBody()             
                .getSubject();         
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true; 
        } catch (io.jsonwebtoken.security.SignatureException ex) {
            logger.warning("Invalid JWT signature"); 
        } catch (MalformedJwtException ex) {
            logger.warning("Invalid JWT token"); 
        } catch (ExpiredJwtException ex) {
            logger.warning("Expired JWT token"); 
        } catch (UnsupportedJwtException ex) {
            logger.warning("Unsupported JWT token"); 
        } catch (IllegalArgumentException ex) {
            logger.warning("JWT claims string is empty."); 
        }
        return false; 
    }
}