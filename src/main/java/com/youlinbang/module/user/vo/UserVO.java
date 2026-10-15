package com.youlinbang.module.user.vo;

import lombok.Data;

/**
 * 用户信息视图：对外展示用，永远不包含密码字段
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String avatar;
    private Integer creditScore;
    private Integer realnameStatus;

    public static UserVO from(com.youlinbang.module.user.entity.User u) {
        UserVO vo = new UserVO();
        vo.setId(u.getId());
        vo.setUsername(u.getUsername());
        vo.setNickname(u.getNickname());
        vo.setPhone(u.getPhone());
        vo.setAvatar(u.getAvatar());
        vo.setCreditScore(u.getCreditScore());
        vo.setRealnameStatus(u.getRealnameStatus());
        return vo;
    }
}
