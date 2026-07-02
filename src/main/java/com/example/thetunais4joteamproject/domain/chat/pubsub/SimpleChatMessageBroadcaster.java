package com.example.thetunais4joteamproject.domain.chat.pubsub;

import com.example.thetunais4joteamproject.domain.chat.dto.ChatMessageResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Profile("!redis")
@Component
@RequiredArgsConstructor
public class SimpleChatMessageBroadcaster implements ChatMessageBroadcaster {

	private static final String CHAT_ROOM_DESTINATION_PREFIX = "/topic/chat/rooms/";

	private final SimpMessagingTemplate messagingTemplate;

	@Override
	public void broadcast(ChatMessageResponse chatMessageResponse) {
		messagingTemplate.convertAndSend(
			CHAT_ROOM_DESTINATION_PREFIX + chatMessageResponse.chatRoomId(),
			chatMessageResponse
		);
	}
}