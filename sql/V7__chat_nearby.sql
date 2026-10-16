-- 友邻帮 V7：群聊消息表
CREATE TABLE IF NOT EXISTS t_message (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL COMMENT '发送者',
    username    VARCHAR(32)  NOT NULL COMMENT '发送者用户名（冗余，方便展示）',
    nickname    VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '发送者昵称（冗余）',
    content     VARCHAR(500) NOT NULL COMMENT '消息内容',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_create_time (create_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='社区群聊消息';
