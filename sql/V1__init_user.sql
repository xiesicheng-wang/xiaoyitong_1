-- 友邻帮 V1：用户表
CREATE TABLE IF NOT EXISTS t_user (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(32)  NOT NULL COMMENT '用户名（登录账号）',
    password        CHAR(60)     NOT NULL COMMENT 'BCrypt 哈希（60 字符定长）',
    nickname        VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '昵称',
    avatar          VARCHAR(255) NOT NULL DEFAULT '' COMMENT '头像 URL（MinIO）',
    credit_score    INT          NOT NULL DEFAULT 100 COMMENT '信誉分：初始 100',
    realname_status TINYINT      NOT NULL DEFAULT 0 COMMENT '实名状态：0未实名 1待审核 2已实名',
    status          TINYINT      NOT NULL DEFAULT 0 COMMENT '账号状态：0正常 1封禁',
    deleted         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';
