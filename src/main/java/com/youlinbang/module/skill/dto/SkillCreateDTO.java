package com.youlinbang.module.skill.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SkillCreateDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 64, message = "标题最长 64 个字符")
    private String title;

    @Size(max = 2000, message = "描述最长 2000 个字符")
    private String description;

    private String category;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于 0")
    private BigDecimal price;

    /** 计价单位：次 / 小时 */
    private String priceUnit;
}
