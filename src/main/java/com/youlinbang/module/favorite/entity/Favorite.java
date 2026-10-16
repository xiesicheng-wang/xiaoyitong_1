package com.youlinbang.module.favorite.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏实体（商品 / 技能通用，target_type 区分）
 */
@Data
@TableName("t_favorite")
public class Favorite {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** PRODUCT / SKILL */
    private String targetType;

    private Long targetId;

    private LocalDateTime createTime;
}
