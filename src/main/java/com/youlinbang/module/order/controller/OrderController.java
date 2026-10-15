package com.youlinbang.module.order.controller;

import com.youlinbang.common.result.PageResult;
import com.youlinbang.common.result.Result;
import com.youlinbang.module.order.service.OrderService;
import com.youlinbang.module.order.vo.OrderVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** 下单（预约服务，冻结全款） */
    @PostMapping
    public Result<OrderVO> create(@Valid @RequestBody CreateDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(orderService.create(userId, dto.getProductId()));
    }

    @GetMapping("/bought")
    public Result<PageResult<OrderVO>> bought(@RequestParam(defaultValue = "1") long page,
                                              @RequestParam(defaultValue = "10") long size,
                                              HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(orderService.bought(userId, page, size));
    }

    @GetMapping("/sold")
    public Result<PageResult<OrderVO>> sold(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "10") long size,
                                            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(orderService.sold(userId, page, size));
    }

    /** 卖家确认完成：结算 */
    @PostMapping("/{id}/complete")
    public Result<OrderVO> complete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(orderService.complete(id, userId));
    }

    /** 买家取消：退款（卖家已确认后的取消视为违约，由超时机制演示） */
    @PostMapping("/{id}/cancel")
    public Result<OrderVO> cancel(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(orderService.cancel(id, userId));
    }

    @Data
    public static class CreateDTO {
        @NotNull(message = "商品 id 不能为空")
        private Long productId;
    }
}
