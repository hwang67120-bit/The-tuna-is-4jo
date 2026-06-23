# The Tuna Is 4jo Team Project

## 프로젝트 파일 구조

> 목표 패키지 구조입니다. 도메인별 책임을 분리하고, 담당 도메인 외 로직 수정을 제한합니다.

```text
src/main/java/com/example/thetunais4joteamproject/
│
├── global                         # 공통 보안, 에러 핸들링, 유틸 인프라
│   ├── config                     # Security, Redis, Redisson, Querydsl 설정
│   ├── error                      # GlobalExceptionHandler, 에러코드 관리
│   ├── common                     # BaseEntity, 공통 Standard Response 포맷
│   └── util                       # JwtProvider, Encryptor 등
│
└── domain                         # 도메인 비즈니스 로직 계층 (상호 격리 구조)
    ├── user                       # 1. 회원 및 인증 도메인
    │   ├── controller             # UsersApiController
    │   ├── service                # UserService, AuthService
    │   ├── repository             # UserRepository
    │   └── dto                    # UserRequestDto, UserResponseDto
    │
    ├── product                    # 2. 상품 및 검색 도메인
    │   ├── controller             # ProductController
    │   ├── service                # ProductService, SearchService
    │   ├── repository             # ProductRepository, CategoryRepository
    │   │   ├── custom             # QueryDSL 성능 최적화용 명세 인터페이스
    │   │   └── impl               # QueryDSL 최적화 동적 쿼리 구현체
    │   ├── cache                  # ProductCacheRepository
    │   └── dto                    # ProductRequest, ProductResponse, SearchCondition
    │
    ├── cart                       # 3. 장바구니 도메인
    │   ├── controller
    │   ├── service
    │   ├── repository
    │   └── dto
    │
    ├── order                      # 4. 주문 및 결제 도메인
    │   ├── controller
    │   ├── service                # OrderService, PaymentService
    │   ├── repository             # OrderRepository, PaymentRepository
    │   └── dto
    │
    ├── coupon                     # 5. 프로모션 및 쿠폰 도메인
    │   ├── controller             # CouponController, AdminCouponController
    │   ├── service                # CouponService, CouponIssueService
    │   ├── repository             # CouponRepository, MemberCouponRepository
    │   ├── aop                    # @DistributedLock, DistributedLockAspect
    │   └── dto
    │
    ├── refund                     # 6. 환불 도메인
    │   ├── controller
    │   ├── service                # RefundService
    │   └── repository
    │
    ├── webhook                    # 7. PortOne 외부 연동 웹훅 도메인
    │   ├── controller             # WebhookController
    │   └── service                # WebhookIdempotencyService
    │
    └── chat                       # 8. 실시간 채팅 도메인
        ├── controller             # ChatRoomController, ChatStompController
        ├── service                # ChatService
        ├── repository             # ChatRoomRepository, ChatMessageRepository
        └── dto
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
| `MEMBER 1:N ORDERS` | 회원은 여러 주문을 생성할 수 있습니다. |
| `CART 1:N CART_ITEM` | 장바구니는 여러 장바구니 상품을 포함합니다. |
| `PRODUCT 1:N CART_ITEM` | 상품은 여러 장바구니 상품에서 참조됩니다. |
| `ORDERS 1:N ORDER_ITEM` | 주문은 여러 주문 상품을 포함합니다. |
| `PRODUCT 1:N ORDER_ITEM` | 주문 상품은 주문 당시 상품 정보를 참조합니다. |
| `COUPON 1:N MEMBER_COUPON` | 쿠폰은 회원에게 발급됩니다. |
| `MEMBER_COUPON 0..1:0..1 ORDERS` | 발급 쿠폰은 주문에 선택적으로 적용됩니다. |
| `ORDERS 1:0..1 PAYMENT` | 주문은 하나의 결제 정보와 연결됩니다. |
| `PAYMENT 1:N REFUND` | 결제는 여러 환불 요청과 연결될 수 있습니다. |
| `PAYMENT 0..1:N WEBHOOK_EVENT` | 결제는 PortOne 웹훅 이벤트를 발생시킬 수 있습니다. |
| `MEMBER 1:N CHAT_ROOM` | 회원은 문의 채팅방을 개설할 수 있습니다. |
| `CHAT_ROOM 1:N CHAT_MESSAGE` | 채팅방은 여러 메시지를 기록합니다. |

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

#### PRODUCT

| 필드명 | 타입 | 키 | 설명 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 상품 ID |
| category_id | BIGINT | FK | 카테고리 참조 |
| name | VARCHAR(255) |  | 상품명 |
| price | INTEGER |  | 기본 판매가 |
| description | TEXT |  | 상품 설명 |
| stock | INTEGER |  | 상품 재고 |
| status | VARCHAR(20) |  | ON_SALE, DISCONTINUED |
| created_at | DATETIME |  | 생성일시 |
| updated_at | DATETIME |  | 수정일시 |

#### CART

| 필드명 | 타입 | 키 | 설명 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 장바구니 ID |
| member_id | BIGINT | FK | 회원 참조 |
| created_at | DATETIME |  | 생성일시 |
| updated_at | DATETIME |  | 수정일시 |

#### CART_ITEM

| 필드명 | 타입 | 키 | 설명 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 장바구니 상품 ID |
| cart_id | BIGINT | FK | 장바구니 참조 |
| product_id | BIGINT | FK | 상품 참조 |
| quantity | INTEGER |  | 수량 |
| created_at | DATETIME |  | 생성일시 |
| updated_at | DATETIME |  | 수정일시 |

#### ORDERS

| 필드명 | 타입 | 키 | 설명 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 주문 ID |
| member_id | BIGINT | FK | 회원 참조 |
| member_coupon_id | BIGINT | FK | 사용 발급쿠폰 참조, Nullable |
| order_number | VARCHAR(50) | UK | 노출용 고유 주문번호 |
| original_amount | INTEGER |  | 상품 총액 |
| discount_amount | INTEGER |  | 쿠폰 할인 금액 |
| payment_amount | INTEGER |  | 최종 결제 금액 |
| status | VARCHAR(30) |  | PENDING, COMPLETED, CANCELED, REFUNDED |
| canceled_at | DATETIME |  | 주문취소일시 |
| created_at | DATETIME |  | 생성일시 |
| updated_at | DATETIME |  | 수정일시 |

#### ORDER_ITEM

| 필드명 | 타입 | 키 | 설명 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 주문 상품 ID |
| order_id | BIGINT | FK | 주문 참조 |
| product_id | BIGINT | FK | 상품 참조 |
| product_name | VARCHAR(255) |  | 주문 당시 상품명 스냅샷 |
| original_price | INTEGER |  | 주문 당시 정상 가격 |
| sale_price | INTEGER |  | 주문 당시 실제 판매 가격 |
| quantity | INTEGER |  | 수량 |
| total_amount | INTEGER |  | 주문 상품 총액 |
| created_at | DATETIME |  | 생성일시 |
| updated_at | DATETIME |  | 수정일시 |

#### PAYMENT

| 필드명 | 타입 | 키 | 설명 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 결제 ID |
| order_id | BIGINT | FK, UK | 주문 참조 |
| portone_payment_id | VARCHAR(50) | UK | PortOne 결제 식별자 |
| requested_amount | INTEGER |  | 결제 요청 금액 |
| paid_amount | INTEGER |  | PG 실제 결제 금액 |
| payment_method | VARCHAR(20) |  | 결제 수단 |
| status | VARCHAR(20) |  | PENDING, PAID, FAILED, REFUNDED |
| paid_at | DATETIME |  | 결제 완료 일시, Nullable |
| created_at | DATETIME |  | 생성일시 |
| updated_at | DATETIME |  | 수정일시 |

#### REFUND

| 필드명 | 타입 | 키 | 설명 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 환불 ID |
| payment_id | BIGINT | FK | 결제 참조 |
| requester_id | BIGINT | FK | 요청 회원 참조 |
| admin_id | BIGINT | FK | 처리 관리자 |
| reason | TEXT |  | 환불 사유 |
| rejection_reason | TEXT |  | 관리자 거절 사유, Nullable |
| failure_reason | TEXT |  | 실패 사유, Nullable |
| coupon_restored | BOOLEAN |  | 쿠폰 복구 여부 |
| refund_amount | INTEGER |  | PG 환불 금액 |
| portone_cancellation_id | VARCHAR(50) | UK | PortOne 취소 식별자 |
| status | VARCHAR(20) |  | REQUESTED, REJECTED, COMPLETED, FAILED |
| requested_at | DATETIME |  | 요청 일시 |
| processed_at | DATETIME |  | 처리 일시 |
| created_at | DATETIME |  | 생성일시 |
| updated_at | DATETIME |  | 수정일시 |

#### COUPON

| 필드명 | 타입 | 키 | 설명 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 쿠폰 ID |
| name | VARCHAR(30) |  | 쿠폰 이름 |
| discount_type | VARCHAR(20) |  | FIXED_AMOUNT, PERCENTAGE |
| discount_value | INTEGER |  | 할인 값 |
| minimum_order_amount | INTEGER |  | 최소 주문 금액 |
| maximum_discount_amount | INTEGER |  | 최대 할인 금액 |
| total_quantity | INTEGER |  | 총 발급 가능 수량 |
| issued_quantity | INTEGER |  | 현재 발급 수량 |
| max_per_member | INTEGER |  | 인당 최대 발급 가능 수량 |
| issue_start_at | DATETIME |  | 발급 시작 시간 |
| issue_end_at | DATETIME |  | 발급 종료 시간 |
| expires_at | DATETIME |  | 사용 만료 시간 |
| status | VARCHAR(20) |  | READY, ACTIVE, ENDED, STOPPED |
| created_at | DATETIME |  | 생성일시 |
| updated_at | DATETIME |  | 수정일시 |

#### MEMBER_COUPON

| 필드명 | 타입 | 키 | 설명 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 발급 쿠폰 ID |
| coupon_id | BIGINT | FK | 쿠폰 참조 |
| member_id | BIGINT | FK | 회원 참조 |
| status | VARCHAR(20) |  | AVAILABLE, USED, EXPIRED |
| issued_at | DATETIME |  | 발급 일시 |
| used_at | DATETIME |  | 사용 일시 |
| expires_at | DATETIME |  | 만료 일시 |
| created_at | DATETIME |  | 생성일시 |
| updated_at | DATETIME |  | 수정일시 |

#### CHAT_ROOM

| 필드명 | 타입 | 키 | 설명 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 채팅방 ID |
| member_id | BIGINT | FK | 문의 회원 참조 |
| title | VARCHAR(255) |  | 문의 제목 |
| status | VARCHAR(20) |  | WAITING, IN_PROGRESS, COMPLETED |
| completed_at | DATETIME |  | 완료 일시 |
| created_at | DATETIME |  | 생성일시 |
| updated_at | DATETIME |  | 수정일시 |

#### CHAT_MESSAGE

| 필드명 | 타입 | 키 | 설명 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 메시지 ID |
| chat_room_id | BIGINT | FK | 채팅방 참조 |
| sender_id | BIGINT | FK | 발신 회원 참조 |
| content | TEXT |  | 채팅 내용 |
| message_type | VARCHAR(20) |  | TEXT, SYSTEM |
| created_at | DATETIME |  | 생성일시 |

#### WEBHOOK_EVENT

| 필드명 | 타입 | 키 | 설명 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 웹훅 이벤트 ID |
| payment_id | BIGINT | FK | 결제 참조 |
| portone_event_id | VARCHAR(50) | UK | PortOne 이벤트 ID |
| portone_payment_id | VARCHAR(50) |  | PortOne 결제 식별자 |
| event_type | VARCHAR(30) |  | 이벤트 유형 |
| status | VARCHAR(20) |  | RECEIVED, COMPLETED, FAILED |
| payload | TEXT |  | 페이로드 |
| failure_reason | TEXT |  | 실패 사유 |
| received_at | DATETIME |  | 수신 일시 |
| processed_at | DATETIME |  | 처리 일시 |
| created_at | DATETIME |  | 생성일시 |
### 주요 상태 값

| 구분 | 상태 값 |
| --- | --- |
| 상품 상태 | `ON_SALE`, `DISCONTINUED` |
| 주문 상태 | `PENDING`, `COMPLETED`, `CANCELED`, `REFUNDED` |
| 결제 상태 | `PENDING`, `PAID`, `FAILED`, `REFUNDED` |
| 환불 상태 | `REQUESTED`, `REJECTED`, `COMPLETED`, `FAILED` |
| 쿠폰 상태 | `READY`, `ACTIVE`, `ENDED`, `STOPPED` |
| 발급 쿠폰 상태 | `AVAILABLE`, `USED`, `EXPIRED` |
| 채팅방 상태 | `WAITING`, `IN_PROGRESS`, `COMPLETED` |
| 메시지 타입 | `TEXT`, `SYSTEM` |
| 웹훅 상태 | `RECEIVED`, `COMPLETED`, `FAILED` |
