package com.youlinbang.module.admin.service;

import com.youlinbang.common.result.PageResult;
import com.youlinbang.module.admin.vo.AdminOrderVO;
import com.youlinbang.module.admin.vo.AdminProductVO;
import com.youlinbang.module.admin.vo.AdminUserVO;

import java.util.Map;

/**
 * 管理端聚合服务（权限由 AdminInterceptor 统一保障）
 */
public interface AdminService {

    Map<String, Object> stats();

    PageResult<AdminUserVO> users(long page, long size, String keyword);

    /** 封禁/解封用户（管理员不可封管理员） */
    void banUser(Long operatorId, Long userId, boolean ban);

    PageResult<AdminProductVO> products(long page, long size, String keyword);

    /** 商品上架/下架 */
    void toggleProduct(Long productId, boolean onSale);

    PageResult<AdminOrderVO> orders(long page, long size, Integer status);
}
