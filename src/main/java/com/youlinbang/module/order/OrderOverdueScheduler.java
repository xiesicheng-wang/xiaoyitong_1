package com.youlinbang.module.order;

import com.youlinbang.module.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 超时违约判定任务。
 *
 * <p>演进点：生产环境替换为 RabbitMQ 延迟队列（下单时投递 TTL 消息），
 * 定时扫描是毕设阶段的简化实现，语义一致。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderOverdueScheduler {

    private final OrderService orderService;

    @Scheduled(fixedDelay = 30000, initialDelay = 30000)
    public void checkOverdue() {
        int count = orderService.checkOverdue();
        if (count > 0) {
            log.info("违约判定完成，处理订单数: {}", count);
        }
    }
}
