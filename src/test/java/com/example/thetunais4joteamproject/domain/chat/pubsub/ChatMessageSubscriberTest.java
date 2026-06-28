package com.example.thetunais4joteamproject.domain.chat.pubsub;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.example.thetunais4joteamproject.domain.chat.dto.ChatMessageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;

class ChatMessageSubscriberTest {

    private SimpMessagingTemplate messagingTemplate;
    private ObjectMapper objectMapper;
    private ChatMessageSubscriber chatMessageSubscriber;

    @BeforeEach
    void setUp() {
        messagingTemplate = mock(SimpMessagingTemplate.class);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        chatMessageSubscriber = new ChatMessageSubscriber(messagingTemplate, objectMapper);
    }

    @Test
    @DisplayName("Redis 메시지를 STOMP 채팅방 구독자에게 전달한다.")
    void onMessage_SendsMessageToStompDestination() throws Exception {
        // given
        ChatMessageResponse chatMessageResponse = new ChatMessageResponse(
                1L,
                10L,
                "hello",
                "USER",
                LocalDateTime.of(2026, 6, 28, 10, 0)
        );
        Message message = mock(Message.class);

        given(message.getBody()).willReturn(objectMapper.writeValueAsBytes(chatMessageResponse));
        given(message.getChannel()).willReturn("chat-room:10".getBytes(StandardCharsets.UTF_8));

        // when
        chatMessageSubscriber.onMessage(message, null);

        // then
        verify(messagingTemplate).convertAndSend(
                eq("/topic/chat/rooms/10"),
                eq(chatMessageResponse)
        );
    }
}