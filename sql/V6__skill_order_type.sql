-- 友邻帮 V6：技能挂单 + 订单支持双类型
CREATE TABLE IF NOT EXISTS t_skill (
    id          BIGINT        AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT        NOT NULL COMMENT '技能提供者',
    title       VARCHAR(64)   NOT NULL COMMENT '服务标题',
    description VARCHAR(2000) NOT NULL DEFAULT '' COMMENT '服务描述',
    category    VARCHAR(32)   NOT NULL DEFAULT '其他' COMMENT '分类：维修/家教/代取/跑腿/摄影/其他',
    price       DECIMAL(10,2) NOT NULL COMMENT '价格',
    price_unit  VARCHAR(8)    NOT NULL DEFAULT '次' COMMENT '计价单位：次/小时',
    status      TINYINT       NOT NULL DEFAULT 0 COMMENT '状态：0接单中 1已下架',
    view_count  INT           NOT NULL DEFAULT 0 COMMENT '浏览量',
    deleted     TINYINT       NOT NULL DEFAULT 0,
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_user_id (user_id),
    KEY idx_status_category (status, category)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='技能挂单表';

-- 订单支持商品/技能双类型
ALTER TABLE t_order
    ADD COLUMN order_type VARCHAR(10) NOT NULL DEFAULT 'PRODUCT' COMMENT '订单类型：PRODUCT/SKILL' AFTER order_no,
    ADD COLUMN skill_id BIGINT NULL COMMENT '技能 id（SKILL 单）' AFTER product_id,
    MODIFY COLUMN product_id BIGINT NULL COMMENT '商品 id（PRODUCT 单）';
