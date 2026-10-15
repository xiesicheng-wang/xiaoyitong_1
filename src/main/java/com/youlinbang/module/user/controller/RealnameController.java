package com.youlinbang.module.user.controller;

import com.youlinbang.common.result.Result;
import com.youlinbang.module.user.dto.RealnameSubmitDTO;
import com.youlinbang.module.user.service.RealnameService;
import com.youlinbang.module.user.vo.RealnameVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 实名认证（用户端，需登录）
 */
@RestController
@RequestMapping("/api/realname")
@RequiredArgsConstructor
public class RealnameController {

    private final RealnameService realnameService;

    @PostMapping("/submit")
    public Result<RealnameVO> submit(@Valid @RequestBody RealnameSubmitDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(realnameService.submit(userId, dto.getRealName(), dto.getIdCard(), dto.getPhotoUrl()));
    }

    @GetMapping("/me")
    public Result<RealnameVO> mine(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(realnameService.mine(userId));
    }
}
