package com.example.thetunais4joteamproject.domain.chat.dto;

import com.example.thetunais4joteamproject.domain.chat.ChatRoom;
import com.example.thetunais4joteamproject.domain.chat.ChatRoomStatus;

public record CreateChatRoomResponse(
        Long chatRoomId,
        ChatRoomStatus status
) {

    public static CreateChatRoomResponse from(ChatRoom chatRoom) {
        return new CreateChatRoomResponse(chatRoom.getId(), chatRoom.getStatus());
    }
}
