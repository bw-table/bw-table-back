package com.zero.bwtableback.security.jwt;

import jakarta.servlet.FilterChain;
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
            throws IOException {
        try {
            String accessToken = getJwtFromRequest(request);
            String refreshToken = request.getHeader("refreshToken");

            if (StringUtils.hasText(accessToken)) {
                if (tokenProvider.validateAccessToken(accessToken)) {
                    Authentication authentication = tokenProvider.getAuthentication(accessToken);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else if (StringUtils.hasText(refreshToken)) {
                    try {
                        if (tokenProvider.validateRefreshToken(refreshToken)) {
                            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "ACCESS_TOKEN_EXPIRED");
                            return;
                        }
                    } catch (RuntimeException e) {
                        sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_REFRESH_TOKEN");
                        return;
                    }
                } else {
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_ACCESS_TOKEN");
                    return;
                }
            } else if (StringUtils.hasText(refreshToken)) {
                try {
                    if (tokenProvider.validateRefreshToken(refreshToken)) {
                        sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "ACCESS_TOKEN_REQUIRED");
                        return;
                    }
                } catch (RuntimeException e) {
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_REFRESH_TOKEN");
                    return;
                }
            } else if (isSecuredEndpoint(request)) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "NO_TOKEN");
                return;
            }

            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "AUTHENTICATION_ERROR");
        }
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String errorCode) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String jsonResponse = String.format("{\"error\": \"%s\"}", errorCode);
        response.getWriter().write(jsonResponse);
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