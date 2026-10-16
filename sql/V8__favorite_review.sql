-- 友邻帮 V8：收藏 + 订单互评
CREATE TABLE IF NOT EXISTS t_favorite (
    id          BIGINT      AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT      NOT NULL COMMENT '收藏者',
    target_type VARCHAR(10) NOT NULL COMMENT 'PRODUCT / SKILL',
    target_id   BIGINT      NOT NULL COMMENT '商品或技能 id',
    create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_target (user_id, target_type, target_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='收藏表';

CREATE TABLE IF NOT EXISTS t_review (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    order_id     BIGINT       NOT NULL COMMENT '关联订单（双方各评一次）',
    reviewer_id  BIGINT       NOT NULL COMMENT '评价人',
    reviewee_id  BIGINT       NOT NULL COMMENT '被评价人',
    target_type  VARCHAR(10)  NOT NULL COMMENT 'PRODUCT / SKILL',
    target_id    BIGINT       NOT NULL COMMENT '商品/技能 id',
    score        TINYINT      NOT NULL COMMENT '评分 1-5',
    content      VARCHAR(500) NOT NULL DEFAULT '' COMMENT '评价内容',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_reviewer (order_id, reviewer_id),
    KEY idx_target (target_type, target_id),
    KEY idx_reviewee (reviewee_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='交易互评';
