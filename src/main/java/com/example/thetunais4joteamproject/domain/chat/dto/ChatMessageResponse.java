package com.example.thetunais4joteamproject.domain.chat.dto;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long chatRoomId,
        Long senderId,
        String content,
        LocalDateTime sentAt
) {

    public static ChatMessageResponse from(SendChatMessageRequest sendChatMessageRequest) {
        return new ChatMessageResponse(
                sendChatMessageRequest.chatRoomId(),
                sendChatMessageRequest.senderId(),
                sendChatMessageRequest.content(),
                LocalDateTime.now()
        );
    }
}
