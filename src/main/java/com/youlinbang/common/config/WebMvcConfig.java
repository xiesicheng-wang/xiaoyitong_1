package com.youlinbang.common.config;

import com.youlinbang.common.interceptor.AdminInterceptor;
import com.youlinbang.common.jwt.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：JWT 拦截器 + 管理员拦截器挂载与放行路径
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;
    private final AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. 登录态：先验 JWT
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/health",
                        "/api/auth/register",
                        "/api/auth/login",
                        "/api/logs/**"
                );
        // 2. 管理员权限：在 JWT 之后，只挂 /api/admin/**
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/api/admin/**");
    }
}
