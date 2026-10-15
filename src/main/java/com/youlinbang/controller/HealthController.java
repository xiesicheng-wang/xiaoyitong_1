package com.youlinbang.controller;

import com.youlinbang.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 健康检查：部署后最先验证的接口
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public Result<Map<String, Object>> health() {
        return Result.ok(Map.of(
                "app", "youlinbang",
                "status", "UP",
                "time", LocalDateTime.now().toString()
        ));
    }
}
