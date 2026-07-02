package com.example.thetunais4joteamproject.domain.infra.webhook;

import org.springframework.stereotype.Component;

import com.example.thetunais4joteamproject.domain.infra.portone.config.PortOneProperties;

import io.portone.sdk.server.errors.WebhookVerificationException;
import io.portone.sdk.server.webhook.Webhook;
import io.portone.sdk.server.webhook.WebhookVerifier;

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
