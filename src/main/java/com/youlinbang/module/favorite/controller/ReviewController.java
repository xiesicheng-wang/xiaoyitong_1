package com.youlinbang.module.favorite.controller;

import com.youlinbang.common.result.Result;
import com.youlinbang.module.favorite.entity.Review;
import com.youlinbang.module.favorite.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public Result<Review> submit(@RequestBody SubmitDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(reviewService.submit(userId, dto.getOrderId(), dto.getScore(), dto.getContent()));
    }

    @GetMapping
    public Result<List<Map<String, Object>>> byTarget(@RequestParam String targetType,
                                                      @RequestParam Long targetId) {
        return Result.ok(reviewService.byTarget(targetType.toUpperCase(), targetId));
    }

    /** 用户评分统计（平均分 + 评价数） */
    @GetMapping("/stat")
    public Result<Map<String, Object>> stat(@RequestParam Long userId) {
        return Result.ok(reviewService.statOfUser(userId));
    }

    /** 我已评价过的订单 id（前端用于隐藏评价按钮） */
    @GetMapping("/my-reviewed-ids")
    public Result<List<Long>> myReviewedIds(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(reviewService.reviewedOrderIds(userId));
    }

    @Data
    public static class SubmitDTO {
        @NotNull(message = "订单 id 不能为空")
        private Long orderId;

        @NotNull(message = "评分不能为空")
        @Min(value = 1, message = "评分最低 1 星")
        @Max(value = 5, message = "评分最高 5 星")
        private Integer score;

        private String content;
    }
}
