package com.youlinbang.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 签发与校验
 *
 * <p>载荷约定：subject = userId，claim "username" = 用户名。
 * 无状态令牌 + Redis 会话双重校验：JWT 验签名，Redis 验会话是否仍有效（支持主动踢下线）。</p>
 */
@Component
public class JwtUtil {

    @Value("${youlinbang.jwt.secret}")
    private String secret;

    @Value("${youlinbang.jwt.expire-hours}")
    private long expireHours;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String create(Long userId, String username) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireHours * 3600_000L))
                .signWith(key())
                .compact();
    }

    /**
     * 校验签名与有效期，返回载荷；非法令牌抛出 JwtException
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
