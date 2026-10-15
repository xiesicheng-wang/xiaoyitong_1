package com.youlinbang.module.skill.service;

import com.youlinbang.common.result.PageResult;
import com.youlinbang.module.skill.dto.SkillCreateDTO;
import com.youlinbang.module.skill.vo.SkillVO;

public interface SkillService {

    SkillVO create(Long userId, SkillCreateDTO dto);

    PageResult<SkillVO> page(long page, long size, String keyword, String category);

    SkillVO detail(Long id);

    /** 我的挂单（管理自己的上下架） */
    PageResult<SkillVO> mine(Long userId, long page, long size);

    SkillVO toggle(Long userId, Long skillId);
}
