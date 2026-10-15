package com.youlinbang.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 实名认证实体（对应 t_realname）
 *
 * <p>身份证完整号不落库：只存 SHA-256 哈希（唯一约束防一号多实）与掩码（展示）。</p>
 */
@Data
@TableName("t_realname")
public class UserRealname {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String realName;

    /** 掩码：520102********1234 */
    private String idCardMasked;

    /** SHA-256 哈希（64 位十六进制） */
    private String idCardHash;

    private String photoUrl;

    /** 状态：1待审核 2已通过 3已驳回 */
    private Integer status;

    private String rejectReason;

    private Long auditUserId;

    private LocalDateTime auditTime;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
