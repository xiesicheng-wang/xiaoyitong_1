package com.youlinbang.module.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体（预约制服务语义：买家下单预冻结全款，履约截止未完成判买家违约）
 */
@Data
@TableName("t_order")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    /** 订单类型：PRODUCT / SKILL */
    private String orderType;

    private Long buyerId;

    private Long sellerId;

    private Long productId;

    private Long skillId;

    private BigDecimal amount;

    private BigDecimal penalty;

    /** 0进行中(冻结) 1已完成(结算) 2已取消(退款) 3违约关闭 */
    private Integer status;

    private LocalDateTime deadline;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
