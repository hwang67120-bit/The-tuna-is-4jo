package com.example.thetunais4joteamproject.domain.chat.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRoomAutoCloseScheduler {

    private static final long CHECK_DELAY_MILLISECONDS = 60_000L;
    private static final long INACTIVE_MINUTES = 3L;

    private final ChatRoomService chatRoomService;

    @Scheduled(fixedDelay = CHECK_DELAY_MILLISECONDS)
    public void closeInactiveChatRooms() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(INACTIVE_MINUTES);
        int closedCount = chatRoomService.closeInactiveChatRooms(threshold);
        if (closedCount > 0) {
            log.info("Inactive chat rooms closed. threshold={}, closedCount={}", threshold, closedCount);
        }
    }
}