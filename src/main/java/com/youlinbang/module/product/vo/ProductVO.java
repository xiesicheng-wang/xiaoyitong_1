package com.youlinbang.module.product.vo;

import com.youlinbang.module.product.entity.Product;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品视图
 */
@Data
public class ProductVO {

    private Long id;
    private Long userId;
    /** 卖家昵称（列表/详情展示用） */
    private String sellerName;
    private String title;
    private String description;
    private BigDecimal price;
    private List<String> images;
    private String category;
    private Integer status;
    private Integer viewCount;
    private LocalDateTime createTime;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static ProductVO from(Product p) {
        ProductVO vo = new ProductVO();
        vo.setId(p.getId());
        vo.setUserId(p.getUserId());
        vo.setTitle(p.getTitle());
        vo.setDescription(p.getDescription());
        vo.setPrice(p.getPrice());
        vo.setImages(parseImages(p.getImages()));
        vo.setCategory(p.getCategory());
        vo.setStatus(p.getStatus());
        vo.setViewCount(p.getViewCount());
        vo.setCreateTime(p.getCreateTime());
        return vo;
    }

    private static List<String> parseImages(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return MAPPER.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
