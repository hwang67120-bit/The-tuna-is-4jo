package com.example.thetunais4joteamproject.global.config;

import com.example.thetunais4joteamproject.domain.chat.pubsub.ChatMessageSubscriber;
import com.example.thetunais4joteamproject.domain.chat.pubsub.ChatRedisChannel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisPubSubConfig {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory,
            ChatMessageSubscriber chatMessageSubscriber
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(
                chatMessageSubscriber,
                new PatternTopic(ChatRedisChannel.CHAT_ROOM_CHANNEL_PATTERN)
        );

        return container;
    }
}