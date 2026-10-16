package com.youlinbang.module.nearby.service.impl;

import com.youlinbang.common.exception.BusinessException;
import com.youlinbang.module.nearby.service.NearbyService;
import com.youlinbang.module.user.entity.User;
import com.youlinbang.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 附近的人：Redis GEO（GEOADD + GEOSEARCH）
 *
 * <p>演进点：位置加 TTL 定期过期（当前简化为常驻），
 * 生产可改 "GEO + ZSET 时间戳" 双结构过滤陈旧位置。</p>
 */
@Service
@RequiredArgsConstructor
public class NearbyServiceImpl implements NearbyService {

    private static final String KEY = "youlinbang:nearby:geo";

    private final StringRedisTemplate redisTemplate;
    private final UserMapper userMapper;

    @Override
    public void report(Long userId, double lng, double lat) {
        if (Math.abs(lat) > 90 || Math.abs(lng) > 180) {
            throw BusinessException.badRequest("经纬度范围不合法");
        }
        redisTemplate.opsForGeo().add(KEY, new Point(lng, lat), userId.toString());
    }

    @Override
    public List<Map<String, Object>> nearby(Long userId, double radiusKm) {
        // 用 GEORADIUSBYMEMBER（Redis 3.2+）而非 GEOSEARCH（6.2+）：
        // 本机 Windows Redis 为 5.0.14，两者语义等价（以自己为圆心搜半径内成员）
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo().radius(
                KEY,
                userId.toString(),
                new Distance(radiusKm, Metrics.KILOMETERS),
                RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeDistance().limit(21));
        List<Map<String, Object>> list = new ArrayList<>();
        if (results == null) {
            return list;
        }
        for (GeoResult<RedisGeoCommands.GeoLocation<String>> r : results) {
            Long id = Long.valueOf(r.getContent().getName());
            if (id.equals(userId)) {
                continue; // 跳过自己
            }
            User u = userMapper.selectById(id);
            if (u == null || (u.getStatus() != null && u.getStatus() != 0)) {
                continue;
            }
            Map<String, Object> item = new HashMap<>();
            item.put("id", id);
            item.put("nickname", u.getNickname());
            item.put("creditScore", u.getCreditScore());
            item.put("distanceKm", Math.round(r.getDistance().getValue() * 100) / 100.0);
            list.add(item);
        }
        return list;
    }

    @Override
    public void leave(Long userId) {
        redisTemplate.opsForGeo().remove(KEY, userId.toString());
    }
}
