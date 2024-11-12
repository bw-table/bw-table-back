package com.zero.bwtableback.chat.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class ChatHandler extends TextWebSocketHandler {
    private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session); // 새로운 세션 추가
        System.out.println("New connection established: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        // 모든 세션에 메시지 전송
        for (WebSocketSession webSocketSession : sessions) {
            if (webSocketSession.isOpen() && !webSocketSession.getId().equals(session.getId())) {
                webSocketSession.sendMessage(message); // 다른 클라이언트에게 메시지 전송
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session); // 세션 제거
        System.out.println("Connection closed: " + session.getId());
    }
}