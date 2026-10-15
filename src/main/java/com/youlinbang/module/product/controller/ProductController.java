package com.youlinbang.module.product.controller;

import com.youlinbang.common.result.PageResult;
import com.youlinbang.common.result.Result;
import com.youlinbang.module.product.dto.ProductCreateDTO;
import com.youlinbang.module.product.service.ProductService;
import com.youlinbang.module.product.vo.ProductVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 二手商品接口
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /** 发布商品（需登录） */
    @PostMapping
    public Result<ProductVO> create(@Valid @RequestBody ProductCreateDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(productService.create(userId, dto));
    }

    /** 商品列表（分页 + 关键词 + 分类） */
    @GetMapping
    public Result<PageResult<ProductVO>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return Result.ok(productService.page(page, size, keyword, category));
    }

    /** 商品详情 */
    @GetMapping("/{id}")
    public Result<ProductVO> detail(@PathVariable Long id) {
        return Result.ok(productService.detail(id));
    }
}
