package com.youlinbang.module.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.youlinbang.module.order.entity.Wallet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface WalletMapper extends BaseMapper<Wallet> {

    /** 余额扣减（带条件防并发超扣）：balance >= amount 才更新，返回受影响行数 */
    @Update("UPDATE t_wallet SET balance = balance - #{amount} WHERE user_id = #{userId} AND balance >= #{amount}")
    int deductBalance(Long userId, BigDecimal amount);
}
