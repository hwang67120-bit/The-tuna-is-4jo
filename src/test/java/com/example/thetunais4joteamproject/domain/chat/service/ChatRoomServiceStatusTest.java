package com.example.thetunais4joteamproject.domain.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.example.thetunais4joteamproject.domain.chat.dto.CloseChatRoomResponse;
import com.example.thetunais4joteamproject.domain.chat.dto.CreateChatRoomRequest;
import com.example.thetunais4joteamproject.domain.chat.dto.CreateChatRoomResponse;
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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

class ChatRoomServiceStatusTest {

    private static final long USER_ID = 1L;
    private static final long ADMIN_ID = 2L;
    private static final long CHAT_ROOM_ID = 10L;

    private MemberRepository memberRepository;
    private ChatRoomRepository chatRoomRepository;
    private ChatMessageRepository chatMessageRepository;
    private SimpMessagingTemplate messagingTemplate;
    private ChatRoomService chatRoomService;

    @BeforeEach
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        chatRoomRepository = mock(ChatRoomRepository.class);
        chatMessageRepository = mock(ChatMessageRepository.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        chatRoomService = new ChatRoomService(
                memberRepository,
                chatRoomRepository,
                chatMessageRepository,
                messagingTemplate
        );
    }

    @Test
    @DisplayName("사용자가 채팅방을 생성하면 대기중, 관리자가 참여하면 진행중, 사용자가 종료하면 종료 상태가 된다.")
    void chatRoomStatus_ChangesFromWaitingToInProgressToClosed() {
        // given
        Member user = createMember(USER_ID, MemberRole.USER);
        Member admin = createMember(ADMIN_ID, MemberRole.ADMIN);
        CreateChatRoomRequest createChatRoomRequest = new CreateChatRoomRequest("상품 문의", "문의합니다.");
        ChatRoom[] savedChatRoom = new ChatRoom[1];

        given(memberRepository.findByIdForUpdate(USER_ID)).willReturn(Optional.of(user));
        given(memberRepository.findById(ADMIN_ID)).willReturn(Optional.of(admin));
        given(memberRepository.findById(USER_ID)).willReturn(Optional.of(user));
        given(chatRoomRepository.existsByMemberIdAndStatusIn(any(Long.class), anyCollection())).willReturn(false);
        given(chatRoomRepository.save(any(ChatRoom.class))).willAnswer(invocation -> {
            ChatRoom chatRoom = invocation.getArgument(0);
            ReflectionTestUtils.setField(chatRoom, "id", CHAT_ROOM_ID);
            savedChatRoom[0] = chatRoom;

            return chatRoom;
        });
        given(chatRoomRepository.findByIdForUpdate(CHAT_ROOM_ID)).willAnswer(invocation -> Optional.of(savedChatRoom[0]));
        given(chatMessageRepository.save(any(ChatMessage.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        CreateChatRoomResponse createChatRoomResponse = chatRoomService.create(USER_ID, createChatRoomRequest);
        ChatRoom chatRoom = savedChatRoom[0];
        JoinChatRoomResponse joinChatRoomResponse = chatRoomService.join(ADMIN_ID, CHAT_ROOM_ID);
        CloseChatRoomResponse closeChatRoomResponse = chatRoomService.close(USER_ID, CHAT_ROOM_ID);

        // then
        assertThat(createChatRoomResponse.status()).isEqualTo(ChatRoomStatus.WAITING);
        assertThat(joinChatRoomResponse.status()).isEqualTo(ChatRoomStatus.IN_PROGRESS);
        assertThat(closeChatRoomResponse.status()).isEqualTo(ChatRoomStatus.CLOSED);
        assertThat(chatRoom.getStatus()).isEqualTo(ChatRoomStatus.CLOSED);
        assertThat(chatRoom.getCompletedAt()).isNotNull();

        verify(chatRoomRepository).save(any(ChatRoom.class));
    }

    @Test
    @DisplayName("관리자는 진행중인 채팅방을 종료할 수 있다.")
    void adminCanCloseInProgressChatRoom() {
        // given
        Member admin = createMember(ADMIN_ID, MemberRole.ADMIN);
        ChatRoom chatRoom = ChatRoom.create(USER_ID, "상품 문의");
        ReflectionTestUtils.setField(chatRoom, "id", CHAT_ROOM_ID);
        chatRoom.joinAdmin(ADMIN_ID);

        given(memberRepository.findById(ADMIN_ID)).willReturn(Optional.of(admin));
        given(chatRoomRepository.findByIdForUpdate(CHAT_ROOM_ID)).willReturn(Optional.of(chatRoom));
        given(chatMessageRepository.save(any(ChatMessage.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        CloseChatRoomResponse response = chatRoomService.close(ADMIN_ID, CHAT_ROOM_ID);

        // then
        assertThat(response.status()).isEqualTo(ChatRoomStatus.CLOSED);
        assertThat(chatRoom.getStatus()).isEqualTo(ChatRoomStatus.CLOSED);
        assertThat(chatRoom.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("관리자는 종료된 채팅방 내용을 조회할 수 있다.")
    void adminCanReadClosedChatRoom() {
        // given
        Member admin = createMember(ADMIN_ID, MemberRole.ADMIN);
        ChatRoom chatRoom = ChatRoom.create(USER_ID, "상품 문의");
        ReflectionTestUtils.setField(chatRoom, "id", CHAT_ROOM_ID);
        chatRoom.joinAdmin(ADMIN_ID);
        chatRoom.closeByUser(USER_ID);

        given(memberRepository.findById(ADMIN_ID)).willReturn(Optional.of(admin));
        given(chatRoomRepository.findById(CHAT_ROOM_ID)).willReturn(Optional.of(chatRoom));
        given(chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(CHAT_ROOM_ID)).willReturn(List.of());

        // when
        GetChatRoomResponse response = chatRoomService.getOne(ADMIN_ID, CHAT_ROOM_ID);

        // then
        assertThat(response.status()).isEqualTo(ChatRoomStatus.CLOSED);
    }

    private Member createMember(Long memberId, MemberRole memberRole) {
        Member member = Member.create(
                memberRole.name().toLowerCase() + "@test.com",
                "password",
                memberRole.name(),
                "01012345678",
                memberRole
        );
        ReflectionTestUtils.setField(member, "id", memberId);

        return member;
    }
}
