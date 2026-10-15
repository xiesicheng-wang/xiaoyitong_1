package com.youlinbang.module.order.service;

import com.youlinbang.common.result.PageResult;
import com.youlinbang.module.order.entity.Notification;

public interface NotificationService {

    void notify(Long userId, String title, String content, String relatedType, Long relatedId);

    PageResult<Notification> my(Long userId, long page, long size);

    long unreadCount(Long userId);

    void markRead(Long userId, Long id);

    void markAllRead(Long userId);
}
