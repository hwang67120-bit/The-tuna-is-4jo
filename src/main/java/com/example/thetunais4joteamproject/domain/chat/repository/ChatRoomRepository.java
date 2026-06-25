package com.example.thetunais4joteamproject.domain.chat.repository;

import com.example.thetunais4joteamproject.domain.chat.ChatRoom;
import com.example.thetunais4joteamproject.domain.chat.ChatRoomStatus;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    boolean existsByMemberIdAndStatusIn(Long memberId, Collection<ChatRoomStatus> statuses);
}
