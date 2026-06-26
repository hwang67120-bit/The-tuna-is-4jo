package com.example.thetunais4joteamproject.domain.infra.webhook;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.example.thetunais4joteamproject.domain.payment.entity.Payment;
import com.example.thetunais4joteamproject.domain.payment.entity.PaymentStatus;
import com.example.thetunais4joteamproject.domain.payment.port.PaymentGateway;
import com.example.thetunais4joteamproject.domain.payment.port.PaymentGatewayResponse;
import com.example.thetunais4joteamproject.domain.payment.service.PaymentCommandService;

import io.portone.sdk.server.webhook.Webhook;
import io.portone.sdk.server.webhook.WebhookTransactionCancelledCancelled;
import io.portone.sdk.server.webhook.WebhookTransactionPaid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * PortOne 웹훅 처리 핸들러
 *
 * [설계 원칙]
 *
 * 1. 외부 API 호출(PG 조회)은 트랜잭션 밖에서 실행한다.
 * 2. PG사(PortOne)가 보내주는 이벤트 페이로드를 그대로 믿지 않는다.
 * 3. Client 결제 확정(PaymentFacade.confirmPayment)과 중복 처리되지 않도록 payment.status를 확인해 이미 처리된 건은 상태 변경을 생략한다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookHandler {

    private final PaymentCommandService paymentCommandService;
    private final PaymentGateway paymentGateway;
    private final WebhookEventService webhookEventService;

    /**
     * 웹훅 진입점
     *
     * 흐름:
     *   1. 멱등성 체크 (webhook_id 중복 여부)
     *   2. 이벤트 DB 저장 (RECEIVED 상태로)
     *   3. 타입별 처리 분기
     *   4. 실패 시 FAILED 마킹
     */
    public void handle(String webhookId, Webhook webhook, String rawPayload) {

        // 수신한 웹훅의 "종류"를 문자열로 뽑아냄
        String type = webhook.getClass().getSimpleName();

        // 1. 멱등성: 같은 webhook_id가 이미 저장돼 있으면 중복 이벤트 -> 스킵
        //    PG가 네트워크 이슈로 같은 이벤트를 여러 번 보내도 한 번만 처리된다.
        Optional<WebhookEvent> saved = webhookEventService.saveIfNotDuplicate(webhookId, type, rawPayload);
        if (saved.isEmpty())
            return;
        Long eventId = saved.get().getId();

        // 2. 타입별 처리. 어떤 예외가 터져도 WebhookEvent는 FAILED로 기록되어야
        //    나중에 운영자가 실패 건을 추적·재처리할 수 있다.
        try {
            if (webhook instanceof WebhookTransactionPaid p) {
                handlePaid(eventId, p.getData().getPaymentId());
            } else if (webhook instanceof WebhookTransactionCancelledCancelled c) {
                handleCancel(eventId, c.getData().getPaymentId());
            } else {
                // 우리가 처리 대상으로 삼지 않는 이벤트 타입 (예: Transaction.Ready)
                // -> 무시하되, 이벤트는 받았다는 기록을 남긴다.
                webhookEventService.markIgnored(eventId, "처리 대상 아님: " + type);
            }
        } catch (Exception e) {
            log.error("[Webhook] failed eventId={}", eventId, e);
            webhookEventService.markFailed(eventId, e.getMessage());
        }
    }

    /**
     * 결제 완료(Transaction.Paid) 웹훅 처리
     *
     * 이 웹훅이 오는 시나리오:
     *  - Client가 confirmPayment를 호출하기 전에 PG가 먼저 웹훅을 보낸 경우
     *  - Client가 confirmPayment 응답을 못 받고 화면을 닫은 경우(네트워크 끊김)
     *  - 이미 Client에서 승인 처리가 끝난 뒤 도착한 보조 확인용 웹훅
     */
    private void handlePaid(Long eventId, String portonePaymentId) {
        // [보안] PG 페이로드는 신뢰하지 않고 실체를 직접 조회한다.
        PaymentGatewayResponse pg = paymentGateway.getPayment(portonePaymentId);

        // PG 실체 상태가 PAID가 아니면 처리 대상이 아님
        if (!"PAID".equals(pg.status())) {
            webhookEventService.markIgnored(eventId, "PG 상태가 PAID가 아님: " + pg.status());
            return;
        }

        Payment payment = paymentCommandService.findByPortonePaymentId(portonePaymentId);

        // 우리가 생성한 결제 금액(DB)과 PG가 실제로 승인한 금액이 다르면
        // 프론트엔드가 조작되었거나 외부 공격이 있었다는 뜻 -> FAILED로 기록
        if (pg.totalAmount() != payment.getPgAmount()) {
            webhookEventService.markFailed(eventId,
                "금액 불일치: db=" + payment.getPgAmount() + ", pg=" + pg.totalAmount());
            return;
        }

        // -> Client 경로와 웹훅 경로가 서로 다른 시점에 도착해도 결과는 한 번만 반영된다.
        if (payment.getStatus() == PaymentStatus.PENDING) {
            paymentCommandService.completePayment(payment);
        }

        // 상태 변경을 수행했든 스킵했든 "이 이벤트는 처리 완료"로 기록한다.
        // PG가 같은 이벤트를 재전송하지 않도록 200 응답의 근거가 된다.
        webhookEventService.markProcessed(eventId);
    }

    /**
     * 결제 취소(Transaction.Cancelled) 웹훅 처리
     *
     * 이 웹훅이 오는 시나리오:
     *  - 우리가 /payments/{id}/cancel을 호출한 뒤 PG가 확인용으로 보낸 웹훅
     *  - PG 관리자 콘솔에서 수동 취소한 경우
     *  - 카드사 측에서 취소가 역행된 경우
     */
    private void handleCancel(Long eventId, String portonePaymentId) {
        // [보안] PG 실체를 직접 조회 (외부 API, 트랜잭션 밖)
        PaymentGatewayResponse pg = paymentGateway.getPayment(portonePaymentId);

        // PG 실체가 CANCELLED가 아니면 처리 대상이 아님
        if (!"CANCELLED".equals(pg.status())) {
            webhookEventService.markIgnored(eventId, "PG 상태가 CANCELLED가 아님: " + pg.status());
            return;
        }

        Payment payment = paymentCommandService.findByPortonePaymentId(portonePaymentId);

        // [중복 처리 방지]
        if (payment.getStatus() == PaymentStatus.PAID) {
            paymentCommandService.cancelPayment(payment.getOrder());
        }

        webhookEventService.markProcessed(eventId);
    }

}

