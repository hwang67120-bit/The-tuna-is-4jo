package com.example.thetunais4joteamproject.domain.infra.webhook;

public enum WebhookStatus {
    RECEIVED, // 수신 완료, 처리 전
    PROCESSED, // 정상 처리 완료
    IGNORED, // 알고는 있으나 처리 대상이 아닌 이벤트
    FAILED // 처리 중 에러 발생 -> 재처리 대상
}
