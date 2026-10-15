package com.youlinbang.common.jwt;

import com.youlinbang.common.exception.BusinessException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 鉴权拦截器：
 * 1. 校验 JWT 签名与有效期
 * 2. 校验 Redis 会话仍有效（登出/踢下线即时生效）
 * 3. 将 userId 放入 request attribute，供 controller 使用
 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private static final String TOKEN_KEY = "auth:token:";

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw BusinessException.unauthorized("未登录");
        }
        String token = auth.substring(7);
        Claims claims;
        try {
            claims = jwtUtil.parse(token);
        } catch (Exception e) {
            throw BusinessException.unauthorized("登录已过期，请重新登录");
        }
        Long userId = Long.valueOf(claims.getSubject());
        String session = redisTemplate.opsForValue().get(TOKEN_KEY + userId);
        if (session == null || !session.equals(token)) {
            throw BusinessException.unauthorized("登录已过期，请重新登录");
        }
        request.setAttribute("userId", userId);
        request.setAttribute("username", claims.get("username", String.class));
        return true;
    }
}
