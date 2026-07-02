package com.example.thetunais4joteamproject.domain.chat.entity;

import java.util.List;

public enum ChatRoomStatus {

	WAITING,
	IN_PROGRESS,
	CLOSED;

	public static List<ChatRoomStatus> activeStatuses() {
		return List.of(WAITING, IN_PROGRESS);
	}
}
