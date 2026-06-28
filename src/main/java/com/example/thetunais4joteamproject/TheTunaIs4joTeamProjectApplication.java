package com.example.thetunais4joteamproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication
public class TheTunaIs4joTeamProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(TheTunaIs4joTeamProjectApplication.class, args);
    }

}
