package com.youlinbang.module.admin.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端用户视图（手机号掩码展示）
 */
@Data
public class AdminUserVO {

    private Long id;
    private String username;
    private String nickname;
    private String phoneMasked;
    private Integer creditScore;
    private Integer realnameStatus;
    /** 0正常 1封禁 */
    private Integer status;
    private Integer role;
    private LocalDateTime createTime;

    public static AdminUserVO from(com.youlinbang.module.user.entity.User u) {
        AdminUserVO vo = new AdminUserVO();
        vo.setId(u.getId());
        vo.setUsername(u.getUsername());
        vo.setNickname(u.getNickname());
        String phone = u.getPhone();
        vo.setPhoneMasked(phone == null || phone.length() < 11
                ? "" : phone.substring(0, 3) + "****" + phone.substring(7));
        vo.setCreditScore(u.getCreditScore());
        vo.setRealnameStatus(u.getRealnameStatus());
        vo.setStatus(u.getStatus());
        vo.setRole(u.getRole());
        vo.setCreateTime(u.getCreateTime());
        return vo;
    }
}
