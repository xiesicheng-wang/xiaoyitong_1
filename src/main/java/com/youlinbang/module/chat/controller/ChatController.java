package com.youlinbang.module.chat.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlinbang.common.result.PageResult;
import com.youlinbang.common.result.Result;
import com.youlinbang.module.chat.entity.Message;
import com.youlinbang.module.chat.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 群聊历史记录（REST 拉取；实时消息走 WebSocket）
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final MessageMapper messageMapper;

    /** 倒序分页取历史，前端反转后展示 */
    @GetMapping("/messages")
    public Result<PageResult<Message>> history(@RequestParam(defaultValue = "1") long page,
                                               @RequestParam(defaultValue = "20") long size) {
        Page<Message> result = messageMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Message>().orderByDesc(Message::getId));
        List<Message> records = result.getRecords().stream().toList();
        return Result.ok(new PageResult<>(records, result.getTotal(), page, size));
    }
}
