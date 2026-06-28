package com.example.thetunais4joteamproject.domain.infra.webhook;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.thetunais4joteamproject.global.common.ApiResponse;
import com.example.thetunais4joteamproject.global.error.BusinessException;
import com.example.thetunais4joteamproject.global.error.ErrorCode;
import com.example.thetunais4joteamproject.global.error.ErrorResponse;

import io.portone.sdk.server.errors.WebhookVerificationException;
import io.portone.sdk.server.webhook.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final PortOneWebhookVerifier portOneWebhookVerifier;
    private final WebhookHandler webhookHandler;

    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<Void>> receiveWebhook(
        @RequestBody String body,
        @RequestHeader("webhook-id") String webhookId,
        @RequestHeader("webhook-signature") String signature,
        @RequestHeader("webhook-timestamp") String timestamp
    ) {
        try {
            Webhook webhook = portOneWebhookVerifier.verify(body, webhookId, signature, timestamp);
            webhookHandler.handle(webhookId, webhook, body);

            return ResponseEntity.ok(ApiResponse.ok(null));

        } catch (WebhookVerificationException e) {
            log.warn("[Webhook] signature verification failed. webhookId={}", webhookId, e);
            throw BusinessException.from(ErrorCode.WEBHOOK_VERIFICATION_FAILED);
        }
    }
}
