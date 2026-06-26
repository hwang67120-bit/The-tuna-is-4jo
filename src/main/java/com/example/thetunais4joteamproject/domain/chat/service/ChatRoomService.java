package com.example.thetunais4joteamproject.domain.chat.service;

import com.example.thetunais4joteamproject.domain.chat.dto.CreateChatRoomRequest;
import com.example.thetunais4joteamproject.domain.chat.dto.CreateChatRoomResponse;
import com.example.thetunais4joteamproject.domain.chat.dto.GetChatMessageResponse;
import com.example.thetunais4joteamproject.domain.chat.dto.GetChatRoomListItemResponse;
import com.example.thetunais4joteamproject.domain.chat.dto.GetChatRoomListResponse;
import com.example.thetunais4joteamproject.domain.chat.dto.GetChatRoomResponse;
import com.example.thetunais4joteamproject.domain.chat.dto.JoinChatRoomResponse;
import com.example.thetunais4joteamproject.domain.chat.dto.ChatMessageResponse;
import com.example.thetunais4joteamproject.domain.chat.dto.ChatRoomEventResponse;
import com.example.thetunais4joteamproject.domain.chat.dto.CloseChatRoomResponse;
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
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private static final String USER_CLOSE_MESSAGE = "문의가 종료되었습니다.";
    private static final String AUTO_CLOSE_MESSAGE = "일정 시간 응답이 없어 문의가 종료되었습니다.";

    private static final String ADMIN_CHAT_ROOM_DESTINATION = "/topic/admin/chat/rooms";
    private static final String EVENT_CREATED = "CREATED";
    private static final String EVENT_STATUS_CHANGED = "STATUS_CHANGED";
    private static final String EVENT_CLOSED = "CLOSED";
    private static final String EVENT_AUTO_CLOSED = "AUTO_CLOSED";

    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**채팅 생성 **/
    @Transactional
    public CreateChatRoomResponse create(Long memberId, CreateChatRoomRequest createChatRoomRequest) {
        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> BusinessException.from(ErrorCode.UNAUTHORIZED));
        validateUserRole(member);
        validateNoActiveChatRoom(member.getId());

        ChatRoom chatRoom = chatRoomRepository.save(
                ChatRoom.create(member.getId(), createChatRoomRequest.title())
        );
        chatMessageRepository.save(
                ChatMessage.createUserMessage(chatRoom.getId(), member.getId(), createChatRoomRequest.content())
        );
        sendAdminChatRoomEvent(chatRoom, EVENT_CREATED);

        return CreateChatRoomResponse.from(chatRoom);
    }

    /**채팅 참여 **/
    @Transactional
    public JoinChatRoomResponse join(Long memberId, Long chatRoomId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> BusinessException.from(ErrorCode.UNAUTHORIZED));
        ChatRoom chatRoom = chatRoomRepository.findByIdForUpdate(chatRoomId)
                .orElseThrow(() -> BusinessException.from(ErrorCode.NOT_FOUND));

        if (member.getRole() == MemberRole.ADMIN) {
            boolean shouldSendEnterSystemMessage = chatRoom.getStatus() == ChatRoomStatus.WAITING;
            chatRoom.joinAdmin(member.getId());
            if (shouldSendEnterSystemMessage) {
                log.info("Chat enter system message requested. chatRoomId={}, memberId={}, role={}",
                        chatRoom.getId(),
                        member.getId(),
                        member.getRole()
                );
                sendEnterSystemMessage(chatRoom, member);
                sendAdminChatRoomEvent(chatRoom, EVENT_STATUS_CHANGED);
            }
            return JoinChatRoomResponse.from(chatRoom, member.getRole());
        }

        chatRoom.joinUser(member.getId());
        return JoinChatRoomResponse.from(chatRoom, member.getRole());
    }

    /**채팅 종료 **/
    @Transactional
    public CloseChatRoomResponse close(Long memberId, Long chatRoomId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> BusinessException.from(ErrorCode.UNAUTHORIZED));
        validateUserRole(member);

        ChatRoom chatRoom = chatRoomRepository.findByIdForUpdate(chatRoomId)
                .orElseThrow(() -> BusinessException.from(ErrorCode.NOT_FOUND));
        chatRoom.closeByUser(member.getId());
        sendCloseSystemMessage(chatRoom, member);
        sendAdminChatRoomEvent(chatRoom, EVENT_CLOSED);

        return CloseChatRoomResponse.from(chatRoom);
    }
    /**채팅 자동 종료 **/
    @Transactional
    public int closeInactiveChatRooms(LocalDateTime threshold) {
        List<ChatRoom> chatRooms = chatRoomRepository.findInactiveRoomsForUpdate(
                ChatRoomStatus.activeStatuses(),
                threshold
        );

        for (ChatRoom chatRoom : chatRooms) {
            chatRoom.closeBySystem();
            sendAutoCloseSystemMessage(chatRoom);
            sendAdminChatRoomEvent(chatRoom, EVENT_AUTO_CLOSED);
        }

        return chatRooms.size();
    }


    /**채팅 목록 조회 **/
    @Transactional(readOnly = true)
    public GetChatRoomListResponse getAll(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> BusinessException.from(ErrorCode.UNAUTHORIZED));

        List<ChatRoom> chatRooms = getChatRoomsByRole(member);
        List<GetChatRoomListItemResponse> chatRoomResponses = chatRooms.stream()
                .map(GetChatRoomListItemResponse::from)
                .toList();

        return GetChatRoomListResponse.from(chatRoomResponses);
    }

    /**채팅방 조회 **/
    @Transactional(readOnly = true)
    public GetChatRoomResponse getOne(Long memberId, Long chatRoomId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> BusinessException.from(ErrorCode.UNAUTHORIZED));

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> BusinessException.from(ErrorCode.NOT_FOUND));
        validateChatRoomReadable(member, chatRoom);

        List<GetChatMessageResponse> messages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(chatRoomId)
                .stream()
                .map(GetChatMessageResponse::from)
                .toList();

        return GetChatRoomResponse.of(chatRoom, messages);
    }

    /**채팅 입장 알람 **/
    private void sendEnterSystemMessage(ChatRoom chatRoom, Member member) {
        log.info("Chat enter system message sending. chatRoomId={}, memberId={}, role={}",
                chatRoom.getId(),
                member.getId(),
                member.getRole()
        );

        ChatMessage chatMessage = chatMessageRepository.save(
                ChatMessage.createSystemMessage(
                        chatRoom.getId(),
                        member.getId(),
                        member.getId() + "님이 입장하셨습니다."
                )
        );

        messagingTemplate.convertAndSend(
                "/topic/chat/rooms/" + chatRoom.getId(),
                ChatMessageResponse.from(chatMessage)
        );
        log.info("Chat enter system message sent. chatRoomId={}, messageId={}, messageType={}",
                chatRoom.getId(),
                chatMessage.getId(),
                chatMessage.getMessageType()
        );
    }

    /**채팅 종료 알람 **/
    private void sendCloseSystemMessage(ChatRoom chatRoom, Member member) {
        log.info("Chat close system message sending. chatRoomId={}, memberId={}, role={}",
                chatRoom.getId(),
                member.getId(),
                member.getRole()
        );

        ChatMessage chatMessage = chatMessageRepository.save(
                ChatMessage.createSystemMessage(
                        chatRoom.getId(),
                        member.getId(),
                        USER_CLOSE_MESSAGE
                )
        );

        /** 채팅방 닫기**/
        messagingTemplate.convertAndSend(
                "/topic/chat/rooms/" + chatRoom.getId(),
                ChatMessageResponse.from(chatMessage)
        );
        log.info("Chat close system message sent. chatRoomId={}, messageId={}, messageType={}",
                chatRoom.getId(),
                chatMessage.getId(),
                chatMessage.getMessageType()
        );
    }
    /**채팅 자동 종료 알람 **/
    private void sendAutoCloseSystemMessage(ChatRoom chatRoom) {
        log.info("Chat auto close system message sending. chatRoomId={}", chatRoom.getId());

        ChatMessage chatMessage = chatMessageRepository.save(
                ChatMessage.createSystemMessage(
                        chatRoom.getId(),
                        chatRoom.getMemberId(),
                        AUTO_CLOSE_MESSAGE
                )
        );

        messagingTemplate.convertAndSend(
                "/topic/chat/rooms/" + chatRoom.getId(),
                ChatMessageResponse.from(chatMessage)
        );
        log.info("Chat auto close system message sent. chatRoomId={}, messageId={}, messageType={}",
                chatRoom.getId(),
                chatMessage.getId(),
                chatMessage.getMessageType()
        );
    }
    /**관리자 문의 목록 갱신 알람 **/
    private void sendAdminChatRoomEvent(ChatRoom chatRoom, String eventType) {
        log.info("Admin chat room event sending. chatRoomId={}, eventType={}", chatRoom.getId(), eventType);

        messagingTemplate.convertAndSend(
                ADMIN_CHAT_ROOM_DESTINATION,
                ChatRoomEventResponse.of(chatRoom, eventType)
        );
    }



    private void validateUserRole(Member member) {
        if (member.getRole() != MemberRole.USER) {
            throw BusinessException.from(ErrorCode.FORBIDDEN);
        }
    }

    private List<ChatRoom> getChatRoomsByRole(Member member) {
        if (member.getRole() == MemberRole.ADMIN) {
            return chatRoomRepository.findAllByOrderByCreatedAtDesc();
        }
        if (member.getRole() == MemberRole.USER) {
            return chatRoomRepository.findAllByMemberIdOrderByCreatedAtDesc(member.getId());
        }

        throw BusinessException.from(ErrorCode.FORBIDDEN);
    }

    private void validateChatRoomReadable(Member member, ChatRoom chatRoom) {
        if (member.getRole() == MemberRole.ADMIN) {
            return;
        }
        if (member.getRole() == MemberRole.USER && chatRoom.isOwner(member.getId())) {
            return;
        }

        throw BusinessException.from(ErrorCode.FORBIDDEN);
    }

    private void validateNoActiveChatRoom(Long memberId) {
        boolean existsActiveChatRoom = chatRoomRepository.existsByMemberIdAndStatusIn(
                memberId,
                ChatRoomStatus.activeStatuses()
        );
        if (existsActiveChatRoom) {
            throw BusinessException.from(ErrorCode.CONFLICT);
        }
    }
}
