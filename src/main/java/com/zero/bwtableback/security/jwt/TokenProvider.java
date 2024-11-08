package com.zero.bwtableback.security.jwt;

import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
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

    /**
     * 요청에서 JWT 토큰을 추출합니다.
     */
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization"); // Authorization 헤더에서 토큰 추출
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer "를 제거하고 토큰 반환
        }
        return null; // 유효한 토큰이 없으면 null 반환
    }

    /**
     * JWT 토큰의 유효성을 검증합니다.
     *
     * 유효한 경우 true
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            // 만료된 토큰인 경우
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException e) {
            // JWT 관련 모든 다른 예외를 INVALID_TOKEN으로 처리
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * JWT 토큰에서 사용자 이름(이메일)을 추출합니다.
     *
     * 사용자 이름(이메일) 반환
     */
    public String getUsername(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}
