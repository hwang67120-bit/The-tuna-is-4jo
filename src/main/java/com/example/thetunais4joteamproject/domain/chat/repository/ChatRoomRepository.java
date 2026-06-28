package com.example.thetunais4joteamproject.domain.chat.repository;

import com.example.thetunais4joteamproject.domain.chat.entity.ChatRoom;
import com.example.thetunais4joteamproject.domain.chat.entity.ChatRoomStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    boolean existsByMemberIdAndStatusIn(Long memberId, Collection<ChatRoomStatus> statuses);

    List<ChatRoom> findAllByOrderByCreatedAtDesc();

    List<ChatRoom> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ChatRoom c where c.id = :chatRoomId")
    Optional<ChatRoom> findByIdForUpdate(@Param("chatRoomId") Long chatRoomId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select c
            from ChatRoom c
            where c.status in :statuses
              and c.createdAt < :threshold
              and not exists (
                  select m.id
                  from ChatMessage m
                  where m.chatRoomId = c.id
                    and m.createdAt > :threshold
              )
            """)
    List<ChatRoom> findInactiveRoomsForUpdate(
            @Param("statuses") Collection<ChatRoomStatus> statuses,
            @Param("threshold") LocalDateTime threshold
    );
}