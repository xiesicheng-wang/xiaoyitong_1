package com.youlinbang.module.skill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlinbang.common.exception.BusinessException;
import com.youlinbang.common.result.PageResult;
import com.youlinbang.module.skill.dto.SkillCreateDTO;
import com.youlinbang.module.skill.entity.Skill;
import com.youlinbang.module.skill.mapper.SkillMapper;
import com.youlinbang.module.skill.service.SkillService;
import com.youlinbang.module.skill.vo.SkillVO;
import com.youlinbang.module.user.entity.User;
import com.youlinbang.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {

    private final SkillMapper skillMapper;
    private final UserMapper userMapper;

    @Override
    public SkillVO create(Long userId, SkillCreateDTO dto) {
        Skill s = new Skill();
        s.setUserId(userId);
        s.setTitle(dto.getTitle());
        s.setDescription(dto.getDescription() == null ? "" : dto.getDescription());
        s.setCategory(dto.getCategory() == null || dto.getCategory().isBlank() ? "其他" : dto.getCategory());
        s.setPrice(dto.getPrice());
        s.setPriceUnit("小时".equals(dto.getPriceUnit()) ? "小时" : "次");
        s.setStatus(0);
        skillMapper.insert(s);
        log.info("技能挂单: id={}, userId={}, title={}", s.getId(), userId, s.getTitle());
        return SkillVO.from(s);
    }

    @Override
    public PageResult<SkillVO> page(long page, long size, String keyword, String category) {
        Page<Skill> result = skillMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Skill>()
                        .eq(Skill::getStatus, 0)
                        .eq(category != null && !category.isBlank(), Skill::getCategory, category)
                        .and(keyword != null && !keyword.isBlank(),
                                w -> w.like(Skill::getTitle, keyword).or().like(Skill::getDescription, keyword))
                        .orderByDesc(Skill::getCreateTime));
        Map<Long, String> names = sellerNames(result.getRecords());
        return new PageResult<>(result.getRecords().stream()
                .map(s -> {
                    SkillVO vo = SkillVO.from(s);
                    vo.setSellerName(names.get(s.getUserId()));
                    return vo;
                }).toList(), result.getTotal(), page, size);
    }

    @Override
    public SkillVO detail(Long id) {
        Skill s = skillMapper.selectById(id);
        if (s == null) {
            throw BusinessException.business("挂单不存在或已删除");
        }
        skillMapper.update(null, new LambdaUpdateWrapper<Skill>()
                .eq(Skill::getId, id)
                .setSql("view_count = view_count + 1"));
        s.setViewCount(s.getViewCount() == null ? 1 : s.getViewCount() + 1);
        SkillVO vo = SkillVO.from(s);
        vo.setSellerName(sellerNames(List.of(s)).get(s.getUserId()));
        return vo;
    }

    private Map<Long, String> sellerNames(List<Skill> skills) {
        List<Long> ids = skills.stream().map(Skill::getUserId).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(User::getId,
                        u -> u.getNickname() == null || u.getNickname().isBlank() ? u.getUsername() : u.getNickname(),
                        (a, b) -> a));
    }

    @Override
    public PageResult<SkillVO> mine(Long userId, long page, long size) {
        Page<Skill> result = skillMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Skill>()
                        .eq(Skill::getUserId, userId)
                        .orderByDesc(Skill::getId));
        return new PageResult<>(result.getRecords().stream().map(SkillVO::from).toList(),
                result.getTotal(), page, size);
    }

    @Override
    public SkillVO toggle(Long userId, Long skillId) {
        Skill s = skillMapper.selectById(skillId);
        if (s == null) {
            throw BusinessException.business("挂单不存在");
        }
        if (!s.getUserId().equals(userId)) {
            throw BusinessException.forbidden("只能操作自己的挂单");
        }
        s.setStatus(s.getStatus() == 0 ? 1 : 0);
        skillMapper.updateById(s);
        return SkillVO.from(s);
    }
}
