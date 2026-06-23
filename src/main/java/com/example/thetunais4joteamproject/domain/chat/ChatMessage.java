package com.example.thetunais4joteamproject.domain.chat;

import java.time.LocalDateTime;

public class ChatMessage {

    private Long id;
    private Long chatRoomId;
    private Long senderId;
    private String content;
    private String messageType;
    private LocalDateTime createdAt;
}
