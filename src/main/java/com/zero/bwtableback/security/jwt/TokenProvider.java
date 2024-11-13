package com.zero.bwtableback.security.jwt;

import com.zero.bwtableback.member.entity.Role;
import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT 토큰 생성 및 검증을 담당하는 클래스
 *
 * 액세스 토큰과 리프레시 토큰의 생성, 검증, 파싱 등 JWT 관련 모든 작업을 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenProvider {
    @Value("${JWT.SECRET}")
    private String jwtSecret;

    @Value("${JWT_ACCESS_TOKEN_VALIDITY}")
    private long accessTokenValidityInMilliseconds;

    @Value("${JWT_REFRESH_TOKEN_VALIDITY}")
    private long refreshTokenValidityInMilliseconds;

    public long getRefreshTokenValidityInMilliseconds() {
        return refreshTokenValidityInMilliseconds * 1000;
    }

    private final MemberDetailsService memberDetailsService;

    public String createAccessToken(String email, Role role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(email)
                .claim("role", role.name())
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String createRefreshToken(String email) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    /**
     * JWT 토큰으로부터 Authentication 객체 생성
     */
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        String username = claims.getSubject();

        UserDetails userDetails = memberDetailsService.loadUserByUsername(username);

        return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());//        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /**
     * 요청에서 JWT 토큰을 추출
     */
    public String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization"); // Authorization 헤더에서 토큰 추출
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer "를 제거하고 토큰 반환
        }
        return null;
    }

    /**
     * 액세스 토큰의 유효성을 검증
     */
    public boolean validateAccessToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 액세스 토큰입니다.");
        } catch (JwtException e) {
            log.error("유효하지 않은 액세스 토큰입니다: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 리프레시 토큰의 유효성을 검증
     */
    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 리프레시 토큰입니다.");
        } catch (JwtException e) {
            log.error("유효하지 않은 리프레시 토큰입니다: {}", e.getMessage());
        }
        return false;
    }

    /**
     * JWT 토큰에서 사용자 이름(이메일)을 추출
     */
    public String getUsername(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * JWT 토큰에서 역할 정보 추출
     */
    public Role getRole(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        return Role.valueOf(claims.get("role", String.class));
    }
}
