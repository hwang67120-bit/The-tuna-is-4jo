package com.example.thetunais4joteamproject.domain.chat.dto;

import com.example.thetunais4joteamproject.domain.chat.entity.ChatRoom;
import com.example.thetunais4joteamproject.domain.chat.entity.ChatRoomStatus;

public record CloseChatRoomResponse(
	Long chatRoomId,
	ChatRoomStatus status
) {

	public static CloseChatRoomResponse from(ChatRoom chatRoom) {
		return new CloseChatRoomResponse(chatRoom.getId(), chatRoom.getStatus());
	}
}