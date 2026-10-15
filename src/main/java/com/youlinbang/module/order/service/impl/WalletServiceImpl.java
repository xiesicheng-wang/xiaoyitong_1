package com.youlinbang.module.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlinbang.common.exception.BusinessException;
import com.youlinbang.common.result.PageResult;
import com.youlinbang.module.order.entity.Wallet;
import com.youlinbang.module.order.entity.WalletLog;
import com.youlinbang.module.order.mapper.WalletLogMapper;
import com.youlinbang.module.order.mapper.WalletMapper;
import com.youlinbang.module.order.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletMapper walletMapper;
    private final WalletLogMapper walletLogMapper;

    @Override
    public Wallet getOrCreate(Long userId) {
        Wallet w = walletMapper.selectById(userId);
        if (w == null) {
            w = new Wallet();
            w.setUserId(userId);
            w.setBalance(BigDecimal.ZERO);
            w.setFrozen(BigDecimal.ZERO);
            try {
                walletMapper.insert(w);
            } catch (Exception e) {
                // 并发首建冲突时读已有记录
                w = walletMapper.selectById(userId);
            }
        }
        return w;
    }

    @Override
    @Transactional
    public Wallet recharge(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw BusinessException.badRequest("充值金额必须大于 0");
        }
        Wallet w = getOrCreate(userId);
        w.setBalance(w.getBalance().add(amount));
        walletMapper.updateById(w);
        log(w, "RECHARGE", amount, null, "模拟充值成功（真实环境走支付宝沙箱回调）");
        return w;
    }

    @Override
    @Transactional
    public void freeze(Long userId, Long orderId, BigDecimal amount, String remark) {
        Wallet w = getOrCreate(userId);
        if (w.getBalance().compareTo(amount) < 0) {
            throw BusinessException.business("余额不足，请先充值（需 " + amount + " 元）");
        }
        // 条件更新防并发超扣
        int rows = walletMapper.deductBalance(userId, amount);
        if (rows == 0) {
            throw BusinessException.business("余额不足，请先充值");
        }
        walletMapper.update(null, new LambdaUpdateWrapper<Wallet>()
                .eq(Wallet::getUserId, userId)
                .setSql("frozen = frozen + " + amount));
        w = getOrCreate(userId);
        log(w, "FREEZE", amount, orderId, remark);
    }

    @Override
    @Transactional
    public void settle(Long buyerId, Long sellerId, Long orderId, BigDecimal amount, String remark) {
        // 买家解冻扣款
        walletMapper.update(null, new LambdaUpdateWrapper<Wallet>()
                .eq(Wallet::getUserId, buyerId)
                .setSql("frozen = frozen - " + amount));
        // 卖家入账
        Wallet seller = getOrCreate(sellerId);
        seller.setBalance(seller.getBalance().add(amount));
        walletMapper.updateById(seller);
        Wallet buyer = getOrCreate(buyerId);
        log(buyer, "SETTLE", amount, orderId, "支出：" + remark);
        log(seller, "SETTLE", amount, orderId, "收入：" + remark);
    }

    @Override
    @Transactional
    public void refund(Long userId, Long orderId, BigDecimal amount, String remark) {
        walletMapper.update(null, new LambdaUpdateWrapper<Wallet>()
                .eq(Wallet::getUserId, userId)
                .setSql("frozen = frozen - " + amount + ", balance = balance + " + amount));
        Wallet w = getOrCreate(userId);
        log(w, "REFUND", amount, orderId, remark);
    }

    @Override
    @Transactional
    public BigDecimal penaltyToSeller(Long buyerId, Long sellerId, Long orderId, BigDecimal amount, String remark) {
        // 违约金从买家冻结款扣除转给卖家
        walletMapper.update(null, new LambdaUpdateWrapper<Wallet>()
                .eq(Wallet::getUserId, buyerId)
                .setSql("frozen = frozen - " + amount));
        Wallet seller = getOrCreate(sellerId);
        seller.setBalance(seller.getBalance().add(amount));
        walletMapper.updateById(seller);
        Wallet buyer = getOrCreate(buyerId);
        log(buyer, "PENALTY", amount, orderId, "违约扣除：" + remark);
        log(seller, "PENALTY", amount, orderId, "违约金收入：" + remark);
        return amount;
    }

    private void log(Wallet w, String type, BigDecimal amount, Long orderId, String remark) {
        WalletLog l = new WalletLog();
        l.setUserId(w.getUserId());
        l.setType(type);
        l.setAmount(amount);
        l.setBalanceAfter(w.getBalance());
        l.setFrozenAfter(w.getFrozen());
        l.setOrderId(orderId);
        l.setRemark(remark);
        walletLogMapper.insert(l);
    }

    @Override
    public PageResult<WalletLog> logs(Long userId, long page, long size) {
        Page<WalletLog> result = walletLogMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<WalletLog>()
                        .eq(WalletLog::getUserId, userId)
                        .orderByDesc(WalletLog::getId));
        return new PageResult<>(result.getRecords(), result.getTotal(), page, size);
    }
}
