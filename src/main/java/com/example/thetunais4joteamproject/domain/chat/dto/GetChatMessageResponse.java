package com.example.thetunais4joteamproject.domain.chat.dto;

import com.example.thetunais4joteamproject.domain.chat.entity.ChatMessage;
import java.time.LocalDateTime;

public record GetChatMessageResponse(
        Long messageId,
        String content,
        String messageType,
        LocalDateTime createdAt
) {

    public static GetChatMessageResponse from(ChatMessage chatMessage) {
        return new GetChatMessageResponse(
                chatMessage.getId(),
                chatMessage.getContent(),
                chatMessage.getMessageType(),
                chatMessage.getCreatedAt()
        );
    }
}