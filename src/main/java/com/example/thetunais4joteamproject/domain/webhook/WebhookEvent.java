package com.example.thetunais4joteamproject.domain.webhook;

import java.time.LocalDateTime;

public class WebhookEvent {

    private Long id;
    private Long paymentId;
    private String portoneEventId;
    private String portonePaymentId;
    private String eventType;
    private String status;
    private String payload;
    private String failureReason;
    private LocalDateTime receivedAt;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
}
