package com.youlinbang.module.chat.handler;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youlinbang.module.chat.entity.Message;
import com.youlinbang.module.chat.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 社区群聊处理器：消息广播 + 落库，在线成员名册
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatHandler extends TextWebSocketHandler {

    /** 在线成员名册：userId → session */
    private static final Map<Long, WebSocketSession> SESSIONS = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;
    private final MessageMapper messageMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        String nickname = (String) session.getAttributes().get("nickname");
        SESSIONS.put(userId, session);
        broadcast(system(nickname + " 进入了群聊"));
        log.info("群聊上线: userId={}, 在线人数={}", userId, SESSIONS.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        String username = (String) session.getAttributes().get("username");
        String nickname = (String) session.getAttributes().get("nickname");

        JsonNode node = objectMapper.readTree(message.getPayload());
        String content = node.path("content").asText("").trim();
        if (content.isEmpty() || content.length() > 500) {
            return;
        }

        // 落库
        Message m = new Message();
        m.setUserId(userId);
        m.setUsername(username);
        m.setNickname(nickname);
        m.setContent(content);
        m.setCreateTime(LocalDateTime.now());
        messageMapper.insert(m);

        // 广播
        broadcast(objectMapper.writeValueAsString(Map.of(
                "type", "chat",
                "userId", userId,
                "username", username,
                "nickname", nickname,
                "content", content,
                "ts", m.getCreateTime().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        )));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null && SESSIONS.remove(userId) != null) {
            String nickname = (String) session.getAttributes().get("nickname");
            broadcast(system(nickname + " 离开了群聊"));
            log.info("群聊下线: userId={}, 在线人数={}", userId, SESSIONS.size());
        }
    }

    private String system(String text) {
        try {
            return objectMapper.writeValueAsString(Map.of("type", "system", "content", text));
        } catch (Exception e) {
            return "{\"type\":\"system\",\"content\":\"\"}";
        }
    }

    private synchronized void broadcast(String json) {
        TextMessage msg = new TextMessage(json);
        SESSIONS.values().forEach(s -> {
            try {
                if (s.isOpen()) {
                    s.sendMessage(msg);
                }
            } catch (Exception e) {
                log.warn("广播失败，剔除失效会话: {}", s.getId());
            }
        });
    }
}
