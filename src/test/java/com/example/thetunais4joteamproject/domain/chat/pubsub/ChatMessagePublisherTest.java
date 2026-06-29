package com.example.thetunais4joteamproject.domain.chat.pubsub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.example.thetunais4joteamproject.domain.chat.dto.ChatMessageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;

class ChatMessagePublisherTest {

    private StringRedisTemplate stringRedisTemplate;
    private ObjectMapper objectMapper;
    private ChatMessagePublisher chatMessagePublisher;

    @BeforeEach
    void setUp() {
        stringRedisTemplate = mock(StringRedisTemplate.class);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        chatMessagePublisher = new ChatMessagePublisher(stringRedisTemplate, objectMapper);
    }

    @Test
    @DisplayName("채팅 메시지를 채팅방 Redis 채널로 발행한다.")
    void broadcast_PublishesMessageToChatRoomChannel() throws Exception {
        // given
        ChatMessageResponse chatMessageResponse = new ChatMessageResponse(
                1L,
                10L,
                "hello",
                "USER",
                LocalDateTime.of(2026, 6, 28, 10, 0)
        );
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        // when
        chatMessagePublisher.broadcast(chatMessageResponse);

        // then
        verify(stringRedisTemplate).convertAndSend(eq("chat-room:10"), messageCaptor.capture());

        ChatMessageResponse publishedMessage = objectMapper.readValue(
                messageCaptor.getValue(),
                ChatMessageResponse.class
        );
        assertThat(publishedMessage).isEqualTo(chatMessageResponse);
    }
}