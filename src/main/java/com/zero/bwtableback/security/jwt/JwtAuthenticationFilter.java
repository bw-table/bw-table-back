package com.zero.bwtableback.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final MemberDetailsService userDetailsService;
    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("doFilterInternal");

        String accessToken = tokenProvider.extractToken(request);

        // 토큰 존재하는 경우
        if (accessToken != null) {
            if (tokenProvider.validateToken(accessToken)) {
                // Access Token이 유효한 경우
                setAuthenticationToContext(accessToken, request);
            } else {
                // Access Token이 유효하지 않은 경우
                String refreshToken = extractRefreshToken(request);
                if (refreshToken != null && tokenProvider.validateToken(refreshToken)) {
                    String email = tokenProvider.getUsername(refreshToken);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Refresh Token");
                    return;
                } else {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or Missing Refresh Token");
                    return;
                }
            }
        }

        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    private void setAuthenticationToContext(String token, HttpServletRequest request) {
        String email = tokenProvider.getUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}