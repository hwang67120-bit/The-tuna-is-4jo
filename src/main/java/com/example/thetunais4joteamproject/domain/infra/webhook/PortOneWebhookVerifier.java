package com.example.thetunais4joteamproject.domain.infra.webhook;

import org.springframework.stereotype.Component;

import com.example.thetunais4joteamproject.domain.infra.portone.config.PortOneProperties;

import io.portone.sdk.server.errors.WebhookVerificationException;
import io.portone.sdk.server.webhook.Webhook;
import io.portone.sdk.server.webhook.WebhookVerifier;

//PortOne 공식 Server SDK를 이용한 웹훅 시그니처 검증기
@Component
public class PortOneWebhookVerifier {

    private final WebhookVerifier webhookVerifier;

    public PortOneWebhookVerifier(PortOneProperties properties) {
        this.webhookVerifier = new WebhookVerifier(properties.getWebhookSecret());
    }

    // 검증 시에만 webhook 객체 반환
    public Webhook verify(String body, String webhookId, String signature, String timestamp) throws
        WebhookVerificationException {
        return webhookVerifier.verify(body, webhookId, signature, timestamp);
    }
}
