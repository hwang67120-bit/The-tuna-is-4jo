package com.example.thetunais4joteamproject.domain.chat.dto;

import java.util.List;

public record GetChatRoomListResponse(
	List<GetChatRoomListItemResponse> chatRooms
) {

	public static GetChatRoomListResponse from(List<GetChatRoomListItemResponse> chatRooms) {
		return new GetChatRoomListResponse(chatRooms);
	}
}
