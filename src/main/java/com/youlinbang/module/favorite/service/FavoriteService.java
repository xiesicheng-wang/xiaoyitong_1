package com.youlinbang.module.favorite.service;

import java.util.List;
import java.util.Map;

public interface FavoriteService {

    /** 收藏/取消收藏（幂等切换），返回切换后状态 */
    boolean toggle(Long userId, String targetType, Long targetId);

    /** 是否已收藏 */
    boolean isFavorited(Long userId, String targetType, Long targetId);

    /** 我的收藏列表（带标题/价格） */
    List<Map<String, Object>> myFavorites(Long userId, String targetType);
}
