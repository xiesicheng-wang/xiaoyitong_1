package com.youlinbang.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务状态码约定：
 * 2xx 成功；4xx 客户端问题（参数/未登录/无权限/资源不存在/业务规则）；5xx 服务端问题
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "ok"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限执行该操作"),
    NOT_FOUND(404, "资源不存在"),
    BUSINESS_ERROR(422, "业务规则不满足"),
    INTERNAL_ERROR(500, "服务器内部错误");

    private final int code;
    private final String message;
}
