package com.example.thetunais4joteamproject.domain.chat.repository;

import com.example.thetunais4joteamproject.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
}
