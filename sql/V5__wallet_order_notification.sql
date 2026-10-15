-- 友邻帮 V5：钱包 + 订单 + 通知
CREATE TABLE IF NOT EXISTS t_wallet (
    user_id     BIGINT        NOT NULL PRIMARY KEY COMMENT '用户 id（一对一）',
    balance     DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '可用余额',
    frozen      DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '冻结金额（订单预冻结）',
    update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='钱包表';

CREATE TABLE IF NOT EXISTS t_wallet_log (
    id            BIGINT        AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT        NOT NULL,
    type          VARCHAR(16)   NOT NULL COMMENT 'RECHARGE充值/FREEZE冻结/SETTLE结算/REFUND退款/PENALTY违约金',
    amount        DECIMAL(10,2) NOT NULL COMMENT '变动金额',
    balance_after DECIMAL(10,2) NOT NULL COMMENT '变动后余额',
    frozen_after  DECIMAL(10,2) NOT NULL COMMENT '变动后冻结',
    order_id      BIGINT        NULL COMMENT '关联订单',
    remark        VARCHAR(200)  NOT NULL DEFAULT '',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user_id (user_id),
    KEY idx_order_id (order_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='钱包流水';

CREATE TABLE IF NOT EXISTS t_order (
    id            BIGINT        AUTO_INCREMENT PRIMARY KEY,
    order_no      VARCHAR(32)   NOT NULL COMMENT '业务单号',
    buyer_id      BIGINT        NOT NULL COMMENT '买家（下单/预约方）',
    seller_id     BIGINT        NOT NULL COMMENT '卖家（商品发布者）',
    product_id    BIGINT        NOT NULL COMMENT '商品 id',
    amount        DECIMAL(10,2) NOT NULL COMMENT '订单金额（= 商品价格）',
    penalty       DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '已产生违约金',
    status        TINYINT       NOT NULL DEFAULT 0 COMMENT '0进行中(冻结) 1已完成(已结算) 2已取消(已退款) 3违约关闭',
    deadline      DATETIME      NOT NULL COMMENT '履约截止时间，超时未完成判买家违约',
    deleted       TINYINT       NOT NULL DEFAULT 0,
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_buyer (buyer_id),
    KEY idx_seller (seller_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='订单表';

CREATE TABLE IF NOT EXISTS t_notification (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT       NOT NULL COMMENT '接收人',
    title        VARCHAR(64)  NOT NULL,
    content      VARCHAR(500) NOT NULL DEFAULT '',
    related_type VARCHAR(20)  NOT NULL DEFAULT '' COMMENT 'ORDER/REALNAME/PRODUCT',
    related_id   BIGINT       NULL,
    read_flag    TINYINT      NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user_read (user_id, read_flag)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='站内通知';
