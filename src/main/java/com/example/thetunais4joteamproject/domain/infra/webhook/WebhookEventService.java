package com.example.thetunais4joteamproject.domain.infra.webhook;

import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

// 웹훅 이벤트의 수신/처리 이력을 관리하는 서비스
@Service
@RequiredArgsConstructor
@Transactional
public class WebhookEventService {

	private final WebhookEventRepository webhookEventRepository;

	public Optional<WebhookEvent> saveIfNotDuplicate(String webhookId, String type, String payload) {
		try {
			WebhookEvent event = webhookEventRepository.saveAndFlush(new WebhookEvent(webhookId, type, payload));
			return Optional.of(event);

		} catch (DataIntegrityViolationException e) {
			return Optional.empty();
		}
	}

	// 웹훅 본 처리 로직이 성공한 경우 호출. status를 PROCESSED로, finished_at을 now()로 바꾼다.
	public void markProcessed(Long eventId) {
		load(eventId).markAsProcessed();
	}

	// 처리할 필요 없는 이벤트(예: 우리가 모르는 type, 이미 취소된 결제 등)일 때 호출
	public void markIgnored(Long eventId, String reason) {
		load(eventId).markAsIgnored(reason);
	}

	// 본 처리 중 예외가 난 경우 호출. 재처리/모니터링을 위해 실패 사유를 남긴다.
	public void markFailed(Long eventId, String reason) {
		load(eventId).markAsFailed(reason);
	}

	// PK로 조회하되, 없으면 비즈니스 예외로 변환한다.
	private WebhookEvent load(Long eventId) {
		return webhookEventRepository.findById(eventId)
			.orElseThrow(() -> BusinessException.from(ErrorCode.WEBHOOK_EVENT_NOT_FOUND));
	}

}
