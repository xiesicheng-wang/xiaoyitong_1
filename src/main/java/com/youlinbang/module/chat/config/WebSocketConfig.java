package com.youlinbang.module.chat.config;

import com.youlinbang.common.jwt.JwtUtil;
import com.youlinbang.module.chat.handler.ChatHandler;
import com.youlinbang.module.user.entity.User;
import com.youlinbang.module.user.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 配置：/ws/chat 群聊端点
 *
 * <p>浏览器原生 WebSocket API 无法携带 Authorization 头，
 * 约定握手时通过 ?token= 传 JWT，拦截器完成与 HTTP 相同的双重校验
 * （JWT 签名 + Redis 会话有效性）。</p>
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private static final String TOKEN_KEY = "auth:token:";

    private final ChatHandler chatHandler;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final UserMapper userMapper;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatHandler, "/ws/chat")
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(@NonNull ServerHttpRequest request,
                                                   @NonNull ServerHttpResponse response,
                                                   @NonNull WebSocketHandler wsHandler,
                                                   @NonNull Map<String, Object> attributes) {
                        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
                            return false;
                        }
                        String token = servletRequest.getServletRequest().getParameter("token");
                        if (token == null || token.isBlank()) {
                            return false;
                        }
                        try {
                            Claims claims = jwtUtil.parse(token);
                            Long userId = Long.valueOf(claims.getSubject());
                            String session = redisTemplate.opsForValue().get(TOKEN_KEY + userId);
                            if (session == null || !session.equals(token)) {
                                return false;
                            }
                            User user = userMapper.selectById(userId);
                            if (user == null || (user.getStatus() != null && user.getStatus() != 0)) {
                                return false;
                            }
                            attributes.put("userId", userId);
                            attributes.put("username", user.getUsername());
                            attributes.put("nickname", user.getNickname());
                            return true;
                        } catch (Exception e) {
                            return false;
                        }
                    }

                    @Override
                    public void afterHandshake(@NonNull ServerHttpRequest request,
                                               @NonNull ServerHttpResponse response,
                                               @NonNull WebSocketHandler wsHandler,
                                               Exception exception) {
                    }
                })
                .setAllowedOriginPatterns("*");
    }
}
