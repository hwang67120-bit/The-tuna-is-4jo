package com.example.thetunais4joteamproject.domain.chat.pubsub;

import com.example.thetunais4joteamproject.domain.chat.dto.ChatMessageResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMessagePublisher {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(ChatMessageResponse chatMessageResponse) {
        try {
            String channel = ChatRedisChannel.from(chatMessageResponse.chatRoomId());
            String message = objectMapper.writeValueAsString(chatMessageResponse);

            stringRedisTemplate.convertAndSend(channel, message);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Chat message publish failed", exception);
        }
    }
}