package com.youlinbang.module.order.controller;

import com.youlinbang.common.result.PageResult;
import com.youlinbang.common.result.Result;
import com.youlinbang.module.order.entity.Wallet;
import com.youlinbang.module.order.entity.WalletLog;
import com.youlinbang.module.order.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    public Result<Wallet> my(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(walletService.getOrCreate(userId));
    }

    /** 模拟充值（真实环境为支付宝沙箱/微信支付回调后入账） */
    @PostMapping("/recharge")
    public Result<Wallet> recharge(@Valid @RequestBody RechargeDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(walletService.recharge(userId, dto.getAmount()));
    }

    @GetMapping("/logs")
    public Result<PageResult<WalletLog>> logs(@RequestParam(defaultValue = "1") long page,
                                              @RequestParam(defaultValue = "10") long size,
                                              HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(walletService.logs(userId, page, size));
    }

    @Data
    public static class RechargeDTO {
        @NotNull(message = "金额不能为空")
        @DecimalMin(value = "0.01", message = "金额必须大于 0")
        private BigDecimal amount;
    }
}
