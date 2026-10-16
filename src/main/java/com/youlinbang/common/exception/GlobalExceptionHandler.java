package com.youlinbang.common.exception;

import com.youlinbang.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLNonTransientConnectionException;

/**
 * 全局异常处理：所有未捕获异常统一转成 Result 结构，避免堆栈直接暴露给前端
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：可预期，warn 级别即可 */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常: code={}, msg={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /** @Valid 参数校验失败：取第一个字段错误提示 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        FieldError fe = e.getBindingResult().getFieldError();
        String msg = fe == null ? "请求参数错误" : fe.getDefaultMessage();
        return Result.fail(400, msg);
    }

    /** 基础设施故障要给出可行动的提示，而不是笼统的"服务器内部错误" */
    @ExceptionHandler({RedisConnectionFailureException.class, RedisSystemException.class})
    public Result<Void> handleRedis(Exception e) {
        log.error("Redis 连接失败", e);
        return Result.fail(503, "缓存服务连接失败：请确认本机 Redis 已启动（start-services.bat）");
    }

    @ExceptionHandler({DataAccessResourceFailureException.class, SQLNonTransientConnectionException.class})
    public Result<Void> handleDb(Exception e) {
        log.error("数据库连接失败", e);
        return Result.fail(503, "数据库连接失败：请确认 MySQL 服务已启动");
    }

    /** 兜底：未预期异常，error 级别并保留堆栈；返回信息带上简短根因便于定位 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleUnexpected(Exception e) {
        log.error("未预期异常", e);
        String root = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        root = root.split("\n")[0];
        if (root.length() > 120) {
            root = root.substring(0, 120) + "...";
        }
        return Result.fail(500, "服务器内部错误: " + root);
    }
}
