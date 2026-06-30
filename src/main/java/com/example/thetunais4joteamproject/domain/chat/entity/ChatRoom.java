package com.example.thetunais4joteamproject.domain.chat.entity;

import com.example.thetunais4joteamproject.global.common.BaseEntity;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "chat_room")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ChatRoomStatus status;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    private ChatRoom(Long memberId, String title, ChatRoomStatus status) {
        this.memberId = memberId;
        this.title = title;
        this.status = status;
    }

    public static ChatRoom create(Long memberId, String title) {
        return new ChatRoom(memberId, title, ChatRoomStatus.WAITING);
    }

    public boolean isOwner(Long memberId) {
        return this.memberId.equals(memberId);
    }

    public void joinUser(Long memberId) {
        validateNotClosed();
        if (!isOwner(memberId)) {
            throw BusinessException.from(ErrorCode.FORBIDDEN);
        }
    }

    public void joinAdmin(Long adminId) {
        validateNotClosed();
        if (status == ChatRoomStatus.WAITING) {
            this.adminId = adminId;
            this.status = ChatRoomStatus.IN_PROGRESS;
            return;
        }
        if (this.adminId != null && this.adminId.equals(adminId)) {
            return;
        }

        throw BusinessException.from(ErrorCode.CONFLICT);
    }

    public void closeByUser(Long memberId) {
        validateNotClosed();
        if (!isOwner(memberId)) {
            throw BusinessException.from(ErrorCode.FORBIDDEN);
        }

        close();
    }

    public void closeByAdmin(Long adminId) {
        validateNotClosed();
        if (this.adminId == null || !this.adminId.equals(adminId)) {
            throw BusinessException.from(ErrorCode.FORBIDDEN);
        }

        close();
    }

    public void closeBySystem() {
        validateNotClosed();
        close();
    }

    private void close() {
        this.status = ChatRoomStatus.CLOSED;
        this.completedAt = LocalDateTime.now();
    }

    private void validateNotClosed() {
        if (status == ChatRoomStatus.CLOSED) {
            throw BusinessException.from(ErrorCode.CONFLICT);
        }
    }
}
