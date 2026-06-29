package com.minichat.message.websocket;

import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class SessionManager {

    // Redis 分布式缓存：userId -> sessionId（或 serverId），30秒自动过期
    private final RMapCache<Long, String> onlineUsers;
    // 本地内存：sessionId -> WebSocketSession（用于实际发送消息）
    private final Map<String, WebSocketSession> SESSIONID_TO_SESSION = new ConcurrentHashMap<>();
    // 反向映射：userId -> sessionId（快速从 userId 找到本地 session）
    private final Map<Long, String> USERID_TO_SESSIONID = new ConcurrentHashMap<>();

    public SessionManager(RedissonClient redissonClient) {
        this.onlineUsers = redissonClient.getMapCache("online:users");
    }

    /**
     * 用户上线
     */
    public void addSession(Long userId, WebSocketSession session) {

        String sessionId = session.getId();

        // 存入 Redis，TTL = 30秒\
        onlineUsers.put(userId, sessionId, 30, TimeUnit.SECONDS);
        // 保存本地映射
        SESSIONID_TO_SESSION.put(sessionId, session);
        USERID_TO_SESSIONID.put(userId, sessionId);
    }

    /**
     * 心跳续期：只需在 Redis 中重新 put，TTL 重置为 30 秒
     */
    public void refreshHeartbeat(Long userId) {
        String sessionId = USERID_TO_SESSIONID.get(userId);
        if (sessionId != null) {
            onlineUsers.put(userId, sessionId, 30, TimeUnit.SECONDS);
        }
    }

    /**
     * 用户下线
     */
    public void removeSession(Long userId) {

        String sessionId = USERID_TO_SESSIONID.remove(userId);

        if (sessionId != null) {
            SESSIONID_TO_SESSION.remove(sessionId);
            onlineUsers.remove(userId);
        }
    }

    public void removeSession(WebSocketSession session) {

        String sessionId = session.getId();
        Long userId = null;

        // 找到 userId
        for (Map.Entry<Long, String> entry : USERID_TO_SESSIONID.entrySet()) {
            if (entry.getValue().equals(sessionId)) {
                userId = entry.getKey();
                break;
            }
        }

        if (userId != null) {
            USERID_TO_SESSIONID.remove(userId);
            SESSIONID_TO_SESSION.remove(sessionId);
            onlineUsers.remove(userId);
        }
    }

    /**
     * 判断用户是否在线（依赖 Redis 自动过期，实时准确）
     */
    public boolean isOnline(Long userId) {

        return onlineUsers.containsKey(userId);
    }

    /**
     * 在线人数（Redis 中的 key 数量）
     */
    public int onlineCount() {

        return onlineUsers.size();
    }

    /**
     * 给指定用户发消息
     */
    public void sendToUser(Long userId, String payload) {

        WebSocketSession session = null;

        if(isOnline(userId)){
            System.out.println("用户在线："+userId);
            String sessionId = onlineUsers.get(userId);
            session = SESSIONID_TO_SESSION.get(sessionId);
        }
        else{
            System.out.println("用户离线："+userId);

            String localSessionId = USERID_TO_SESSIONID.get(userId);
            if(localSessionId == null){
                System.out.println("localSessionId == null");
                return;
            }

            session = SESSIONID_TO_SESSION.get(localSessionId);
            if(session == null || !session.isOpen()){
                System.out.println("session == null || !session.isOpen()");
                removeSession(userId);
                return;
            }

            // redis显示用户离线，但是本地有会话连接
            System.out.println("自愈："+userId+"重新注册到 redis");
            onlineUsers.put(userId, localSessionId, 30, TimeUnit.SECONDS);
        }

        if (session == null || !session.isOpen()) {
            System.out.println("session == null || !session.isOpen()");
            removeSession(userId);
            return;
        }

        try {

            session.sendMessage(new org.springframework.web.socket.TextMessage(payload));
            System.out.println("消息发送");

        } catch (Exception e) {

            e.printStackTrace();

        }
    }
}