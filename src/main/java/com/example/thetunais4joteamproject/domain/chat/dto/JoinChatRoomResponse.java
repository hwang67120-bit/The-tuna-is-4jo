package com.example.thetunais4joteamproject.domain.chat.dto;

import com.example.thetunais4joteamproject.domain.chat.entity.ChatRoom;
import com.example.thetunais4joteamproject.domain.chat.entity.ChatRoomStatus;
import com.example.thetunais4joteamproject.domain.user.entity.MemberRole;

public record JoinChatRoomResponse(
	Long chatRoomId,
	ChatRoomStatus status,
	MemberRole participantRole
) {

	public static JoinChatRoomResponse from(ChatRoom chatRoom, MemberRole participantRole) {
		return new JoinChatRoomResponse(chatRoom.getId(), chatRoom.getStatus(), participantRole);
	}
}
