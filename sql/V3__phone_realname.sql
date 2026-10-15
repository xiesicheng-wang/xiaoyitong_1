-- 友邻帮 V3：用户表加手机号 + 独立实名表
ALTER TABLE t_user
    ADD COLUMN phone CHAR(11) NULL COMMENT '手机号（唯一，可用于登录）' AFTER nickname,
    ADD UNIQUE KEY uk_phone (phone);

-- 身份证完整号不落库：只存 SHA-256 哈希（防一号多实）+ 掩码（展示）
CREATE TABLE IF NOT EXISTS t_realname (
    id             BIGINT       AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT       NOT NULL COMMENT '用户 id（一对一）',
    real_name      VARCHAR(32)  NOT NULL COMMENT '真实姓名',
    id_card_masked VARCHAR(20)  NOT NULL COMMENT '身份证掩码，如 5201**********1234',
    id_card_hash   CHAR(64)     NOT NULL COMMENT '身份证号 SHA-256 哈希（唯一，防一号多实名）',
    photo_url      VARCHAR(255) NOT NULL COMMENT '证件照 URL（MinIO）',
    status         TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1待审核 2已通过 3已驳回',
    reject_reason  VARCHAR(200) NOT NULL DEFAULT '' COMMENT '驳回原因',
    audit_user_id  BIGINT       NULL COMMENT '审核管理员 id',
    audit_time     DATETIME     NULL COMMENT '审核时间',
    deleted        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_id (user_id),
    UNIQUE KEY uk_id_card_hash (id_card_hash)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='实名认证表';
