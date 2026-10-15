package com.youlinbang.module.user.vo;

import lombok.Data;

/**
 * 登录成功响应：JWT + 用户信息
 */
@Data
public class LoginVO {

    private String token;
    private UserVO user;

    public static LoginVO of(String token, UserVO user) {
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUser(user);
        return vo;
    }
}
