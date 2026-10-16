package com.youlinbang.module.favorite.controller;

import com.youlinbang.common.result.Result;
import com.youlinbang.module.favorite.service.FavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /** 收藏 / 取消收藏（幂等切换） */
    @PostMapping("/{type}/{targetId}/toggle")
    public Result<Map<String, Object>> toggle(@PathVariable String type, @PathVariable Long targetId,
                                              HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean favorited = favoriteService.toggle(userId, type.toUpperCase(), targetId);
        return Result.ok(Map.of("favorited", favorited));
    }

    @GetMapping("/{type}/{targetId}/status")
    public Result<Map<String, Object>> status(@PathVariable String type, @PathVariable Long targetId,
                                              HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(Map.of("favorited",
                favoriteService.isFavorited(userId, type.toUpperCase(), targetId)));
    }

    /** 我的收藏（type 可选过滤） */
    @GetMapping("/my")
    public Result<List<Map<String, Object>>> my(@RequestParam(required = false) String type,
                                                HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(favoriteService.myFavorites(userId, type));
    }
}
