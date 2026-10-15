package com.youlinbang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 友邻帮 - 社区互助服务平台 启动类
 *
 * <p>模块化单体架构，按业务分包（com.youlinbang.module.*），预留微服务拆分点。</p>
 */
@SpringBootApplication
public class YoulinbangApplication {

    public static void main(String[] args) {
        SpringApplication.run(YoulinbangApplication.class, args);
    }
}
