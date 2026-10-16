package com.youlinbang.module.nearby.controller;

import com.youlinbang.common.result.Result;
import com.youlinbang.module.nearby.service.NearbyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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

/**
 * 附近的人（需登录）
 */
@RestController
@RequestMapping("/api/nearby")
@RequiredArgsConstructor
public class NearbyController {

    private final NearbyService nearbyService;

    /** 上报我的位置（浏览器 Geolocation 获取） */
    @PostMapping("/report")
    public Result<Void> report(@RequestBody LocationDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        nearbyService.report(userId, dto.getLng(), dto.getLat());
        return Result.ok();
    }

    /** 附近的人（默认 2km） */
    @GetMapping
    public Result<List<Map<String, Object>>> nearby(@RequestParam(defaultValue = "2") double radiusKm,
                                                    HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(nearbyService.nearby(userId, radiusKm));
    }

    /** 隐身：清掉我的位置 */
    @PostMapping("/leave")
    public Result<Void> leave(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        nearbyService.leave(userId);
        return Result.ok();
    }

    @Data
    public static class LocationDTO {
        @NotNull(message = "经度不能为空")
        @DecimalMin(value = "-180", message = "经度不合法")
        @DecimalMax(value = "180", message = "经度不合法")
        private Double lng;

        @NotNull(message = "纬度不能为空")
        @DecimalMin(value = "-90", message = "纬度不合法")
        @DecimalMax(value = "90", message = "纬度不合法")
        private Double lat;
    }
}
