package com.zero.bwtableback.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT 토큰 생성 및 검증을 담당하는 클래스
 *
 * 액세스 토큰과 리프레시 토큰의 생성, 검증, 파싱 등 JWT 관련 모든 작업을 처리
 */
@Component
public class TokenProvider {
    @Value("${JWT.SECRET}")
    private String jwtSecret;

    @Value("${JWT_ACCESS_TOKEN_VALIDITY}")
    private long accessTokenValidityInMilliseconds;

    @Value("${JWT_REFRESH_TOKEN_VALIDITY}")
    private long refreshTokenValidityInMilliseconds;

    public String createAccessToken(String email) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String createRefreshToken() {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenValidityInMilliseconds);

        return Jwts.builder()
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    //TODO 토큰 검증 메소드
}
