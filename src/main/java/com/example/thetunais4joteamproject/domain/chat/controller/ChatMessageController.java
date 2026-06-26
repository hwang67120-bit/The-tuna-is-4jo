package com.example.thetunais4joteamproject.domain.chat.controller;

import com.example.thetunais4joteamproject.domain.chat.dto.ChatMessageResponse;
import com.example.thetunais4joteamproject.domain.chat.dto.SendChatMessageRequest;
import com.example.thetunais4joteamproject.domain.chat.service.ChatMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private static final Logger log = LoggerFactory.getLogger(ChatMessageController.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageService chatMessageService;

    @MessageMapping("/chat/message")
    public void sendMessage(
            @Valid
            @Payload
            SendChatMessageRequest sendChatMessageRequest
    ) {
        log.info("Chat message received. chatRoomId={}, senderId={}, content={}",
                sendChatMessageRequest.chatRoomId(),
                sendChatMessageRequest.senderId(),
                sendChatMessageRequest.content()
        );

        ChatMessageResponse chatMessageResponse = chatMessageService.create(sendChatMessageRequest);

        messagingTemplate.convertAndSend(
                "/topic/chat/rooms/" + sendChatMessageRequest.chatRoomId(),
                chatMessageResponse
        );
    }
}
