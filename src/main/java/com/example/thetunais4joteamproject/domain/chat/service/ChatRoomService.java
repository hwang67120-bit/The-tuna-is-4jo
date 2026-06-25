package com.example.thetunais4joteamproject.domain.chat.service;

import com.example.thetunais4joteamproject.domain.chat.dto.CreateChatRoomRequest;
import com.example.thetunais4joteamproject.domain.chat.dto.CreateChatRoomResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public CreateChatRoomResponse create(Long memberId, CreateChatRoomRequest createChatRoomRequest) {
        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> BusinessException.from(ErrorCode.UNAUTHORIZED));
        validateUserRole(member);
        validateNoActiveChatRoom(member.getId());

        ChatRoom chatRoom = chatRoomRepository.save(
                ChatRoom.create(member, createChatRoomRequest.title())
        );
        chatMessageRepository.save(
                ChatMessage.createUserMessage(chatRoom, member, createChatRoomRequest.content())
        );

        return CreateChatRoomResponse.from(chatRoom);
    }

    private void validateUserRole(Member member) {
        if (member.getRole() != MemberRole.USER) {
            throw BusinessException.from(ErrorCode.FORBIDDEN);
        }
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
