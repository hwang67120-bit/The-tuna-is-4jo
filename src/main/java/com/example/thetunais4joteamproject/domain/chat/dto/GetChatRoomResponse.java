package com.example.thetunais4joteamproject.domain.chat.dto;

import com.example.thetunais4joteamproject.domain.chat.entity.ChatRoom;
import com.example.thetunais4joteamproject.domain.chat.entity.ChatRoomStatus;
import java.time.LocalDateTime;
import java.util.List;

public record GetChatRoomResponse(
        Long chatRoomId,
        Long adminId,
        String title,
        ChatRoomStatus status,
        LocalDateTime createdAt,
        List<GetChatMessageResponse> messages
) {

    public static GetChatRoomResponse of(
            ChatRoom chatRoom,
            List<GetChatMessageResponse> messages
    ) {
        return new GetChatRoomResponse(
                chatRoom.getId(),
                chatRoom.getAdminId(),
                chatRoom.getTitle(),
                chatRoom.getStatus(),
                chatRoom.getCreatedAt(),
                messages
        );
    }
}
