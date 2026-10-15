package com.youlinbang.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youlinbang.common.exception.BusinessException;
import com.youlinbang.common.jwt.JwtUtil;
import com.youlinbang.module.user.dto.LoginDTO;
import com.youlinbang.module.user.dto.RegisterDTO;
import com.youlinbang.module.user.entity.User;
import com.youlinbang.module.user.mapper.UserMapper;
import com.youlinbang.module.user.service.UserService;
import com.youlinbang.module.user.vo.LoginVO;
import com.youlinbang.module.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String TOKEN_KEY = "auth:token:";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    @Override
    public UserVO register(RegisterDTO dto) {
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (exists > 0) {
            throw BusinessException.business("用户名已被占用");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        // 密码只存 BCrypt 哈希，绝不存明文
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() == null || dto.getNickname().isBlank()
                ? dto.getUsername() : dto.getNickname());
        userMapper.insert(user);
        log.info("新用户注册: id={}, username={}", user.getId(), user.getUsername());
        return UserVO.from(user);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        // 用户名不存在与密码错误统一提示，防止账号枚举
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw BusinessException.unauthorized("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw BusinessException.forbidden("账号已被封禁，请联系管理员");
        }
        String token = jwtUtil.create(user.getId(), user.getUsername());
        // Redis 会话：与服务端登出/踢下线联动
        redisTemplate.opsForValue().set(TOKEN_KEY + user.getId(), token, Duration.ofHours(72));
        log.info("用户登录: id={}, username={}", user.getId(), user.getUsername());
        return LoginVO.of(token, UserVO.from(user));
    }

    @Override
    public UserVO getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw BusinessException.business("用户不存在");
        }
        return UserVO.from(user);
    }
}
