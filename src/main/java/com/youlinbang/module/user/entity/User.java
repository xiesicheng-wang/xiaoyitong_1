package com.youlinbang.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体（对应 t_user）
 */
@Data
@TableName("t_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    /** BCrypt 哈希，任何接口不得返回该字段 */
    private String password;

    private String nickname;

    /** 手机号（唯一，可用于登录） */
    private String phone;

    private String avatar;

    private Integer creditScore;

    /** 实名状态：0未实名 1待审核 2已实名 */
    private Integer realnameStatus;

    /** 账号状态：0正常 1封禁 */
    private Integer status;

    /** 角色：0普通用户 1管理员 */
    private Integer role;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
