package com.youlinbang.module.order.vo;

import com.youlinbang.module.order.entity.Order;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderVO {

    private Long id;
    private String orderNo;
    private Long buyerId;
    private Long sellerId;
    private Long productId;
    private String productTitle;
    private BigDecimal amount;
    private BigDecimal penalty;
    private Integer status;
    private LocalDateTime deadline;
    private LocalDateTime createTime;

    public static OrderVO from(Order o, String productTitle) {
        OrderVO vo = new OrderVO();
        vo.setId(o.getId());
        vo.setOrderNo(o.getOrderNo());
        vo.setBuyerId(o.getBuyerId());
        vo.setSellerId(o.getSellerId());
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
