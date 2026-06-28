package com.example.thetunais4joteamproject.domain.chat.entity;

import com.example.thetunais4joteamproject.domain.user.entity.MemberRole;
import com.example.thetunais4joteamproject.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "chat_message",
        indexes = {
                @Index(name = "idx_chat_message_room_id_id", columnList = "chat_room_id, id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_room_id", nullable = false)
    private Long chatRoomId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "message_type", nullable = false, length = 20)
    private String messageType;

    private ChatMessage(Long chatRoomId, Long senderId, String content, String messageType) {
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.content = content;
        this.messageType = messageType;
    }

    public static ChatMessage createUserMessage(Long chatRoomId, Long senderId, String content) {
        return new ChatMessage(chatRoomId, senderId, content, MemberRole.USER.name());
    }

    public static ChatMessage create(Long chatRoomId, Long senderId, String content, MemberRole senderRole) {
        return new ChatMessage(chatRoomId, senderId, content, senderRole.name());
    }

    public static ChatMessage createSystemMessage(Long chatRoomId, Long senderId, String content) {
        return new ChatMessage(chatRoomId, senderId, content, "SYSTEM");
    }
}
