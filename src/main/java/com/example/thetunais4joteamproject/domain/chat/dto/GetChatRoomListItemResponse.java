package com.example.thetunais4joteamproject.domain.chat.dto;

import com.example.thetunais4joteamproject.domain.chat.entity.ChatRoom;
import com.example.thetunais4joteamproject.domain.chat.entity.ChatRoomStatus;
import java.time.LocalDateTime;

public record GetChatRoomListItemResponse(
        Long chatRoomId,
        Long adminId,
        String title,
        ChatRoomStatus status,
        LocalDateTime createdAt
) {

    public static GetChatRoomListItemResponse from(ChatRoom chatRoom) {
        return new GetChatRoomListItemResponse(
                chatRoom.getId(),
                chatRoom.getAdminId(),
                chatRoom.getTitle(),
                chatRoom.getStatus(),
                chatRoom.getCreatedAt()
        );
    }
}
