package com.youlinbang.module.favorite.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 交易互评实体（每个已完成订单，买卖双方各可评价一次）
 */
@Data
@TableName("t_review")
public class Review {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long reviewerId;

    private Long revieweeId;

    /** PRODUCT / SKILL */
    private String targetType;

    private Long targetId;

    /** 1-5 星 */
    private Integer score;

    private String content;

    private LocalDateTime createTime;
}
