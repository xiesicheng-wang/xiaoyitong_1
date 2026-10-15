package com.youlinbang.module.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包流水（每一笔资金变动都留痕，对账可溯）
 */
@Data
@TableName("t_wallet_log")
public class WalletLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** RECHARGE/FREEZE/SETTLE/REFUND/PENALTY */
    private String type;

    private BigDecimal amount;

    private BigDecimal balanceAfter;

    private BigDecimal frozenAfter;

    private Long orderId;

    private String remark;

    private LocalDateTime createTime;
}
