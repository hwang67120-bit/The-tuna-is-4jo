package com.example.thetunais4joteamproject.global.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        // 로컬 환경의 Embedded Redis 또는 외부 Redis에 접근하기 위해 주소를 설정합니다.
        config.useSingleServer()
              .setAddress("redis://127.0.0.1:6379");
              
        return Redisson.create(config);
    }
}
