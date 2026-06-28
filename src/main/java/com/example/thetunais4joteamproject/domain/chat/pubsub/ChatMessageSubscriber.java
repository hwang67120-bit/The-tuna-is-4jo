package com.example.thetunais4joteamproject.domain.chat.pubsub;

import com.example.thetunais4joteamproject.domain.chat.dto.ChatMessageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Profile("redis")
@Component
@RequiredArgsConstructor
public class ChatMessageSubscriber implements MessageListener {

    private static final String CHAT_ROOM_DESTINATION_PREFIX = "/topic/chat/rooms/";

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            ChatMessageResponse chatMessageResponse = objectMapper.readValue(
                    message.getBody(),
                    ChatMessageResponse.class
            );

            messagingTemplate.convertAndSend(
                    CHAT_ROOM_DESTINATION_PREFIX + chatMessageResponse.chatRoomId(),
                    chatMessageResponse
            );
        } catch (IOException exception) {
            log.warn("Redis chat message deserialize failed. channel={}", new String(message.getChannel()), exception);
        }
    }
}