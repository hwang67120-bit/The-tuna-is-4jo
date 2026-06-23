package com.example.thetunais4joteamproject.domain.chat;

import com.example.thetunais4joteamproject.global.common.BaseEntity;
import java.time.LocalDateTime;

public class ChatRoom extends BaseEntity {

    private Long id;
    private Long memberId;
    private String title;
    private String status;
    private LocalDateTime completedAt;
}
