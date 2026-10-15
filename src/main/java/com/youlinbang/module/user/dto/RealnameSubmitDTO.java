package com.youlinbang.module.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 实名认证提交请求（证件照先经 /api/files/upload 上传，传回 URL）
 */
@Data
public class RealnameSubmitDTO {

    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 32, message = "姓名最长 32 个字符")
    private String realName;

    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^\\d{17}[\\dXx]$", message = "身份证号格式不正确")
    private String idCard;

    @NotBlank(message = "证件照不能为空")
    private String photoUrl;
}
