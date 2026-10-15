package com.youlinbang.module.admin.vo;

import com.youlinbang.module.product.entity.Product;
import com.youlinbang.module.product.vo.ProductVO;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 管理端商品视图
 */
@Data
public class AdminProductVO {

    private Long id;
    private String title;
    private BigDecimal price;
    private String category;
    private Integer status;
    private Integer viewCount;
    private Long sellerId;
    private String sellerUsername;
    private String createTime;

    public static AdminProductVO from(Product p, String sellerUsername) {
        AdminProductVO vo = new AdminProductVO();
        vo.setId(p.getId());
        vo.setTitle(p.getTitle());
        vo.setPrice(p.getPrice());
        vo.setCategory(p.getCategory());
        vo.setStatus(p.getStatus());
        vo.setViewCount(p.getViewCount());
        vo.setSellerId(p.getUserId());
        vo.setSellerUsername(sellerUsername);
        vo.setCreateTime(p.getCreateTime() == null ? "" : p.getCreateTime().toString().replace('T', ' '));
        return vo;
    }
}
