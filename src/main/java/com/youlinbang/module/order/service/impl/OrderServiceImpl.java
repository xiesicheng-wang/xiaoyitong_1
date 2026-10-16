package com.youlinbang.module.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlinbang.common.exception.BusinessException;
import com.youlinbang.common.result.PageResult;
import com.youlinbang.module.order.entity.Order;
import com.youlinbang.module.order.entity.WalletLog;
import com.youlinbang.module.order.mapper.OrderMapper;
import com.youlinbang.module.order.mapper.WalletLogMapper;
import com.youlinbang.module.order.service.NotificationService;
import com.youlinbang.module.order.service.OrderService;
import com.youlinbang.module.order.service.WalletService;
import com.youlinbang.module.order.vo.OrderVO;
import com.youlinbang.module.product.entity.Product;
import com.youlinbang.module.product.mapper.ProductMapper;
import com.youlinbang.module.skill.entity.Skill;
import com.youlinbang.module.skill.mapper.SkillMapper;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 订单服务：商品(PRODUCT)与技能(SKILL)共用同一套
 * 冻结 → 结算 / 退款 / 违约 资金流
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    @Value("${youlinbang.order.penalty-percent:20}")
    private int penaltyPercent;

    @Value("${youlinbang.order.timeout-minutes:30}")
    private long timeoutMinutes;

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final SkillMapper skillMapper;
    private final UserMapper userMapper;
    private final WalletService walletService;
    private final NotificationService notificationService;
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
        return createOrder(buyerId, p.getUserId(), p.getPrice(), "PRODUCT", productId, null, p.getTitle());
    }

    @Override
    @Transactional
    public OrderVO createSkill(Long buyerId, Long skillId) {
        Skill s = skillMapper.selectById(skillId);
        if (s == null || s.getStatus() != 0) {
            throw BusinessException.business("该技能挂单不存在或已停止接单");
        }
        if (s.getUserId().equals(buyerId)) {
            throw BusinessException.business("不能预约自己的技能服务");
        }
        return createOrder(buyerId, s.getUserId(), s.getPrice(), "SKILL", null, skillId, s.getTitle());
    }

    /**
     * 公共下单逻辑：实名校验 → 信誉门槛 → 冻结全款 → 落单 → 通知服务方
     */
    private OrderVO createOrder(Long buyerId, Long sellerId, BigDecimal amount,
                                String type, Long productId, Long skillId, String title) {
        User buyer = userMapper.selectById(buyerId);
        if (buyer.getRealnameStatus() == null || buyer.getRealnameStatus() != 2) {
            throw BusinessException.business("下单前需完成实名认证（线下交易安全要求）");
        }
        if (buyer.getCreditScore() == null || buyer.getCreditScore() < 60) {
            throw BusinessException.business("信誉分低于 60，已被限制下单");
        }

        // 预冻结全款（钱先锁进冻结区，完成才结算给服务方）
        walletService.freeze(buyerId, null, amount, "下单冻结：" + title);

        Order o = new Order();
        o.setOrderNo("YLB" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        o.setOrderType(type);
        o.setBuyerId(buyerId);
        o.setSellerId(sellerId);
        o.setProductId(productId);
        o.setSkillId(skillId);
        o.setAmount(amount);
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

        notificationService.notify(sellerId, "新订单提醒",
                buyer.getUsername() + ("SKILL".equals(type) ? " 预约了你的服务「" : " 下单了你的商品「")
                        + title + "」，金额 " + amount + " 元，请在截止时间前完成交易",
                "ORDER", o.getId());
        log.info("订单创建: id={}, type={}, buyer={}, amount={}", o.getId(), type, buyerId, amount);
        return OrderVO.from(o, title);
    }

    @Override
    @Transactional
    public OrderVO complete(Long orderId, Long sellerId) {
        Order o = mustGet(orderId);
        if (!o.getSellerId().equals(sellerId)) {
            throw BusinessException.forbidden("只有服务提供方可以确认完成");
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
                "订单 " + o.getOrderNo() + " 已完成，款项已结算", "ORDER", orderId);
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

    /** 超时违约：违约金(比例)给服务方，余额退买家，买家信誉 -10
     *  幂等保护：多实例部署时调度器可能同时扫到同一订单，
     *  用条件更新 (status=0 → 3) 抢占处理权，抢不到的实例直接放弃资金操作 */
    private void closeAsOverdue(Order o) {
        BigDecimal penalty = o.getAmount()
                .multiply(BigDecimal.valueOf(penaltyPercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN);
        // 先抢占：只有从 status=0 成功翻转为 3 的实例才执行资金操作
        o.setPenalty(penalty);
        o.setStatus(3);
        int claimed = orderMapper.update(o, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Order>()
                .eq(Order::getId, o.getId())
                .eq(Order::getStatus, 0));
        if (claimed == 0) {
            log.info("订单已被其他实例处理，跳过: orderId={}", o.getId());
            return;
        }
        if (penalty.compareTo(BigDecimal.ZERO) > 0) {
            walletService.penaltyToSeller(o.getBuyerId(), o.getSellerId(), o.getId(), penalty, "超时未完成");
        }
        BigDecimal refund = o.getAmount().subtract(penalty);
        if (refund.compareTo(BigDecimal.ZERO) > 0) {
            walletService.refund(o.getBuyerId(), o.getId(), refund, "违约关闭退回：" + o.getOrderNo());
        }
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

    @Override
    public PageResult<OrderVO> bought(Long buyerId, long page, long size) {
        return pageBy(buyerId, true, page, size);
    }

    @Override
    public PageResult<OrderVO> sold(Long sellerId, long page, long size) {
        return pageBy(sellerId, false, page, size);
    }

    private PageResult<OrderVO> pageBy(Long userId, boolean asBuyer, long page, long size) {
        Page<Order> result = orderMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Order>()
                        .eq(asBuyer ? Order::getBuyerId : Order::getSellerId, userId)
                        .orderByDesc(Order::getId));
        Map<Long, String> titles = itemTitles(result.getRecords());
        List<OrderVO> records = result.getRecords().stream()
                .map(o -> OrderVO.from(o, titles.get(o.getId()))).toList();
        return new PageResult<>(records, result.getTotal(), page, size);
    }

    /** 商品与技能标题合并查询，key = orderId */
    private Map<Long, String> itemTitles(List<Order> orders) {
        if (orders.isEmpty()) {
            return Map.of();
        }
        List<Long> productIds = orders.stream().filter(o -> o.getProductId() != null)
                .map(Order::getProductId).distinct().toList();
        List<Long> skillIds = orders.stream().filter(o -> o.getSkillId() != null)
                .map(Order::getSkillId).distinct().toList();
        Map<Long, String> byProduct = productIds.isEmpty() ? Map.of()
                : productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Product::getTitle, (a, b) -> a));
        Map<Long, String> bySkill = skillIds.isEmpty() ? Map.of()
                : skillMapper.selectBatchIds(skillIds).stream()
                .collect(Collectors.toMap(Skill::getId, Skill::getTitle, (a, b) -> a));
        Map<Long, String> result = new HashMap<>();
        for (Order o : orders) {
            String title = "SKILL".equals(o.getOrderType())
                    ? bySkill.get(o.getSkillId()) : byProduct.get(o.getProductId());
            if (title != null) {
                result.put(o.getId(), title);
            }
        }
        return result;
    }
}
