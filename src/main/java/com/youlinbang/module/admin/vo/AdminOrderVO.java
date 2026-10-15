package com.youlinbang.module.admin.vo;

import com.youlinbang.module.order.entity.Order;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理端订单视图（带买家/卖家用户名与商品标题）
 */
@Data
public class AdminOrderVO {

    private Long id;
    private String orderNo;
    private Long buyerId;
    private String buyerUsername;
    private Long sellerId;
    private String sellerUsername;
    private Long productId;
    private String productTitle;
    private BigDecimal amount;
    private BigDecimal penalty;
    private Integer status;
    private LocalDateTime deadline;
    private LocalDateTime createTime;

    public static AdminOrderVO from(Order o, String buyerUsername, String sellerUsername, String productTitle) {
        AdminOrderVO vo = new AdminOrderVO();
        vo.setId(o.getId());
        vo.setOrderNo(o.getOrderNo());
        vo.setBuyerId(o.getBuyerId());
        vo.setBuyerUsername(buyerUsername);
        vo.setSellerId(o.getSellerId());
        vo.setSellerUsername(sellerUsername);
        vo.setProductId(o.getProductId());
        vo.setProductTitle(productTitle);
        vo.setAmount(o.getAmount());
        vo.setPenalty(o.getPenalty());
        vo.setStatus(o.getStatus());
        vo.setDeadline(o.getDeadline());
        vo.setCreateTime(o.getCreateTime());
        return vo;
    }
}
