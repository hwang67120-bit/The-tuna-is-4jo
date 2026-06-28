package com.example.thetunais4joteamproject.domain.chat.pubsub;

import com.example.thetunais4joteamproject.domain.chat.dto.ChatMessageResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Profile("redis")
@Component
@RequiredArgsConstructor
public class ChatMessagePublisher implements ChatMessageBroadcaster {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void broadcast(ChatMessageResponse chatMessageResponse) {
        try {
            String channel = ChatRedisChannel.from(chatMessageResponse.chatRoomId());
            String message = objectMapper.writeValueAsString(chatMessageResponse);

            stringRedisTemplate.convertAndSend(channel, message);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Chat message publish failed", exception);
        }
    }
}