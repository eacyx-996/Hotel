package com.example.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.backend.common.Result;
import com.example.backend.utils.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        // 放行白名单
        if (isWhitelisted(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 从 Header 中获取 token
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (jwtUtil.validateToken(token)) {
                    Claims claims = jwtUtil.parseToken(token);
                    UserContext.setUserId(claims.get("userId", Long.class));
                    UserContext.setUsername(claims.get("username", String.class));
                    UserContext.setRole(claims.get("role", String.class));
                    try {
                        filterChain.doFilter(request, response);
                    } finally {
                        UserContext.clear();
                    }
                    return;
                }
            } catch (Exception e) {
                log.warn("JWT 解析失败: {}", e.getMessage());
            }
        }

        // 返回未授权
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                Result.error(401, "未登录或登录已过期")
        ));
    }

    private boolean isWhitelisted(String uri) {
        return uri.contains("/auth/login")
                || uri.contains("/auth/register")
                || uri.contains("/actuator")
                || uri.endsWith("/error");
    }
}
