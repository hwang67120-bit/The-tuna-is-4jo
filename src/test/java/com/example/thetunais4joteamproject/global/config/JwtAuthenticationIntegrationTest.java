package com.example.thetunais4joteamproject.global.config;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.thetunais4joteamproject.domain.chat.controller.ChatRoomController;
import com.example.thetunais4joteamproject.domain.chat.dto.GetChatRoomListResponse;
import com.example.thetunais4joteamproject.domain.chat.service.ChatMessageService;
import com.example.thetunais4joteamproject.domain.chat.service.ChatRoomService;
import com.example.thetunais4joteamproject.domain.user.entity.MemberRole;
import com.example.thetunais4joteamproject.global.util.JwtProvider;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class JwtAuthenticationIntegrationTest {

    private static final long MEMBER_ID = 1L;
    private static final String JWT_SECRET = "and0LXRlc3Qtc2VjcmV0LWtleS1tdXN0LWJlLWxvbmctZW5vdWdoLWZvci1obWFjLXNpZ25pbmctMTIzNA==";
    private static final long ACCESS_TOKEN_EXPIRATION = 3_600_000L;

    private MockMvc mockMvc;
    private JwtProvider jwtProvider;
    private ChatRoomService chatRoomService;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(JWT_SECRET, ACCESS_TOKEN_EXPIRATION);
        chatRoomService = mock(ChatRoomService.class);
        ChatMessageService chatMessageService = mock(ChatMessageService.class);
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtProvider);
        ChatRoomController chatRoomController = new ChatRoomController(chatRoomService, chatMessageService);

        mockMvc = MockMvcBuilders.standaloneSetup(chatRoomController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .addFilters(jwtAuthenticationFilter)
                .build();
    }

    @Test
    @DisplayName("유효한 JWT로 보호된 채팅 목록 API를 호출하면 인증된 사용자 ID가 서비스로 전달된다.")
    void getChatRooms_WithValidJwt_PassesAuthenticatedMemberId() throws Exception {
        // given
        String accessToken = jwtProvider.createAccessToken(MEMBER_ID, MemberRole.USER);
        GetChatRoomListResponse getChatRoomListResponse = GetChatRoomListResponse.from(List.of());

        given(chatRoomService.getAll(MEMBER_ID)).willReturn(getChatRoomListResponse);

        // when & then
        mockMvc.perform(get("/api/chats")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk());

        verify(chatRoomService).getAll(MEMBER_ID);
    }

    @Test
    @DisplayName("잘못된 JWT로 보호된 채팅 목록 API를 호출하면 인증에 실패하고 서비스가 호출되지 않는다.")
    void getChatRooms_WithInvalidJwt_ReturnsUnauthorized() throws Exception {
        // when & then
        mockMvc.perform(get("/api/chats")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized());

        verify(chatRoomService, never()).getAll(anyLong());
    }
}
