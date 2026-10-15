package com.youlinbang.module.order.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包（与用户一对一，user_id 即主键）
 */
@Data
@TableName("t_wallet")
public class Wallet {

    @TableId
    private Long userId;

    private BigDecimal balance;

    private BigDecimal frozen;

    private LocalDateTime updateTime;
}
