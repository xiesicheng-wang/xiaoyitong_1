package com.youlinbang.module.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlinbang.common.exception.BusinessException;
import com.youlinbang.common.result.PageResult;
import com.youlinbang.module.admin.service.AdminService;
import com.youlinbang.module.admin.vo.AdminOrderVO;
import com.youlinbang.module.admin.vo.AdminProductVO;
import com.youlinbang.module.admin.vo.AdminUserVO;
import com.youlinbang.module.order.entity.Order;
import com.youlinbang.module.order.entity.Wallet;
import com.youlinbang.module.order.mapper.OrderMapper;
import com.youlinbang.module.order.mapper.WalletMapper;
import com.youlinbang.module.order.service.NotificationService;
import com.youlinbang.module.product.entity.Product;
import com.youlinbang.module.product.mapper.ProductMapper;
import com.youlinbang.module.user.entity.User;
import com.youlinbang.module.user.mapper.UserMapper;
import com.youlinbang.module.user.mapper.UserRealnameMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final UserRealnameMapper realnameMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final WalletMapper walletMapper;
    private final NotificationService notificationService;

    @Override
    public Map<String, Object> stats() {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("userCount", userMapper.selectCount(null));
        map.put("realnamePassed", realnameMapper.selectCount(
                new LambdaQueryWrapper<com.youlinbang.module.user.entity.UserRealname>()
                        .eq(com.youlinbang.module.user.entity.UserRealname::getStatus, 2)));
        map.put("productOnSale", productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getStatus, 0)));
        map.put("orderTotal", orderMapper.selectCount(null));
        map.put("orderProcessing", orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getStatus, 0)));
        map.put("orderOverdue", orderMapper.selectCount(
                new LambdaQueryWrapper<Order>().eq(Order::getStatus, 3)));
        // 平台资金池：所有钱包余额/冻结总额
        List<Map<String, Object>> sums = walletMapper.selectMaps(
                new QueryWrapper<Wallet>().select(
                        "IFNULL(SUM(balance),0) AS totalBalance",
                        "IFNULL(SUM(frozen),0) AS totalFrozen"));
        Map<String, Object> money = sums.isEmpty() ? Map.of() : sums.get(0);
        map.put("walletBalance", money.getOrDefault("totalBalance", BigDecimal.ZERO));
        map.put("walletFrozen", money.getOrDefault("totalFrozen", BigDecimal.ZERO));
        return map;
    }

    @Override
    public PageResult<AdminUserVO> users(long page, long size, String keyword) {
        Page<User> result = userMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<User>()
                        .and(keyword != null && !keyword.isBlank(),
                                w -> w.like(User::getUsername, keyword).or().like(User::getNickname, keyword))
                        .orderByAsc(User::getId));
        return new PageResult<>(result.getRecords().stream().map(AdminUserVO::from).toList(),
                result.getTotal(), page, size);
    }

    @Override
    public void banUser(Long operatorId, Long userId, boolean ban) {
        User target = userMapper.selectById(userId);
        if (target == null) {
            throw BusinessException.business("用户不存在");
        }
        if (target.getRole() != null && target.getRole() == 1) {
            throw BusinessException.business("不能封禁管理员账号");
        }
        target.setStatus(ban ? 1 : 0);
        userMapper.updateById(target);
        if (ban) {
            notificationService.notify(userId, "账号封禁通知",
                    "你的账号已被管理员封禁，如有疑问请联系平台", "USER", userId);
        }
    }

    @Override
    public PageResult<AdminProductVO> products(long page, long size, String keyword) {
        Page<Product> result = productMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Product>()
                        .like(keyword != null && !keyword.isBlank(), Product::getTitle, keyword)
                        .orderByDesc(Product::getId));
        Map<Long, String> sellers = usernames(result.getRecords().stream().map(Product::getUserId).distinct().toList());
        return new PageResult<>(result.getRecords().stream()
                        .map(p -> AdminProductVO.from(p, sellers.get(p.getUserId()))).toList(),
                result.getTotal(), page, size);
    }

    @Override
    public void toggleProduct(Long productId, boolean onSale) {
        Product p = productMapper.selectById(productId);
        if (p == null) {
            throw BusinessException.business("商品不存在");
        }
        p.setStatus(onSale ? 0 : 1);
        productMapper.updateById(p);
        notificationService.notify(p.getUserId(), onSale ? "商品重新上架" : "商品下架通知",
                "你的商品「" + p.getTitle() + "」已被管理员" + (onSale ? "重新上架" : "下架"),
                "PRODUCT", productId);
    }

    @Override
    public PageResult<AdminOrderVO> orders(long page, long size, Integer status) {
        Page<Order> result = orderMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Order>()
                        .eq(status != null, Order::getStatus, status)
                        .orderByDesc(Order::getId));
        Map<Long, String> usernames = usernames(result.getRecords().stream()
                .flatMap(o -> java.util.stream.Stream.of(o.getBuyerId(), o.getSellerId())).distinct().toList());
        Map<Long, String> titles = productTitles(result.getRecords());
        return new PageResult<>(result.getRecords().stream()
                        .map(o -> AdminOrderVO.from(o, usernames.get(o.getBuyerId()),
                                usernames.get(o.getSellerId()), titles.get(o.getProductId())))
                        .toList(),
                result.getTotal(), page, size);
    }

    private Map<Long, String> usernames(List<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername, (a, b) -> a));
    }

    private Map<Long, String> productTitles(List<Order> orders) {
        if (orders.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = orders.stream().map(Order::getProductId).distinct().toList();
        return productMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Product::getId, Product::getTitle, (a, b) -> a));
    }
}
