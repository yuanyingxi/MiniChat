package com.minichat.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionManager {

    private static final Map<Long, WebSocketSession> ONLINE_USERS = new ConcurrentHashMap<>();
    private static final Map<WebSocketSession, Long> SESSION_USER_MAP = new ConcurrentHashMap<>();

    /**
     * 用户上线
     */
    public void addSession(Long userId, WebSocketSession session) {

        ONLINE_USERS.put(userId, session);
        SESSION_USER_MAP.put(session, userId);
    }

    /**
     * 用户下线
     */
    public void removeSession(Long userId) {

        WebSocketSession session =
                ONLINE_USERS.remove(userId);

        if (session != null) {
            SESSION_USER_MAP.remove(session);
        }
    }

    public void removeSession(
            WebSocketSession session) {

        Long userId =
                SESSION_USER_MAP.remove(session);

        if (userId != null) {
            ONLINE_USERS.remove(userId);
        }
    }

    /**
     * 是否在线
     */
    public boolean isOnline(Long userId) {

        return ONLINE_USERS.containsKey(userId);
    }

    /**
     * 在线人数
     */
    public int onlineCount() {

        return ONLINE_USERS.size();
    }

    /**
     * 给指定用户发消息
     */
    public void sendToUser(Long userId, String payload) {

        WebSocketSession session = ONLINE_USERS.get(userId);
        if (session == null) return;

        try {
            session.sendMessage(new TextMessage(payload));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}