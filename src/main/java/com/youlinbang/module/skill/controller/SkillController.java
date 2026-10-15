package com.youlinbang.module.skill.controller;

import com.youlinbang.common.result.PageResult;
import com.youlinbang.common.result.Result;
import com.youlinbang.module.skill.dto.SkillCreateDTO;
import com.youlinbang.module.skill.service.SkillService;
import com.youlinbang.module.skill.vo.SkillVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    @PostMapping
    public Result<SkillVO> create(@Valid @RequestBody SkillCreateDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(skillService.create(userId, dto));
    }

    @GetMapping
    public Result<PageResult<SkillVO>> page(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "10") long size,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String category) {
        return Result.ok(skillService.page(page, size, keyword, category));
    }

    @GetMapping("/{id}")
    public Result<SkillVO> detail(@PathVariable Long id) {
        return Result.ok(skillService.detail(id));
    }

    @GetMapping("/mine")
    public Result<PageResult<SkillVO>> mine(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "10") long size,
                                            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(skillService.mine(userId, page, size));
    }

    @PostMapping("/{id}/toggle")
    public Result<SkillVO> toggle(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(skillService.toggle(userId, id));
    }
}
