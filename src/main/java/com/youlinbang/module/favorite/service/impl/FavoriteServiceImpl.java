package com.youlinbang.module.favorite.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.youlinbang.common.exception.BusinessException;
import com.youlinbang.module.favorite.entity.Favorite;
import com.youlinbang.module.favorite.mapper.FavoriteMapper;
import com.youlinbang.module.favorite.service.FavoriteService;
import com.youlinbang.module.product.entity.Product;
import com.youlinbang.module.product.mapper.ProductMapper;
import com.youlinbang.module.skill.entity.Skill;
import com.youlinbang.module.skill.mapper.SkillMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final ProductMapper productMapper;
    private final SkillMapper skillMapper;

    private void validateType(String type) {
        if (!"PRODUCT".equals(type) && !"SKILL".equals(type)) {
            throw BusinessException.badRequest("收藏类型不合法");
        }
    }

    @Override
    public boolean toggle(Long userId, String targetType, Long targetId) {
        validateType(targetType);
        Favorite exist = favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetType, targetType)
                .eq(Favorite::getTargetId, targetId));
        if (exist != null) {
            favoriteMapper.deleteById(exist.getId());
            return false;
        }
        Favorite f = new Favorite();
        f.setUserId(userId);
        f.setTargetType(targetType);
        f.setTargetId(targetId);
        favoriteMapper.insert(f);
        return true;
    }

    @Override
    public boolean isFavorited(Long userId, String targetType, Long targetId) {
        return favoriteMapper.selectCount(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getTargetType, targetType)
                .eq(Favorite::getTargetId, targetId)) > 0;
    }

    @Override
    public List<Map<String, Object>> myFavorites(Long userId, String targetType) {
        List<Favorite> favs = favoriteMapper.selectList(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(targetType != null && !targetType.isBlank(), Favorite::getTargetType, targetType)
                .orderByDesc(Favorite::getId));
        List<Map<String, Object>> list = new ArrayList<>();
        for (Favorite f : favs) {
            Map<String, Object> item = new HashMap<>();
            item.put("targetType", f.getTargetType());
            item.put("targetId", f.getTargetId());
            item.put("createTime", f.getCreateTime());
            if ("PRODUCT".equals(f.getTargetType())) {
                Product p = productMapper.selectById(f.getTargetId());
                if (p == null) continue;
                item.put("title", p.getTitle());
                item.put("price", p.getPrice());
                item.put("status", p.getStatus());
            } else {
                Skill s = skillMapper.selectById(f.getTargetId());
                if (s == null) continue;
                item.put("title", s.getTitle());
                item.put("price", s.getPrice());
                item.put("priceUnit", s.getPriceUnit());
                item.put("status", s.getStatus());
            }
            list.add(item);
        }
        return list;
    }
}
