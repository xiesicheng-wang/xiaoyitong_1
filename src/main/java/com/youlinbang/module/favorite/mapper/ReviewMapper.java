package com.youlinbang.module.favorite.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youlinbang.module.favorite.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    /** 用户平均分与评价数 */
    @Select("SELECT IFNULL(ROUND(AVG(score),1),0) AS avgScore, COUNT(*) AS total FROM t_review WHERE reviewee_id = #{userId}")
    Map<String, Object> statOfUser(Long userId);
}
