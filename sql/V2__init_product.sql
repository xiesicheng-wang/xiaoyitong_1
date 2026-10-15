-- 友邻帮 V2：商品表（二手交易）
CREATE TABLE IF NOT EXISTS t_product (
    id          BIGINT        AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT        NOT NULL COMMENT '发布者用户 id',
    title       VARCHAR(64)   NOT NULL COMMENT '商品标题',
    description VARCHAR(2000) NOT NULL DEFAULT '' COMMENT '商品描述',
    price       DECIMAL(10,2) NOT NULL COMMENT '价格（元）',
    images      VARCHAR(2000) NOT NULL DEFAULT '' COMMENT '图片 URL 列表，JSON 数组，最多 9 张（MinIO）',
    category    VARCHAR(32)   NOT NULL DEFAULT '其他' COMMENT '分类：数码/图书/家居/服饰/运动/其他',
    status      TINYINT       NOT NULL DEFAULT 0 COMMENT '状态：0在售 1已下架 2已售出',
    view_count  INT           NOT NULL DEFAULT 0 COMMENT '浏览量',
    deleted     TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0正常 1已删除',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_user_id (user_id),
    KEY idx_status_category (status, category)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='二手商品表';
