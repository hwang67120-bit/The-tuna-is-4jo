package com.example.thetunais4joteamproject.domain.chat.service;

import com.example.thetunais4joteamproject.domain.chat.dto.CreateChatRoomRequest;
import com.example.thetunais4joteamproject.domain.chat.dto.CreateChatRoomResponse;
import com.example.thetunais4joteamproject.domain.chat.dto.GetChatMessageResponse;
import com.example.thetunais4joteamproject.domain.chat.dto.GetChatRoomListItemResponse;
import com.example.thetunais4joteamproject.domain.chat.dto.GetChatRoomListResponse;
import com.example.thetunais4joteamproject.domain.chat.dto.GetChatRoomResponse;
import com.example.thetunais4joteamproject.domain.chat.dto.JoinChatRoomResponse;
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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

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
            chatRoom.joinAdmin(member.getId());
            return JoinChatRoomResponse.from(chatRoom, member.getRole());
        }

        chatRoom.joinUser(member.getId());
        return JoinChatRoomResponse.from(chatRoom, member.getRole());
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
