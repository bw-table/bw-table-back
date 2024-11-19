package com.zero.bwtableback.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String accessToken = getJwtFromRequest(request);
            String refreshToken = request.getHeader("refreshToken");

            if (StringUtils.hasText(accessToken)) { // 빈 문자열도 체크
                if (tokenProvider.validateAccessToken(accessToken)) {
                    // 액세스 토큰이 유효한 경우 인증 처리
                    Authentication authentication = tokenProvider.getAuthentication(accessToken);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else if (StringUtils.hasText(refreshToken) && tokenProvider.validateRefreshToken(refreshToken)) {
                    // 액세스 토큰은 만료되었지만 리프레시 토큰이 유효한 경우
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setHeader("Token-Expired", "true");
                    return;
                } else {
                    // 액세스 토큰이 유효하지 않고, 리프레시 토큰도 없거나 유효하지 않은 경우
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            } else if (StringUtils.hasText(refreshToken) && tokenProvider.validateRefreshToken(refreshToken)) {
                // 액세스 토큰은 없지만 리프레시 토큰이 유효한 경우
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setHeader("Token-Expired", "true");
                return;
            } else {
                // 액세스 토큰과 리프레시 토큰 모두 없는 경우
                // 인증이 필요한 엔드포인트인지 확인
                if (isSecuredEndpoint(request)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isSecuredEndpoint(HttpServletRequest request) {
        // FIXME 인증이 필요한 엔드포인트 목록을 정의하고 확인하는 로직
        String path = request.getRequestURI();
        return path.startsWith("/api/test/test") || path.equals("/test/test");
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}