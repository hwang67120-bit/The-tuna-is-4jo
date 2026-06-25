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

    // [상품(Product) 도메인 비즈니스 에러 코드]
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다"),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 상품입니다"),
    OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 옵션입니다"),
    DEFAULT_OPTION_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "상품의 기본 옵션을 찾을 수 없습니다. 시스템 오류입니다"),
    OUT_OF_STOCK(HttpStatus.CONFLICT, "상품 재고가 부족합니다"),
    PRODUCT_OPTION_NOT_ON_SALE(HttpStatus.CONFLICT, "판매 중인 상품 옵션이 아닙니다"),

    // [장바구니(Cart) 도메인 비즈니스 에러 코드]
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 장바구니입니다"),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 장바구니 상품입니다"),
    INVALID_CART_ITEM_QUANTITY(HttpStatus.BAD_REQUEST, "장바구니 상품 수량은 1개 이상이어야 합니다"),

    // [회원(Member) 도메인 비즈니스 에러 코드]
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다");

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
