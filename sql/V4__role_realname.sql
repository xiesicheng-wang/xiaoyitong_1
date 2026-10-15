-- 友邻帮 V4：用户角色 + 实名流程支持
ALTER TABLE t_user
    ADD COLUMN role TINYINT NOT NULL DEFAULT 0 COMMENT '角色：0普通用户 1管理员' AFTER status;

-- 开发期把 1 号账号设为管理员（答辩演示用）
UPDATE t_user SET role = 1 WHERE id = 1;
