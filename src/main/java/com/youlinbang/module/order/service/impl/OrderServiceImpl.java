package com.youlinbang.module.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlinbang.common.exception.BusinessException;
import com.youlinbang.common.result.PageResult;
import com.youlinbang.module.order.entity.Notification;
import com.youlinbang.module.order.entity.Order;
import com.youlinbang.module.order.entity.WalletLog;
import com.youlinbang.module.order.mapper.NotificationMapper;
import com.youlinbang.module.order.mapper.OrderMapper;
import com.youlinbang.module.order.mapper.WalletLogMapper;
import com.youlinbang.module.order.service.NotificationService;
import com.youlinbang.module.order.service.OrderService;
import com.youlinbang.module.order.service.WalletService;
import com.youlinbang.module.order.vo.OrderVO;
import com.youlinbang.module.product.entity.Product;
import com.youlinbang.module.product.mapper.ProductMapper;
import com.youlinbang.module.user.entity.User;
import com.youlinbang.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    /** 违约金比例（%） */
    @Value("${youlinbang.order.penalty-percent:20}")
    private int penaltyPercent;

    /** 履约截止分钟数：超时未完成判买家违约 */
    @Value("${youlinbang.order.timeout-minutes:30}")
    private long timeoutMinutes;

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final WalletService walletService;
    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;
    private final WalletLogMapper walletLogMapper;

    @Override
    @Transactional
    public OrderVO create(Long buyerId, Long productId) {
        Product p = productMapper.selectById(productId);
        if (p == null || p.getStatus() != 0) {
            throw BusinessException.business("商品不存在或已下架");
        }
        if (p.getUserId().equals(buyerId)) {
            throw BusinessException.business("不能购买自己发布的商品");
        }
        User buyer = userMapper.selectById(buyerId);
        if (buyer.getRealnameStatus() == null || buyer.getRealnameStatus() != 2) {
            throw BusinessException.business("下单前需完成实名认证（线下交易安全要求）");
        }
        if (buyer.getCreditScore() == null || buyer.getCreditScore() < 60) {
            throw BusinessException.business("信誉分低于 60，已被限制下单");
        }

        // 预冻结全款（核心：钱先锁进冻结区，完成才结算给卖家）
        walletService.freeze(buyerId, null, p.getPrice(), "下单冻结：" + p.getTitle());

        Order o = new Order();
        o.setOrderNo("YLB" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        o.setBuyerId(buyerId);
        o.setSellerId(p.getUserId());
        o.setProductId(productId);
        o.setAmount(p.getPrice());
        o.setPenalty(BigDecimal.ZERO);
        o.setStatus(0);
        o.setDeadline(LocalDateTime.now().plusMinutes(timeoutMinutes));
        orderMapper.insert(o);

        // 冻结流水的 orderId 补挂到订单
        walletLogMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<WalletLog>()
                .eq(WalletLog::getUserId, buyerId)
                .eq(WalletLog::getType, "FREEZE")
                .isNull(WalletLog::getOrderId)
                .orderByDesc(WalletLog::getId)
                .last("LIMIT 1")
                .set(WalletLog::getOrderId, o.getId()));

        notificationService.notify(p.getUserId(), "新订单提醒",
                buyer.getUsername() + " 下单了你的商品「" + p.getTitle() + "」，金额 " + p.getPrice() + " 元，请在截止时间前完成交易",
                "ORDER", o.getId());
        return OrderVO.from(o, p.getTitle());
    }

    @Override
    @Transactional
    public OrderVO complete(Long orderId, Long sellerId) {
        Order o = mustGet(orderId);
        if (!o.getSellerId().equals(sellerId)) {
            throw BusinessException.forbidden("只有卖家可以确认完成");
        }
        if (o.getStatus() != 0) {
            throw BusinessException.business("订单当前状态不可完成");
        }
        walletService.settle(o.getBuyerId(), sellerId, orderId, o.getAmount(), "订单 " + o.getOrderNo() + " 交易完成");
        o.setStatus(1);
        orderMapper.updateById(o);
        addCredit(o.getBuyerId(), 2);
        addCredit(sellerId, 2);
        notificationService.notify(o.getBuyerId(), "订单完成",
                "订单 " + o.getOrderNo() + " 已完成，卖家已收到货款", "ORDER", orderId);
        return OrderVO.from(o, null);
    }

    @Override
    @Transactional
    public OrderVO cancel(Long orderId, Long buyerId) {
        Order o = mustGet(orderId);
        if (!o.getBuyerId().equals(buyerId)) {
            throw BusinessException.forbidden("只有买家可以取消订单");
        }
        if (o.getStatus() != 0) {
            throw BusinessException.business("订单当前状态不可取消");
        }
        // 完成前取消 = 无责取消，全额退款
        walletService.refund(buyerId, orderId, o.getAmount(), "取消订单退款：" + o.getOrderNo());
        o.setStatus(2);
        orderMapper.updateById(o);
        notificationService.notify(o.getSellerId(), "订单取消",
                "买家已取消订单 " + o.getOrderNo() + "，预冻结款项已退回买家", "ORDER", orderId);
        return OrderVO.from(o, null);
    }

    @Override
    @Transactional
    public int checkOverdue() {
        List<Order> overdue = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, 0)
                .lt(Order::getDeadline, LocalDateTime.now()));
        for (Order o : overdue) {
            try {
                closeAsOverdue(o);
            } catch (Exception e) {
                log.error("违约处理失败: orderId={}", o.getId(), e);
            }
        }
        return overdue.size();
    }

    /** 超时违约：违约金(比例)给卖家，余额退买家，买家信誉 -10 */
    private void closeAsOverdue(Order o) {
        BigDecimal penalty = o.getAmount()
                .multiply(BigDecimal.valueOf(penaltyPercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN);
        if (penalty.compareTo(BigDecimal.ZERO) > 0) {
            walletService.penaltyToSeller(o.getBuyerId(), o.getSellerId(), o.getId(), penalty, "超时未完成");
        }
        BigDecimal refund = o.getAmount().subtract(penalty);
        if (refund.compareTo(BigDecimal.ZERO) > 0) {
            walletService.refund(o.getBuyerId(), o.getId(), refund, "违约关闭退回：" + o.getOrderNo());
        }
        o.setPenalty(penalty);
        o.setStatus(3);
        orderMapper.updateById(o);
        deductCredit(o.getBuyerId(), 10);
        notificationService.notify(o.getBuyerId(), "订单违约",
                "订单 " + o.getOrderNo() + " 超时未完成，已扣除违约金 " + penalty + " 元，信誉分 -10", "ORDER", o.getId());
        notificationService.notify(o.getSellerId(), "订单违约关闭",
                "订单 " + o.getOrderNo() + " 超时未完成，你收到违约金 " + penalty + " 元", "ORDER", o.getId());
        log.warn("订单超时违约: orderId={}, penalty={}", o.getId(), penalty);
    }

    private void addCredit(Long userId, int delta) {
        userMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .setSql("credit_score = LEAST(credit_score + " + delta + ", 100)"));
    }

    private void deductCredit(Long userId, int delta) {
        userMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .setSql("credit_score = GREATEST(credit_score - " + delta + ", 0)"));
    }

    private Order mustGet(Long id) {
        Order o = orderMapper.selectById(id);
        if (o == null) {
            throw BusinessException.business("订单不存在");
        }
        return o;
    }

    private PageResult<OrderVO> pageBy(Long userId, boolean asBuyer, long page, long size) {
        Page<Order> result = orderMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Order>()
                        .eq(asBuyer ? Order::getBuyerId : Order::getSellerId, userId)
                        .orderByDesc(Order::getId));
        Map<Long, String> titles = productTitles(result.getRecords());
        List<OrderVO> records = result.getRecords().stream()
                .map(o -> OrderVO.from(o, titles.get(o.getProductId()))).toList();
        return new PageResult<>(records, result.getTotal(), page, size);
    }

    private Map<Long, String> productTitles(List<Order> orders) {
        if (orders.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = orders.stream().map(Order::getProductId).distinct().toList();
        return productMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Product::getId, Product::getTitle, (a, b) -> a));
    }

    @Override
    public PageResult<OrderVO> bought(Long buyerId, long page, long size) {
        return pageBy(buyerId, true, page, size);
    }

    @Override
    public PageResult<OrderVO> sold(Long sellerId, long page, long size) {
        return pageBy(sellerId, false, page, size);
    }
}
