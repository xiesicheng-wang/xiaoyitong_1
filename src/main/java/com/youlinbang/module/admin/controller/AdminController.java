package com.youlinbang.module.admin.controller;

import com.youlinbang.common.result.PageResult;
import com.youlinbang.common.result.Result;
import com.youlinbang.module.admin.service.AdminService;
import com.youlinbang.module.admin.vo.AdminOrderVO;
import com.youlinbang.module.admin.vo.AdminProductVO;
import com.youlinbang.module.admin.vo.AdminUserVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 管理端接口（/api/admin/** 由 AdminInterceptor 统一鉴权 role=1）
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.ok(adminService.stats());
    }

    @GetMapping("/users")
    public Result<PageResult<AdminUserVO>> users(@RequestParam(defaultValue = "1") long page,
                                                 @RequestParam(defaultValue = "10") long size,
                                                 @RequestParam(required = false) String keyword) {
        return Result.ok(adminService.users(page, size, keyword));
    }

    @PostMapping("/users/{id}/ban")
    public Result<Void> ban(@PathVariable Long id, @RequestBody BanDTO dto, HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        adminService.banUser(operatorId, id, Boolean.TRUE.equals(dto.getBan()));
        return Result.ok();
    }

    @GetMapping("/products")
    public Result<PageResult<AdminProductVO>> products(@RequestParam(defaultValue = "1") long page,
                                                       @RequestParam(defaultValue = "10") long size,
                                                       @RequestParam(required = false) String keyword) {
        return Result.ok(adminService.products(page, size, keyword));
    }

    @PostMapping("/products/{id}/status")
    public Result<Void> toggleProduct(@PathVariable Long id, @RequestBody BanDTO dto) {
        adminService.toggleProduct(id, Boolean.TRUE.equals(dto.getOnSale()));
        return Result.ok();
    }

    @GetMapping("/orders")
    public Result<PageResult<AdminOrderVO>> orders(@RequestParam(defaultValue = "1") long page,
                                                   @RequestParam(defaultValue = "10") long size,
                                                   @RequestParam(required = false) Integer status) {
        return Result.ok(adminService.orders(page, size, status));
    }

    @Data
    public static class BanDTO {
        private Boolean ban;
        private Boolean onSale;
    }
}
