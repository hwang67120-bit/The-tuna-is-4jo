package com.example.thetunais4joteamproject.global.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "요청 내용을 확인해 주세요"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "찾을 수 없습니다"),
    CONFLICT(HttpStatus.CONFLICT, "요청을 처리할 수 없습니다"),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "현재 서비스를 이용할 수 없습니다"),
    GATEWAY_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "응답 시간이 초과되었습니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 장애가 발생했습니다"),

    // [상품(Product) 도메인 비즈니스 에러 코드]
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다"),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 상품입니다"),
    OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 옵션입니다"),
    DEFAULT_OPTION_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "상품의 기본 옵션을 찾을 수 없습니다. 시스템 오류입니다"),
    OUT_OF_STOCK(HttpStatus.CONFLICT, "상품 재고가 부족합니다"),
    PRODUCT_OPTION_NOT_ON_SALE(HttpStatus.CONFLICT, "판매 중인 상품 옵션이 아닙니다"),
    PRODUCT_OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "선택한 상품 옵션의 재고가 부족하여 주문할 수 없습니다."),

    // [장바구니(Cart) 도메인 비즈니스 에러 코드]
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 장바구니입니다"),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 장바구니 상품입니다"),
    INVALID_CART_ITEM_QUANTITY(HttpStatus.BAD_REQUEST, "장바구니 상품 수량은 1개 이상이어야 합니다"),
    CART_EMPTY(HttpStatus.BAD_REQUEST, "장바구니가 비어 있습니다"),

	// [주문(Order) 도메인 비즈니스 에러 코드]
	ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 주문입니다"),
	INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "변경할 수 없는 주문 상태입니다"),
	INVALID_ORDER_QUANTITY(HttpStatus.BAD_REQUEST, "주문 수량은 1개 이상이어야 합니다"),
	ORDER_ALREADY_PENDING(HttpStatus.CONFLICT, "이미 결제 대기 중인 주문이 있습니다"),

	// [결제(Payment) 도메인 비즈니스 에러 코드]
	ALREADY_PROCESSED_PAYMENT(HttpStatus.BAD_REQUEST, "이미 결제를 완료하였습니다."),
	INVALID_PAYMENT_STATUS(HttpStatus.BAD_REQUEST, "변경할 수 없는 결제 상태입니다"),
    PAYMENT_INVALID_STATUS(HttpStatus.BAD_REQUEST, "변경할 수 없는 결제 상태입니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    PORTONE_PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "포트원 결제 아이디를 찾을 수 없습니다."),
    PAYMENT_ALREADY_FAILED(HttpStatus.BAD_REQUEST, "이미 실패한 결제입니다."),
    PAYMENT_ALREADY_CANCELED(HttpStatus.BAD_REQUEST, "이미 실패한 결제입니다."),
    PG_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "결제사 서버와 통신 중 오류가 발생했습니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "금액이 일치하지 않습니다."),
    PAYMENT_NOT_PAID(HttpStatus.BAD_REQUEST, "결제가 완료되지 않았습니다."),
    PAYMENT_ORDER_MISMATCH(HttpStatus.BAD_REQUEST, "결제와 주문이 일치하지 않습니다."),

    // [환불(Refund) 도메인 비즈니스 에러 코드]
    INVALID_REFUND_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "변경할 수 없는 환불 상태입니다."),
    REFUND_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 환불을 찾을 수 없습니다."),
    INVALID_REFUND_AMOUNT(HttpStatus.BAD_REQUEST, "금액이 맞지 않습니다."),
    ALREADY_REQUESTED_REFUND(HttpStatus.BAD_REQUEST,"이미 환불된 결제 건입니다."),
    INVALID_REFUND_STATUS(HttpStatus.BAD_REQUEST, "환불 상태가 존재하지 않습니다."),

    // [웹훅(Webhook) 도메인 비즈니스 에러 코드]
    WEBHOOK_EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "주문 금액보다 많이 사용할 수 없습니다."),
    WEBHOOK_VERIFICATION_FAILED(HttpStatus.UNAUTHORIZED, "웹훅 서명 인증에 실패하였습니다."),

    // [회원(Member) 도메인 비즈니스 에러 코드]
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다"),

    // [쿠폰(Coupon) 도메인 비즈니스 에러 코드]
    INVALID_COUPON_EXPIRATION(HttpStatus.BAD_REQUEST, "쿠폰 만료 일시는 현재 시간보다 과거일 수 없습니다."),
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 쿠폰입니다."),
    COUPON_ALREADY_ISSUED(HttpStatus.BAD_REQUEST, "이미 발급받은 쿠폰입니다."),
    COUPON_OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "쿠폰 수량이 모두 소진되었습니다."),
    COUPON_EXPIRED(HttpStatus.BAD_REQUEST, "유효기간이 만료된 쿠폰입니다."),
    INVALID_COUPON_ORDER_PRICE(HttpStatus.BAD_REQUEST, "주문 금액이 쿠폰의 최소 주문 금액 조건을 충족하지 못했습니다."),
    INVALID_COUPON_DISCOUNT_PRICE(HttpStatus.BAD_REQUEST, "쿠폰 할인 금액은 주문 금액보다 클 수 없습니다."),
    COUPON_NOT_USED(HttpStatus.BAD_REQUEST, "사용 완료 상태의 쿠폰만 복구할 수 있습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }
}
