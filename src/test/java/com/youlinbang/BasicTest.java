package com.youlinbang;

import com.youlinbang.common.exception.BusinessException;
import com.youlinbang.common.result.Result;
import com.youlinbang.common.result.ResultCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 公共层基础测试（不依赖 Spring 上下文与数据库）
 */
class BasicTest {

    @Test
    void resultOkShouldCarryData() {
        Result<String> r = Result.ok("hello");
        assertEquals(200, r.getCode());
        assertEquals("hello", r.getData());
    }

    @Test
    void businessExceptionShouldCarryCode() {
        BusinessException e = BusinessException.business("信誉分不足");
        assertEquals(ResultCode.BUSINESS_ERROR.getCode(), e.getCode());
        assertEquals("信誉分不足", e.getMessage());
    }
}
