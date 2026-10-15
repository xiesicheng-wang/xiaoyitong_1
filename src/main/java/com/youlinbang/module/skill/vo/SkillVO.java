package com.youlinbang.module.skill.vo;

import com.youlinbang.module.skill.entity.Skill;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SkillVO {

    private Long id;
    private Long userId;
    private String title;
    private String description;
    private String category;
    private BigDecimal price;
    private String priceUnit;
    private Integer status;
    private Integer viewCount;
    private LocalDateTime createTime;

    public static SkillVO from(Skill s) {
        SkillVO vo = new SkillVO();
        vo.setId(s.getId());
        vo.setUserId(s.getUserId());
        vo.setTitle(s.getTitle());
        vo.setDescription(s.getDescription());
        vo.setCategory(s.getCategory());
        vo.setPrice(s.getPrice());
        vo.setPriceUnit(s.getPriceUnit());
        vo.setStatus(s.getStatus());
        vo.setViewCount(s.getViewCount());
        vo.setCreateTime(s.getCreateTime());
        return vo;
    }
}
