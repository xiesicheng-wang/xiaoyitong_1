package com.youlinbang.module.nearby.service;

import java.util.List;
import java.util.Map;

/**
 * 附近的人：Redis GEO 实现
 */
public interface NearbyService {

    /** 上报/刷新我的位置（GEOADD） */
    void report(Long userId, double lng, double lat);

    /** 搜索附近用户（GEOSEARCH，不含自己），返回 [{id, nickname, distanceKm}] */
    List<Map<String, Object>> nearby(Long userId, double radiusKm);

    /** 退出附近的人（清掉自己的位置） */
    void leave(Long userId);
}
