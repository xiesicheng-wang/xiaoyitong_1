package com.youlinbang.module.order.service;

import com.youlinbang.common.result.PageResult;
import com.youlinbang.module.order.vo.OrderVO;

public interface OrderService {

    /** 买家下单（商品）：冻结全款，生成履约截止时间 */
    OrderVO create(Long buyerId, Long productId);

    /** 买家预约技能服务：同样冻结全款 */
    OrderVO createSkill(Long buyerId, Long skillId);

    /** 卖家确认完成：结算 + 双方信誉分 */
    OrderVO complete(Long orderId, Long sellerId);

    /** 买家取消（未完成前）：全额退款 */
    OrderVO cancel(Long orderId, Long buyerId);

    /** 超时违约判定（定时任务/管理员手动触发） */
    int checkOverdue();

    PageResult<OrderVO> bought(Long buyerId, long page, long size);

    PageResult<OrderVO> sold(Long sellerId, long page, long size);
}
