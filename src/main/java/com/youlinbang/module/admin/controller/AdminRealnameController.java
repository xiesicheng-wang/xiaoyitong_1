package com.youlinbang.module.admin.controller;

import com.youlinbang.common.exception.BusinessException;
import com.youlinbang.common.result.PageResult;
import com.youlinbang.common.result.Result;
import com.youlinbang.module.user.service.RealnameService;
import com.youlinbang.module.user.vo.RealnameVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理后台：实名审核（仅 role=1 管理员可用，service 内校验）
 */
@RestController
@RequestMapping("/api/admin/realnames")
@RequiredArgsConstructor
public class AdminRealnameController {

    private final RealnameService realnameService;

    @GetMapping
    public Result<PageResult<RealnameVO>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Integer status) {
        return Result.ok(realnameService.pageForAdmin(page, size, status));
    }

    @PostMapping("/{id}/audit")
    public Result<RealnameVO> audit(@PathVariable Long id,
                                    @Valid @RequestBody AuditDTO dto,
                                    HttpServletRequest request) {
        Long auditUserId = (Long) request.getAttribute("userId");
        return Result.ok(realnameService.audit(auditUserId, id, dto.getPass(), dto.getReason()));
    }

    @Data
    public static class AuditDTO {
        @NotNull(message = "审核结果不能为空")
        private Boolean pass;

        private String reason;
    }
}
