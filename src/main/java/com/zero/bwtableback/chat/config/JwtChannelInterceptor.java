package com.zero.bwtableback.chat.config;

import com.zero.bwtableback.common.exception.CustomException;
import com.zero.bwtableback.common.exception.ErrorCode;
import com.zero.bwtableback.security.jwt.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 웹소켓 연결 시 인증을 위한 인터셉터
 */
@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final TokenProvider tokenProvider;

    @Autowired
    public JwtChannelInterceptor(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider; // 생성자에서 초기화
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor
                .getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authHeader = accessor.getNativeHeader("Authorization");
            if (authHeader != null && !authHeader.isEmpty()) {
                String token = authHeader.get(0).replace("Bearer ", "");
                if (tokenProvider.validateAccessToken(token)) {
                    accessor.setUser(tokenProvider.getAuthentication(token));
                } else {
                    throw new CustomException(ErrorCode.INVALID_TOKEN);
                }
            }
        }
        System.out.println(message);
        return message;
    }
}