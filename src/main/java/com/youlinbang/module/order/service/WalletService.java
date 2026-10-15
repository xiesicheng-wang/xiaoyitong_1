package com.youlinbang.module.order.service;

import com.youlinbang.common.result.PageResult;
import com.youlinbang.module.order.entity.Wallet;
import com.youlinbang.module.order.entity.WalletLog;

import java.math.BigDecimal;

/**
 * 钱包领域服务：所有资金变动必须走这里，保证"变更 + 流水"原子性
 */
public interface WalletService {

    Wallet getOrCreate(Long userId);

    /** 模拟充值（真实环境接支付宝沙箱/微信支付回调） */
    Wallet recharge(Long userId, BigDecimal amount);

    /** 下单冻结：balance -= amount, frozen += amount */
    void freeze(Long userId, Long orderId, BigDecimal amount, String remark);

    /** 完成结算：卖家 frozen(买家冻结款)划入 balance */
    void settle(Long buyerId, Long sellerId, Long orderId, BigDecimal amount, String remark);

    /** 取消退款：frozen -= amount, balance += amount */
    void refund(Long userId, Long orderId, BigDecimal amount, String remark);

    /** 违约金：从买家冻结款扣除转给卖家，返回实际扣除额 */
    BigDecimal penaltyToSeller(Long buyerId, Long sellerId, Long orderId, BigDecimal amount, String remark);

    PageResult<WalletLog> logs(Long userId, long page, long size);
}
