package com.youlinbang.module.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youlinbang.common.exception.BusinessException;
import com.youlinbang.common.result.PageResult;
import com.youlinbang.module.product.dto.ProductCreateDTO;
import com.youlinbang.module.product.entity.Product;
import com.youlinbang.module.product.mapper.ProductMapper;
import com.youlinbang.module.product.service.ProductService;
import com.youlinbang.module.product.vo.ProductVO;
import com.youlinbang.module.user.entity.User;
import com.youlinbang.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    @Override
    public ProductVO create(Long userId, ProductCreateDTO dto) {
        Product p = new Product();
        p.setUserId(userId);
        p.setTitle(dto.getTitle());
        p.setDescription(dto.getDescription() == null ? "" : dto.getDescription());
        p.setPrice(dto.getPrice());
        p.setCategory(dto.getCategory() == null || dto.getCategory().isBlank() ? "其他" : dto.getCategory());
        p.setStatus(0);
        p.setImages(toJson(dto.getImages()));
        productMapper.insert(p);
        log.info("商品发布: id={}, userId={}, title={}", p.getId(), userId, p.getTitle());
        return ProductVO.from(p);
    }

    @Override
    public PageResult<ProductVO> page(long page, long size, String keyword, String category) {
        LambdaQueryWrapper<Product> qw = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 0)
                .eq(category != null && !category.isBlank(), Product::getCategory, category)
                .and(keyword != null && !keyword.isBlank(),
                        w -> w.like(Product::getTitle, keyword).or().like(Product::getDescription, keyword))
                .orderByDesc(Product::getCreateTime);
        Page<Product> result = productMapper.selectPage(new Page<>(page, size), qw);
        Map<Long, String> names = sellerNames(result.getRecords());
        List<ProductVO> records = result.getRecords().stream()
                .map(p -> {
                    ProductVO vo = ProductVO.from(p);
                    vo.setSellerName(names.get(p.getUserId()));
                    return vo;
                }).toList();
        return new PageResult<>(records, result.getTotal(), page, size);
    }

    @Override
    public ProductVO detail(Long id) {
        Product p = productMapper.selectById(id);
        if (p == null) {
            throw BusinessException.business("商品不存在或已删除");
        }
        // 浏览量 +1（不阻塞主流程，失败忽略）
        productMapper.update(null, new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, id)
                .setSql("view_count = view_count + 1"));
        p.setViewCount(p.getViewCount() == null ? 1 : p.getViewCount() + 1);
        ProductVO vo = ProductVO.from(p);
        vo.setSellerName(sellerNames(List.of(p)).get(p.getUserId()));
        return vo;
    }

    /** 卖家昵称批量填充（避免 N+1） */
    private Map<Long, String> sellerNames(List<Product> products) {
        List<Long> ids = products.stream().map(Product::getUserId).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(User::getId,
                        u -> u.getNickname() == null || u.getNickname().isBlank() ? u.getUsername() : u.getNickname(),
                        (a, b) -> a));
    }

    private String toJson(List<String> images) {
        if (images == null || images.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(images);
        } catch (Exception e) {
            throw BusinessException.badRequest("图片列表序列化失败");
        }
    }
}
