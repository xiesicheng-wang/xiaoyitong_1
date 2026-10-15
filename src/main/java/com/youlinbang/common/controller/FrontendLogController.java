package com.youlinbang.common.controller;

import com.youlinbang.common.result.Result;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 前端错误上报：浏览器端的报错经此落盘到 logs/frontend.log
 * （该路径在 JwtInterceptor 白名单中——登录页报错也要能报上来）
 */
@Slf4j
@RestController
@RequestMapping("/api/logs")
public class FrontendLogController {

    private static final org.slf4j.Logger FRONTEND = LoggerFactory.getLogger("FRONTEND");

    @PostMapping("/frontend")
    public Result<Void> report(@RequestBody FrontendLog log) {
        String line = "[{}] {} | page={} | ua={}"
                + (log.getStack() == null || log.getStack().isBlank() ? "" : " | stack={}");
        if ("ERROR".equalsIgnoreCase(log.getLevel())) {
            FRONTEND.error(line, tag(log), safe(log.getMessage()), safe(log.getPage()), safe(log.getUa()), safe(log.getStack()));
        } else if ("WARN".equalsIgnoreCase(log.getLevel())) {
            FRONTEND.warn(line, tag(log), safe(log.getMessage()), safe(log.getPage()), safe(log.getUa()), safe(log.getStack()));
        } else {
            FRONTEND.info(line, tag(log), safe(log.getMessage()), safe(log.getPage()), safe(log.getUa()), safe(log.getStack()));
        }
        return Result.ok();
    }

    private String tag(FrontendLog log) {
        return log.getKind() == null ? "unknown" : log.getKind();
    }

    private String safe(String s) {
        if (s == null) return "";
        // 单行化 + 限长，防止恶意超长内容刷爆日志
        return s.replace("\r", " ").replace("\n", " ").substring(0, Math.min(s.length(), 2000));
    }

    @Data
    public static class FrontendLog {
        private String level;   // INFO / WARN / ERROR
        private String kind;    // window / promise / axios
        private String message;
        private String stack;
        private String page;    // 出错页面 URL
        private String ua;      // 浏览器 UA
    }
}
