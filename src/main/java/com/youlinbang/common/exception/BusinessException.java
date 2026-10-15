package com.youlinbang.common.exception;

import com.youlinbang.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常：在 service 层校验业务规则不满足时抛出，
 * 由 GlobalExceptionHandler 统一转换为 Result 返回
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ResultCode rc) {
        super(rc.getMessage());
        this.code = rc.getCode();
    }

    public BusinessException(ResultCode rc, String message) {
        super(message);
        this.code = rc.getCode();
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(ResultCode.BAD_REQUEST, message);
    }

    public static BusinessException unauthorized(String message) {
        return new BusinessException(ResultCode.UNAUTHORIZED, message);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(ResultCode.FORBIDDEN, message);
    }

    public static BusinessException business(String message) {
        return new BusinessException(ResultCode.BUSINESS_ERROR, message);
    }
}
