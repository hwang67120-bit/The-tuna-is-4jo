package com.example.thetunais4joteamproject.domain.chat.pubsub;

public final class ChatRedisChannel {

	public static final String CHAT_ROOM_CHANNEL_PATTERN = "chat-room:*";
	private static final String CHAT_ROOM_CHANNEL_PREFIX = "chat-room:";

	private ChatRedisChannel() {
	}

	public static String from(Long chatRoomId) {
		return CHAT_ROOM_CHANNEL_PREFIX + chatRoomId;
	}
}