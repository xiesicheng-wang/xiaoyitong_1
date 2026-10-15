package com.youlinbang.common.interceptor;

import com.youlinbang.common.exception.BusinessException;
import com.youlinbang.module.user.entity.User;
import com.youlinbang.module.user.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理员拦截器：挂载在 /api/admin/** 之上（运行于 JwtInterceptor 之后，
 * userId 已注入）。集中做 role=1 校验，业务代码不再各自判断。
 */
@Component
@RequiredArgsConstructor
public class AdminInterceptor implements HandlerInterceptor {

    private final UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw BusinessException.unauthorized("未登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null || user.getRole() == null || user.getRole() != 1) {
            throw BusinessException.forbidden("无管理员权限");
        }
        return true;
    }
}
