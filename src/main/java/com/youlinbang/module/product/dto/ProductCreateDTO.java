package com.youlinbang.module.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 发布商品请求
 */
@Data
public class ProductCreateDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 64, message = "标题最长 64 个字符")
    private String title;

    @Size(max = 2000, message = "描述最长 2000 个字符")
    private String description;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于 0")
    private BigDecimal price;

    @Size(max = 9, message = "图片最多 9 张")
    private List<String> images;

    private String category;
}
