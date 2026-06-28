package com.example.thetunais4joteamproject.domain.chat.dto;

import com.example.thetunais4joteamproject.domain.chat.entity.ChatRoom;

public record ChatRoomEventResponse(
        Long chatRoomId,
        String title,
        String status,
        String eventType
) {

    public static ChatRoomEventResponse of(ChatRoom chatRoom, String eventType) {
        return new ChatRoomEventResponse(
                chatRoom.getId(),
                chatRoom.getTitle(),
                chatRoom.getStatus().name(),
                eventType
        );
    }
}