package com.youlinbang.module.favorite.service;

import com.youlinbang.module.favorite.entity.Review;

import java.util.List;
import java.util.Map;

public interface ReviewService {

    /** 交易完成后互评（买卖双方各一次） */
    Review submit(Long reviewerId, Long orderId, int score, String content);

    /** 按目标（商品/技能）查评价，附评价人昵称 */
    List<Map<String, Object>> byTarget(String targetType, Long targetId);

    /** 被评价人的评分统计 */
    Map<String, Object> statOfUser(Long userId);

    /** 某用户已评价过的订单 id 列表 */
    List<Long> reviewedOrderIds(Long userId);
}
