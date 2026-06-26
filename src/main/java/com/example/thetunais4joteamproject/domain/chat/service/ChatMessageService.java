package com.example.thetunais4joteamproject.domain.chat.service;

import com.example.thetunais4joteamproject.domain.chat.dto.ChatMessageResponse;
import com.example.thetunais4joteamproject.domain.chat.dto.ChatRoomEventResponse;
import com.example.thetunais4joteamproject.domain.chat.dto.SendChatMessageRequest;
import com.example.thetunais4joteamproject.domain.chat.entity.ChatMessage;
import com.example.thetunais4joteamproject.domain.chat.entity.ChatRoom;
import com.example.thetunais4joteamproject.domain.chat.entity.ChatRoomStatus;
import com.example.thetunais4joteamproject.domain.chat.repository.ChatMessageRepository;
import com.example.thetunais4joteamproject.domain.chat.repository.ChatRoomRepository;
import com.example.thetunais4joteamproject.domain.user.entity.Member;
import com.example.thetunais4joteamproject.domain.user.entity.MemberRole;
import com.example.thetunais4joteamproject.domain.user.repository.MemberRepository;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private static final String ADMIN_CHAT_ROOM_DESTINATION = "/topic/admin/chat/rooms";
    private static final String EVENT_MESSAGE_CREATED = "MESSAGE_CREATED";

    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /** 메세지 생성 **/
    @Transactional
    public ChatMessageResponse create(SendChatMessageRequest sendChatMessageRequest, Long senderId) {
        Member sender = memberRepository.findById(senderId)
                .orElseThrow(() -> BusinessException.from(ErrorCode.UNAUTHORIZED));
        ChatRoom chatRoom = chatRoomRepository.findById(sendChatMessageRequest.chatRoomId())
                .orElseThrow(() -> BusinessException.from(ErrorCode.NOT_FOUND));
        validateMessageSender(chatRoom, sender);

        ChatMessage chatMessage = chatMessageRepository.save(
                ChatMessage.create(
                        chatRoom.getId(),
                        sender.getId(),
                        sendChatMessageRequest.content(),
                        sender.getRole()
                )
        );
        sendAdminChatRoomEvent(chatRoom, EVENT_MESSAGE_CREATED);

        return ChatMessageResponse.from(chatMessage);
    }
    
    private void sendAdminChatRoomEvent(ChatRoom chatRoom, String eventType) {
        messagingTemplate.convertAndSend(
                ADMIN_CHAT_ROOM_DESTINATION,
                ChatRoomEventResponse.of(chatRoom, eventType)
        );
    }


    
    private void validateMessageSender(ChatRoom chatRoom, Member sender) {
        if (chatRoom.getStatus() == ChatRoomStatus.CLOSED) {
            throw BusinessException.from(ErrorCode.CONFLICT);
        }
        if (sender.getRole() == MemberRole.USER && chatRoom.isOwner(sender.getId())) {
            return;
        }
        if (sender.getRole() == MemberRole.ADMIN && sender.getId().equals(chatRoom.getAdminId())) {
            return;
        }

        throw BusinessException.from(ErrorCode.FORBIDDEN);
    }
}
