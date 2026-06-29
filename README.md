# The Tuna Is 4jo Team Project

## 프로젝트 파일 구조

> 현재 프로젝트 기준 패키지 구조입니다. 도메인별 책임을 분리하고, 공통 설정과 예외 처리는 `global`에서 관리합니다.

```text
src/
├── main
│   ├── java/com/example/thetunais4joteamproject/
│   │   ├── TheTunaIs4joTeamProjectApplication.java
│   │   │
│   │   ├── global                         # 전역 공통 계층
│   │   │   ├── aop                        # 채팅 복구 API 시간 측정 등 공통 AOP
│   │   │   ├── common                     # BaseEntity, 공통 API 응답
│   │   │   ├── config                     # Security, JWT Filter, Redis, WebSocket, Querydsl 설정
│   │   │   ├── entity                     # 전역 공통 엔티티
│   │   │   ├── error                      # 공통 예외, 에러 코드, 예외 응답
│   │   │   └── util                       # JwtProvider, PasswordEncryptor 등 유틸
│   │   │
│   │   └── domain                         # 도메인 비즈니스 로직 계층
│   │       ├── user                       # 회원, 로그인, JWT 발급
│   │       │   ├── controller
│   │       │   ├── dto
│   │       │   ├── entity
│   │       │   ├── repository
│   │       │   └── service
│   │       │
│   │       ├── product                    # 상품, 카테고리, 검색, 캐시
│   │       │   ├── cache
│   │       │   ├── controller
│   │       │   ├── dto
│   │       │   ├── entity
│   │       │   ├── repository
│   │       │   │   ├── custom             # Querydsl 커스텀 Repository 인터페이스
│   │       │   │   └── impl               # Querydsl 커스텀 Repository 구현체
│   │       │   └── service
│   │       │
│   │       ├── cart                       # 장바구니
│   │       │   ├── controller
│   │       │   ├── dto
│   │       │   ├── entity
│   │       │   ├── facade
│   │       │   ├── repository
│   │       │   └── service
│   │       │
│   │       ├── order                      # 주문, 주문 스케줄러
│   │       │   ├── controller
│   │       │   ├── dto
│   │       │   ├── entity
│   │       │   ├── facade
│   │       │   ├── repository
│   │       │   ├── scheduler
│   │       │   └── service
│   │       │
│   │       ├── payment                    # 결제, 결제 포트/파사드
│   │       │   ├── controller
│   │       │   ├── dto
│   │       │   ├── entity
│   │       │   ├── facade
│   │       │   ├── port
│   │       │   ├── repository
│   │       │   └── service
│   │       │
│   │       ├── refund                     # 환불
│   │       │   ├── controller
│   │       │   ├── repository
│   │       │   └── service
│   │       │
│   │       ├── coupon                     # 쿠폰, 분산락, 쿠폰 스케줄러
│   │       │   ├── aop
│   │       │   ├── controller
│   │       │   ├── dto
│   │       │   ├── entity
│   │       │   ├── facade
│   │       │   ├── repository
│   │       │   ├── scheduler
│   │       │   └── service
│   │       │
│   │       ├── chat                       # STOMP 채팅, 재연결 복구, Redis Pub/Sub
│   │       │   ├── controller
│   │       │   ├── dto
│   │       │   ├── entity
│   │       │   ├── pubsub                 # Redis Publisher/Subscriber, 브로드캐스터
│   │       │   ├── repository
│   │       │   └── service
│   │       │
│   │       └── infra                      # 외부 인프라 연동
│   │           ├── portone
│   │           │   ├── client
│   │           │   ├── config
│   │           │   ├── controller
│   │           │   └── dto
│   │           └── webhook                # PortOne 웹훅 처리
│   │
│   └── resources
│       └── static                         # 테스트/데모용 정적 화면
│
└── test
    ├── java/com/example/thetunais4joteamproject/
    │   ├── domain                         # 도메인별 단위/통합 테스트
    │   │   ├── chat                       # Pub/Sub, 상태 변화 테스트
    │   │   ├── coupon
    │   │   ├── infra
    │   │   ├── order
    │   │   ├── payment
    │   │   ├── product
    │   │   └── user                       # 로그인/JWT 테스트
    │   └── global                         # 보안 설정/JWT 필터 테스트
    └── k6                                # 채팅 메시지 복구 성능 테스트 스크립트
```

## API 요약

### 공통

- 인증이 필요한 API는 JWT Access Token을 사용한다.
- 관리자 API는 관리자 권한이 필요하다.
- 실패 응답은 공통 예외 응답 규칙을 따른다.
- README API 요약에는 상세 필드명과 데이터 타입을 작성하지 않는다.

| 상태 코드 | 메시지 |
| ---: | --- |
| 400 | 요청 내용을 확인해 주세요 |
| 401 | 로그인이 필요합니다 |
| 404 | 찾을 수 없습니다 |
| 409 | 요청을 처리할 수 없습니다 |
| 503 | 현재 서비스를 이용할 수 없습니다 |
| 504 | 응답 시간이 초과되었습니다 |

---

### 사용자

| 기능 | Method | API Path | 인증 | 요청 | 응답 |
| --- | --- | --- | --- | --- | --- |
| 이메일 중복 확인 | GET | `/api/members/email-check` | X | 이메일 중복 확인 요청 | 이메일 사용 가능 여부 반환 |
| 회원가입 | POST | `/api/members/signup` | X | 회원가입 정보 전달 | 회원 ID 반환 |
| 로그인 | POST | `/api/members/login` | X | 로그인 정보 전달 | 인증 토큰 반환 |
| 로그아웃 | POST | `/api/members/logout` | O | 로그아웃 요청 | 로그아웃 성공 반환 |
| 내 정보 조회 | GET | `/api/members/me` | O | 내 정보 조회 요청 | 회원 정보 반환 |
| 내 정보 수정 | PUT | `/api/members/me` | O | 수정할 회원 정보 전달 | 수정 결과 반환 |

### 상품

| 기능 | Method | API Path | 인증 | 요청 | 응답 |
| --- | --- | --- | --- | --- | --- |
| 상품 생성 | POST | `/api/products` | 관리자 | 상품 생성 정보 전달 | 상품 ID 반환 |
| 상품 목록 조회 | GET | `/api/products` | X | 상품 목록 조회 요청 | 상품 목록 반환 |
| 상품 상세 조회 | GET | `/api/products/{productId}` | X | 상품 ID 전달 | 상품 상세 정보 반환 |
| 상품 카테고리 조회 | GET | `/api/products/categories/{categoryId}` | X | 카테고리 ID 전달 | 카테고리 상품 목록 반환 |

### 장바구니

| 기능 | Method | API Path | 인증 | 요청 | 응답 |
| --- | --- | --- | --- | --- | --- |
| 상품 담기 | POST | `/api/carts/items` | O | 담을 상품과 수량 전달 | 장바구니 상품 ID 반환 |
| 장바구니 조회 | GET | `/api/carts` | O | 장바구니 조회 요청 | 장바구니 상품 목록 반환 |
| 장바구니 상품 수량 변경 | PUT | `/api/carts/items/{cartItemId}` | O | 변경할 수량 전달 | 변경 결과 반환 |
| 장바구니 상품 개별 삭제 | DELETE | `/api/carts/items/{cartItemId}` | O | 삭제할 장바구니 상품 ID 전달 | 삭제 성공 반환 |
| 장바구니 전체 비우기 | DELETE | `/api/carts` | O | 전체 비우기 요청 | 삭제 성공 반환 |

### 주문

| 기능 | Method | API Path | 인증 | 요청 | 응답 |
| --- | --- | --- | --- | --- | --- |
| 주문서 미리보기 | GET | `/api/orders/preview` | O | 장바구니와 쿠폰 정보 전달 | 결제 예정 금액 반환 |
| 주문 생성 | POST | `/api/orders` | O | 장바구니 기준 주문 요청 | 주문 정보 반환 |
| 주문 목록 조회 | GET | `/api/orders` | O | 주문 목록 조회 요청 | 주문 목록 반환 |
| 주문 상세 조회 | GET | `/api/orders/{orderId}` | O | 주문 ID 전달 | 주문 상세 정보 반환 |
| 주문 취소 | POST | `/api/orders/{orderId}/cancel` | O | 주문 취소 요청 | 주문 취소 결과 반환 |

### 결제

| 기능 | Method | API Path | 인증 | 요청 | 응답 |
| --- | --- | --- | --- | --- | --- |
| 결제 확정 | POST | `/api/payments/confirm` | O | 결제 결과 정보 전달 | 결제 확정 결과 반환 |
| 결제 취소 | POST | `/api/payments/{paymentId}/cancel` | O | 결제 취소 요청 | 결제 취소 결과 반환 |
| 웹훅 수신 | POST | `/api/webhooks/portone` | X | PortOne 웹훅 수신 | 웹훅 처리 결과 반환 |

### 환불

| 기능 | Method | API Path | 인증 | 요청 | 응답 |
| --- | --- | --- | --- | --- | --- |
| 환불 요청 | POST | `/api/refunds` | O | 환불 요청 정보 전달 | 환불 요청 결과 반환 |
| 환불 조회 | GET | `/api/refunds/{refundId}` | O | 환불 ID 전달 | 환불 상세 정보 반환 |
| 환불 승인 | POST | `/api/admin/refunds/{refundId}/approve` | 관리자 | 환불 승인 요청 | 환불 승인 결과 반환 |
| 환불 거절 | POST | `/api/admin/refunds/{refundId}/reject` | 관리자 | 환불 거절 요청 | 환불 거절 결과 반환 |

### 쿠폰

| 기능 | Method | API Path | 인증 | 요청 | 응답 |
| --- | --- | --- | --- | --- | --- |
| 쿠폰 생성 | POST | `/api/admin/coupons` | 관리자 | 쿠폰 생성 정보 전달 | 쿠폰 ID 반환 |
| 쿠폰 발급 | POST | `/api/coupons/{couponId}/issue` | O | 발급할 쿠폰 ID 전달 | 발급 결과 반환 |
| 보유 쿠폰 조회 | GET | `/api/coupons` | O | 보유 쿠폰 조회 요청 | 보유 쿠폰 목록 반환 |

### 채팅

| 기능 | Method | API Path | 인증 | 요청 | 응답 |
| --- | --- | --- | --- | --- | --- |
| 채팅 생성 | POST | `/api/chatrooms` | O | 문의 제목과 내용 전달 | 채팅방 ID 반환 |
| 채팅 참여 | POST | `/api/chatrooms/{chatRoomId}/join` | O | 채팅방 참여 요청 | 참여 결과 반환 |
| 채팅방 목록 조회 | GET | `/api/admin/chatrooms` | 관리자 | 채팅방 목록 조회 요청 | 채팅방 목록 반환 |
| 채팅 내역 조회 | GET | `/api/chatrooms/{chatRoomId}/messages` | O | 채팅방 ID 전달 | 메시지 목록 반환 |
| 메시지 전송 | WebSocket | `/pub/chatrooms/{chatRoomId}/messages` | O | 메시지 전송 | 메시지 전송 결과 반환 |
| 메시지 구독 | WebSocket | `/sub/chatrooms/{chatRoomId}` | O | 채팅방 구독 | 실시간 메시지 수신 |
---

## ERD 요약

### 핵심 연관관계

| 관계 | 설명 |
| --- | --- |
| `MEMBER 1:1 CART` | 회원은 하나의 장바구니를 가집니다. |
| `MEMBER 1:N PRODUCT` | 회원은 여러 상품을 등록할 수 있습니다. |
| `CATEGORY 1:N PRODUCT` | 카테고리는 여러 상품을 포함합니다. |
| `PRODUCT 1:N PRODUCT_OPTION` | 상품은 여러 옵션을 가질 수 있습니다. |
| `CART 1:N CART_ITEM` | 장바구니는 여러 장바구니 상품을 포함합니다. |
| `PRODUCT_OPTION 1:N CART_ITEM` | 장바구니 상품은 상품 옵션을 참조합니다. |
| `MEMBER 1:N ORDERS` | 회원은 여러 주문을 생성할 수 있습니다. |
| `ORDERS 1:N ORDER_ITEM` | 주문은 여러 주문 상품을 포함합니다. |
| `PRODUCT_OPTION 1:N ORDER_ITEM` | 주문 상품은 주문 당시 상품 옵션을 참조합니다. |
| `ORDERS 1:1 PAYMENT` | 주문은 하나의 결제 정보와 연결됩니다. |
| `COUPONS 1:N MEMBER_COUPONS` | 쿠폰은 여러 회원에게 발급될 수 있습니다. |
| `MEMBER 1:N MEMBER_COUPONS` | 회원은 여러 쿠폰을 보유할 수 있습니다. |
| `MEMBER 1:N CHAT_ROOM` | 회원은 문의 채팅방을 개설할 수 있습니다. |
| `CHAT_ROOM 1:N CHAT_MESSAGE` | 채팅방은 여러 메시지를 기록합니다. |
| `WEBHOOK_EVENTS` | PortOne 웹훅 이벤트 처리 상태를 기록합니다. |

### 주요 테이블 필드

#### MEMBER

| 필드명 | 타입 | 키 | 설명 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 회원 ID |
| email | VARCHAR(100) | UK | 로그인 이메일 |
| password | VARCHAR(255) |  | 암호화된 비밀번호 |
| name | VARCHAR(50) |  | 이름 |
| phone_number | VARCHAR(20) |  | 전화번호 |
| role | VARCHAR(20) |  | USER, ADMIN |
| created_at | DATETIME |  | 생성일시 |
| updated_at | DATETIME |  | 수정일시 |

#### CATEGORY

| 필드명 | 타입 | 키 | 설명 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 카테고리 ID |
| name | VARCHAR(50) |  | 카테고리명 |
| created_at | DATETIME |  | 생성일시 |
| updated_at | DATETIME |  | 수정일시 |

#### PRODUCT

| 필드명 | 타입 | 키 | 설명 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 상품 ID |
| member_id | BIGINT | FK | 상품 등록 회원 ID |
| category_id | BIGINT | FK | 카테고리 참조 |
| name | VARCHAR(255) |  | 상품명 |
| price | INTEGER |  | 기본 판매가 |
| description | TEXT |  | 상품 설명 |
| status | VARCHAR(20) |  | ON_SALE, DISCONTINUED, DELETED |
| created_at | DATETIME |  | 생성일시 |
| updated_at | DATETIME |  | 수정일시 |

#### PRODUCT_OPTION

| 필드명 | 타입 | 키 | 설명 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 상품 옵션 ID |
| product_id | BIGINT | FK | 상품 참조 |
| option_name | VARCHAR(100) |  | 옵션명 |
| option_stock | INTEGER |  | 옵션 재고 |
| additional_price | INTEGER |  | 추가 금액 |
| status | VARCHAR(20) |  | ON_SALE, SOLDOUT, DISCONTINUED |
| created_at | DATETIME |  | 생성일시 |
| updated_at | DATETIME |  | 수정일시 |

#### CART

| 필드명 | 타입 | 키 | 설명 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 장바구니 ID |
| member_id | BIGINT | FK, UK | 회원 참조 |
| created_at | DATETIME |  | 생성일시 |
| updated_at | DATETIME |  | 수정일시 |

#### CART_ITEM

| 필드명 | 타입 | 키 | 설명 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 장바구니 상품 ID |
| cart_id | BIGINT | FK | 장바구니 참조 |
| product_option_id | BIGINT | FK | 상품 옵션 참조 |
| quantity | INTEGER |  | 수량 |
| created_at | DATETIME |  | 생성일시 |
| updated_at | DATETIME |  | 수정일시 |

#### ORDERS

| 필드명 | 타입 | 키 | 설명 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 주문 ID |
| member_id | BIGINT | FK | 회원 참조 |
| order_number | VARCHAR(50) | UK | 노출용 고유 주문번호 |
| order_price | INTEGER |  | 주문 상품 금액 |
| discount_price | INTEGER |  | 할인 금액 |
| delivery_price | INTEGER |  | 배송비 |
| total_amount | INTEGER |  | 최종 결제 금액 |
| status | VARCHAR(30) |  | PENDING_PAYMENT, CONFIRMED, CANCELED, EXPIRED |
| created_at | DATETIME |  | 생성일시 |
| updated_at | DATETIME |  | 수정일시 |

#### ORDER_ITEM

| 필드명 | 타입 | 키 | 설명 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 주문 상품 ID |
| order_id | BIGINT | FK | 주문 참조 |
| product_option_id | BIGINT | FK | 상품 옵션 참조 |
| cart_item_id | BIGINT |  | 장바구니 상품 스냅샷 ID, Nullable |
| product_id | BIGINT |  | 주문 당시 상품 ID 스냅샷 |
| product_name | VARCHAR(255) |  | 주문 당시 상품명 스냅샷 |
| option_name | VARCHAR(255) |  | 주문 당시 옵션명 스냅샷 |
| unit_price | INTEGER |  | 주문 당시 단가 |
| quantity | INTEGER |  | 수량 |
| total_price | INTEGER |  | 주문 상품 총액 |
| created_at | DATETIME |  | 생성일시 |
| updated_at | DATETIME |  | 수정일시 |

#### PAYMENT

| 필드명 | 타입 | 키 | 설명 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 결제 ID |
| order_id | BIGINT | FK, UK | 주문 참조 |
| portone_payment_id | VARCHAR(50) | UK | PortOne 결제 식별자 |
| requested_amount | INTEGER |  | 결제 요청 금액 |
| pg_amount | INTEGER |  | PG 실제 결제 금액 |
| status | VARCHAR(20) |  | PENDING, PAID, FAILED, CANCELED, REFUNDED |
| paid_at | DATETIME |  | 결제 완료 일시, Nullable |
| created_at | DATETIME |  | 생성일시 |
| updated_at | DATETIME |  | 수정일시 |

#### COUPONS

| 필드명 | 타입 | 키 | 설명 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 쿠폰 ID |
| name | VARCHAR(255) |  | 쿠폰 이름 |
| discount_price | INTEGER |  | 할인 금액 |
| min_order_price | INTEGER |  | 최소 주문 금액 |
| total_quantity | INTEGER |  | 총 발급 가능 수량 |
| remaining_quantity | INTEGER |  | 남은 발급 가능 수량 |
| coupon_status | VARCHAR(20) |  | ACTIVE, DISABLED |
| expiration_at | DATETIME |  | 만료 일시 |
| created_at | DATETIME |  | 생성일시 |
| updated_at | DATETIME |  | 수정일시 |

#### MEMBER_COUPONS

| 필드명 | 타입 | 키 | 설명 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 발급 쿠폰 ID |
| member_id | BIGINT | FK | 회원 참조 |
| coupon_id | BIGINT | FK | 쿠폰 참조 |
| coupon_status | VARCHAR(20) |  | UNUSED, USED, EXPIRED |
| used_at | DATETIME |  | 사용 일시, Nullable |
| created_at | DATETIME |  | 생성일시 |
| updated_at | DATETIME |  | 수정일시 |

#### CHAT_ROOM

| 필드명 | 타입 | 키 | 설명 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 채팅방 ID |
| member_id | BIGINT | FK | 문의 회원 참조 |
| admin_id | BIGINT | FK | 담당 관리자, Nullable |
| title | VARCHAR(100) |  | 문의 제목 |
| status | VARCHAR(20) |  | WAITING, IN_PROGRESS, CLOSED |
| completed_at | DATETIME |  | 종료 일시, Nullable |
| created_at | DATETIME |  | 생성일시 |
| updated_at | DATETIME |  | 수정일시 |

#### CHAT_MESSAGE

| 필드명 | 타입 | 키 | 설명 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 메시지 ID |
| chat_room_id | BIGINT | FK | 채팅방 참조 |
| sender_id | BIGINT | FK | 발신 회원 참조 |
| content | TEXT |  | 채팅 내용 |
| message_type | VARCHAR(20) |  | USER, ADMIN, SYSTEM |
| created_at | DATETIME |  | 생성일시 |
| updated_at | DATETIME |  | 수정일시 |

#### WEBHOOK_EVENTS

| 필드명 | 타입 | 키 | 설명 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 웹훅 이벤트 ID |
| webhook_id | VARCHAR(200) | UK | PortOne 웹훅 식별자 |
| type | VARCHAR(100) |  | 웹훅 이벤트 타입 |
| status | VARCHAR(20) |  | RECEIVED, PROCESSED, IGNORED, FAILED |
| payload | TEXT |  | 웹훅 원본 페이로드 |
| finished_at | DATETIME |  | 처리 완료 일시, Nullable |
| failure_reason | VARCHAR(500) |  | 실패 또는 무시 사유, Nullable |
| created_at | DATETIME |  | 생성일시 |
| updated_at | DATETIME |  | 수정일시 |

### 주요 상태 값

| 구분 | 상태 값 |
| --- | --- |
| 회원 권한 | `USER`, `ADMIN` |
| 상품 상태 | `ON_SALE`, `DISCONTINUED`, `DELETED` |
| 상품 옵션 상태 | `ON_SALE`, `SOLDOUT`, `DISCONTINUED` |
| 주문 상태 | `PENDING_PAYMENT`, `CONFIRMED`, `CANCELED`, `EXPIRED` |
| 결제 상태 | `PENDING`, `PAID`, `FAILED`, `CANCELED`, `REFUNDED` |
| 쿠폰 상태 | `ACTIVE`, `DISABLED` |
| 발급 쿠폰 상태 | `UNUSED`, `USED`, `EXPIRED` |
| 채팅방 상태 | `WAITING`, `IN_PROGRESS`, `CLOSED` |
| 메시지 타입 | `USER`, `ADMIN`, `SYSTEM` |
| 웹훅 상태 | `RECEIVED`, `PROCESSED`, `IGNORED`, `FAILED` |