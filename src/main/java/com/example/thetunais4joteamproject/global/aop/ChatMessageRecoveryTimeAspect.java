package com.example.thetunais4joteamproject.global.aop;

import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Slf4j
@Aspect
@Component
public class ChatMessageRecoveryTimeAspect {

	@Around("execution(* com.example.thetunais4joteamproject.domain.chat.service.ChatMessageService.getMessagesAfter(..))")
	public Object measureRecoveryMessageQueryTime(ProceedingJoinPoint joinPoint) throws Throwable {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		try {
			return joinPoint.proceed();
		} finally {
			stopWatch.stop();

			log.info(
				"[CHAT_MESSAGE_RECOVERY_TIME] method={}, executionTime={}ms",
				joinPoint.getSignature().toShortString(),
				stopWatch.getTotalTimeMillis()
			);
		}
	}
}
