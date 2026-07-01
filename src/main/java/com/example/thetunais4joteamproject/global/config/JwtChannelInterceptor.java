package com.example.thetunais4joteamproject.global.config;

import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;
import com.example.thetunais4joteamproject.global.util.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String ADMIN_CHAT_ROOM_DESTINATION = "/topic/admin/chat/rooms";
    private static final String ADMIN_AUTHORITY = "ROLE_ADMIN";

    private final JwtProvider jwtProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorizationHeader = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);
            String accessToken = jwtProvider.extractToken(authorizationHeader);
            Authentication authentication = jwtProvider.getAuthentication(accessToken);

            accessor.setUser(authentication);
        }

        if (StompCommand.SEND.equals(accessor.getCommand()) && accessor.getUser() == null) {
            throw BusinessException.from(ErrorCode.UNAUTHORIZED);
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            validateAdminSubscribe(accessor);
        }

        return message;
    }

    private void validateAdminSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith(ADMIN_CHAT_ROOM_DESTINATION)) {
            return;
        }
        if (!(accessor.getUser() instanceof Authentication authentication)) {
            throw BusinessException.from(ErrorCode.UNAUTHORIZED);
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> ADMIN_AUTHORITY.equals(authority.getAuthority()));
        if (!isAdmin) {
            throw BusinessException.from(ErrorCode.FORBIDDEN);
        }
    }
}