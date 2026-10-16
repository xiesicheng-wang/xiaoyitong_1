package com.youlinbang.module.favorite.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youlinbang.common.exception.BusinessException;
import com.youlinbang.module.favorite.entity.Review;
import com.youlinbang.module.favorite.mapper.ReviewMapper;
import com.youlinbang.module.favorite.service.ReviewService;
import com.youlinbang.module.order.entity.Order;
import com.youlinbang.module.order.mapper.OrderMapper;
import com.youlinbang.module.user.entity.User;
import com.youlinbang.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;

    @Override
    public Review submit(Long reviewerId, Long orderId, int score, String content) {
        if (score < 1 || score > 5) {
            throw BusinessException.badRequest("评分须在 1-5 星之间");
        }
        Order o = orderMapper.selectById(orderId);
        if (o == null) {
            throw BusinessException.business("订单不存在");
        }
        if (o.getStatus() != 1) {
            throw BusinessException.business("订单完成后才能评价");
        }
        boolean isBuyer = o.getBuyerId().equals(reviewerId);
        boolean isSeller = o.getSellerId().equals(reviewerId);
        if (!isBuyer && !isSeller) {
            throw BusinessException.forbidden("只有交易双方可以评价");
        }
        Long exists = reviewMapper.selectCount(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, orderId)
                .eq(Review::getReviewerId, reviewerId));
        if (exists > 0) {
            throw BusinessException.business("该订单你已评价过");
        }
        Review r = new Review();
        r.setOrderId(orderId);
        r.setReviewerId(reviewerId);
        r.setRevieweeId(isBuyer ? o.getSellerId() : o.getBuyerId());
        r.setTargetType(o.getOrderType());
        r.setTargetId("SKILL".equals(o.getOrderType()) ? o.getSkillId() : o.getProductId());
        r.setScore(score);
        r.setContent(content == null ? "" : content);
        reviewMapper.insert(r);
        log.info("交易互评: orderId={}, reviewer={}, score={}", orderId, reviewerId, score);
        return r;
    }

    @Override
    public List<Map<String, Object>> byTarget(String targetType, Long targetId) {
        List<Review> reviews = reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                .eq(Review::getTargetType, targetType)
                .eq(Review::getTargetId, targetId)
                .orderByDesc(Review::getId));
        Set<Long> reviewerIds = reviews.stream().map(Review::getReviewerId).collect(Collectors.toSet());
        Map<Long, User> users = reviewerIds.isEmpty() ? Map.of()
                : userMapper.selectBatchIds(reviewerIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        List<Map<String, Object>> list = new ArrayList<>();
        for (Review r : reviews) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", r.getId());
            item.put("score", r.getScore());
            item.put("content", r.getContent());
            item.put("createTime", r.getCreateTime());
            User u = users.get(r.getReviewerId());
            item.put("reviewerName", u == null ? "邻居" :
                    (u.getNickname() == null || u.getNickname().isBlank() ? u.getUsername() : u.getNickname()));
            list.add(item);
        }
        return list;
    }

    @Override
    public Map<String, Object> statOfUser(Long userId) {
        return reviewMapper.statOfUser(userId);
    }

    @Override
    public List<Long> reviewedOrderIds(Long userId) {
        return reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                        .eq(Review::getReviewerId, userId)
                        .select(Review::getOrderId))
                .stream().map(Review::getOrderId).toList();
    }
}
