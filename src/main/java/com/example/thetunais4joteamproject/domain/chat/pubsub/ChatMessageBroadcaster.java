package com.example.thetunais4joteamproject.domain.chat.pubsub;

import com.example.thetunais4joteamproject.domain.chat.dto.ChatMessageResponse;

public interface ChatMessageBroadcaster {

    void broadcast(ChatMessageResponse chatMessageResponse);
}