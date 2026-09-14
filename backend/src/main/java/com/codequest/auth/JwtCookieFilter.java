package com.codequest.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtCookieFilter extends OncePerRequestFilter {
    public static final String COOKIE = "codequest_session";
    private final JwtService jwt;
    public JwtCookieFilter(JwtService jwt) { this.jwt = jwt; }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (COOKIE.equals(cookie.getName())) {
                    try {
                        var authentication = new UsernamePasswordAuthenticationToken(jwt.userId(cookie.getValue()), null, List.of());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } catch (JwtException | IllegalArgumentException ignored) {
                        SecurityContextHolder.clearContext();
                    }
                    break;
                }
            }
        }
        chain.doFilter(request, response);
    }
}
