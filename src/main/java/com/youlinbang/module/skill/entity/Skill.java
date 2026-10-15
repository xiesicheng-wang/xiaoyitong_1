package com.youlinbang.module.skill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 技能挂单实体（"修电脑 50 元/小时"）
 */
@Data
@TableName("t_skill")
public class Skill {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String description;

    private String category;

    private BigDecimal price;

    /** 计价单位：次 / 小时 */
    private String priceUnit;

    /** 状态：0接单中 1已下架 */
    private Integer status;

    private Integer viewCount;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
