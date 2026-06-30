# The Tuna Is 4jo Team Project

단순 CRUD 구현을 넘어, 동시성 제어·조회 성능·실시간 메시지 복구·결제 검증처럼 서비스 운영 중 발생할 수 있는 문제를 직접 다뤄본 커머스 백엔드 프로젝트입니다.

## 기술 스택

| 구분 | 사용 기술 |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot 4.0.6 |
| Build | Gradle |
| Database | MySQL, H2 |
| ORM / Query | Spring Data JPA, Querydsl |
| Security | Spring Security, JWT |
| Realtime | WebSocket, STOMP, Redis Pub/Sub |
| Cache / Lock | Redis, Redisson |
| Payment | PortOne |
| Test | JUnit 5, Postman, k6 |
| Infra | Docker, AWS EC2, GitHub Actions |

## 기술 선택과 설계 고민

기술 스택을 단순히 나열하기보다, 각 기술을 어떤 문제를 해결하기 위해 선택했는지 중심으로 정리했습니다.

### JWT 인증

초기에는 프론트에서 `memberId`, `senderId` 같은 사용자 식별값을 직접 전달하는 구조였습니다.  
하지만 클라이언트가 보내는 식별값을 그대로 신뢰하면 다른 사용자의 데이터에 접근하거나 메시지 발신자를 조작할 수 있다고 판단했습니다.

그래서 사용자 식별은 요청 DTO가 아니라 JWT 인증 정보에서 가져오도록 변경했습니다.  
또한 응답 DTO에서도 불필요한 사용자 식별자 노출을 줄여 정보 노출을 최소화했습니다.

### STOMP 기반 채팅

초기에는 단순 WebSocket만으로 채팅을 구현할 수 있다고 생각했습니다.  
하지만 채팅방 생성, 관리자 참여, 채팅 종료처럼 상태 변경 이벤트를 상대방에게 전달해야 하면서 단순 연결만으로는 구조가 부족해졌습니다.

STOMP는 destination 기반 라우팅과 구독 구조를 제공하기 때문에 채팅방 단위 메시지 전달에 적합했습니다.  
또한 `ChannelInterceptor`에서 JWT를 검증하여 STOMP 연결 시점의 인증을 처리했습니다.

### 채팅 메시지 복구

STOMP는 현재 연결된 사용자에게 실시간 메시지를 전달하지만, 연결이 끊긴 동안의 메시지를 자동으로 복구하지는 않습니다.

이를 보완하기 위해 메시지는 DB에 저장하고, 클라이언트가 마지막으로 받은 메시지 ID 이후의 메시지만 다시 조회할 수 있도록 복구 API를 추가했습니다.

```http
GET /api/chats/{chatRoomId}/messages?afterMessageId={lastMessageId}
```

### Redis Pub/Sub 채팅 브로드캐스트

단일 서버에서는 STOMP 브로드캐스트만으로 메시지 전달이 가능하지만, 서버가 여러 대가 되면 서로 다른 서버에 연결된 사용자에게 메시지가 전달되지 않을 수 있습니다.

이를 해결하기 위해 Redis Pub/Sub을 적용했습니다.  
서버는 채팅 메시지를 Redis의 `chat-room:{roomId}` 채널로 발행하고, 각 서버는 해당 메시지를 받아 자기 서버의 STOMP 구독자에게 다시 전달합니다.

Redis Pub/Sub은 메시지를 영구 저장하지 않기 때문에, 메시지 신뢰성은 DB 저장과 복구 API로 보완했습니다.

### 채팅 상태 변경과 비관적 락

채팅방은 사용자 종료, 관리자 참여, 자동 종료처럼 같은 방의 상태가 동시에 변경될 수 있습니다.  
상태 전이가 꼬이는 문제를 막기 위해 상태 변경이 필요한 채팅방 조회에 `PESSIMISTIC_WRITE` 비관적 락을 적용했습니다.

비관적 락은 조회 시점에 DB row lock을 획득해 다른 트랜잭션의 수정을 대기시키므로, 상태 불일치를 사전에 막는 데 유리하다고 판단했습니다.  
다만 락 대기로 인한 성능 저하와 deadlock 가능성이 있어 단순 조회에는 적용하지 않고 상태 변경 로직에만 제한적으로 사용했습니다.

### 쿠폰 발급과 Redisson 분산 락

선착순 쿠폰 발급은 여러 사용자가 동시에 같은 쿠폰 수량을 차감하는 구조입니다.  
동시 요청이 들어오면 쿠폰 재고보다 많은 발급이 발생할 수 있어 Redisson 기반 분산 락을 적용했습니다.

쿠폰 ID를 기준으로 락 키를 생성하고, 락을 획득한 요청만 쿠폰 발급 로직에 진입하도록 구성했습니다.

```java
RLock lock = redissonClient.getLock("LOCK:COUPON:" + couponId);
boolean available = lock.tryLock(10, 1, TimeUnit.SECONDS);
```

Redisson을 사용해 락 획득 대기 시간과 락 점유 시간을 설정하고, 락 해제 흐름을 명확하게 관리했습니다.

### 상품 목록 조회와 No-Offset

상품 목록은 데이터가 많아질수록 Offset 기반 페이징에서 뒤 페이지 조회 비용이 커질 수 있습니다.  
이를 줄이기 위해 마지막으로 조회한 상품 ID를 기준으로 다음 데이터를 가져오는 No-Offset 커서 기반 조회를 적용했습니다.

이 방식은 페이지 번호 기반 이동보다는 무한 스크롤처럼 다음 목록을 이어서 조회하는 기능에 적합하다고 판단했습니다.

### 상품 상세 조회와 Redis Cache

상품 상세 조회는 같은 상품이 반복적으로 조회될 가능성이 높은 API입니다.  
반복 조회 시 DB 접근을 줄이기 위해 Redis Cache-Aside 전략을 적용했습니다.

처음 조회할 때는 DB에서 데이터를 가져와 캐시에 저장하고, 이후 같은 상품을 조회할 때는 Redis에서 먼저 반환하도록 구성했습니다.  
상품 정보가 수정되거나 삭제될 때는 캐시를 제거해 오래된 데이터가 남지 않도록 처리했습니다.

### PortOne 결제와 웹훅

결제는 프론트에서 결제창을 호출하는 것만으로 끝나지 않습니다.  
클라이언트의 결제 성공 응답만 신뢰하면 서버의 주문 상태와 실제 결제 상태가 어긋날 수 있습니다.

이를 보완하기 위해 PortOne 결제 연동과 웹훅 수신 구조를 구성했습니다.  
결제 완료 이후 서버는 웹훅을 통해 결제 결과를 확인하고, 주문 및 결제 상태를 서버 기준으로 변경할 수 있도록 처리했습니다.
## 프로젝트 파일 구조

> 현재 프로젝트 기준 패키지 구조입니다. 도메인별 책임을 분리하고, 공통 설정과 예외 처리는 `global`에서 관리합니다.

```text
src/
├── main
│   ├── java/com/example/thetunais4joteamproject/
│   │   ├── TheTunaIs4joTeamProjectApplication.java
│   │   │
│   │   ├── global                         # 전역 공통 계층
│   │   │   ├── aop                        # API 실행 시간 측정 등 공통 AOP
│   │   │   ├── common                     # 공통 API 응답
│   │   │   ├── config                     # Security, JWT, Redis, WebSocket, Querydsl 설정
│   │   │   ├── entity                     # BaseTimeEntity 등 공통 엔티티
│   │   │   ├── error                      # BusinessException, ErrorCode, 전역 예외 처리
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
│   │       ├── address                    # 회원 배송지
│   │       │   ├── controller
│   │       │   ├── dto
│   │       │   ├── entity
│   │       │   ├── repository
│   │       │   └── service
│   │       │
│   │       ├── product                    # 상품, 카테고리, 검색
│   │       │   ├── controller
│   │       │   ├── dto
│   │       │   ├── entity
│   │       │   ├── repository
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
│   │       ├── order                      # 주문, 주문 만료 스케줄러
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
│   │       │   ├── dto
│   │       │   ├── entity
│   │       │   ├── repository
│   │       │   └── service
│   │       │
│   │       ├── coupon                     # 쿠폰, 분산락, 쿠폰 만료 스케줄러
│   │       │   ├── controller
│   │       │   ├── dto
│   │       │   ├── entity
│   │       │   ├── facade
│   │       │   ├── repository
│   │       │   ├── scheduler
│   │       │   └── service
│   │       │
│   │       ├── chat                       # STOMP 채팅, 누락 메시지 복구, Redis Pub/Sub
│   │       │   ├── controller
│   │       │   ├── dto
│   │       │   ├── entity
│   │       │   ├── pubsub                 # Redis Publisher/Subscriber, 채널 유틸
│   │       │   ├── repository
│   │       │   └── service
│   │       │
│   │       └── infra                      # 외부 인프라 연동
│   │           ├── portone                # PortOne 결제 조회/취소, 설정 응답
│   │           │   ├── client
│   │           │   ├── config
│   │           │   ├── controller
│   │           │   └── dto
│   │           └── webhook                # PortOne 웹훅 검증/처리
│   │
│   └── resources
│       ├── application.yml                # 애플리케이션 설정
│       ├── data.sql                       # 로컬 테스트용 초기 데이터
│       └── static                         # 백엔드 API 검증/시연용 정적 화면
│
└── test
    ├── java/com/example/thetunais4joteamproject/
    │   ├── domain                         # 도메인별 단위/통합 테스트
    │   │   ├── chat                       # Pub/Sub, 상태 변화 테스트
    │   │   ├── coupon                     # 쿠폰 발급/동시성 테스트
    │   │   ├── infra                      # 웹훅 테스트
    │   │   ├── order                      # 주문 파사드 테스트
    │   │   ├── payment                    # 결제 승인 테스트
    │   │   ├── product                    # 상품/검색 테스트
    │   │   ├── refund                     # 환불 테스트
    │   │   └── user                       # 로그인/JWT 테스트
    │   └── global                         # 보안 설정/JWT 필터 테스트
    └── k6                                 # 채팅 메시지 복구 성능 테스트 스크립트
```

## API 요약

### 공통

- 인증이 필요한 API는 JWT Access Token을 사용한다.
- 관리자 API는 관리자 권한이 필요하다.
- 실패 응답은 공통 예외 응답 규칙을 따른다.
- README API 요약에는 상세 필드명과 데이터 타입을 작성하지 않는다.

| 예외 코드 | HTTP Status | 메시지 |
| --- | ---: | --- |
| BAD_REQUEST | 400 | 요청 내용을 확인해 주세요 |
| UNAUTHORIZED | 401 | 로그인이 필요합니다 |
| FORBIDDEN | 403 | 접근 권한이 없습니다 |
| NOT_FOUND | 404 | 찾을 수 없습니다 |
| CONFLICT | 409 | 요청을 처리할 수 없습니다 |
| SERVICE_UNAVAILABLE | 503 | 현재 서비스를 이용할 수 없습니다 |
| GATEWAY_TIMEOUT | 504 | 응답 시간이 초과되었습니다 |
| INTERNAL_SERVER_ERROR | 500 | 서버 장애가 발생했습니다 |
| CATEGORY_NOT_FOUND | 404 | 존재하지 않는 카테고리입니다 |
| PRODUCT_NOT_FOUND | 404 | 존재하지 않는 상품입니다 |
| OPTION_NOT_FOUND | 404 | 존재하지 않는 옵션입니다 |
| DEFAULT_OPTION_NOT_FOUND | 500 | 상품의 기본 옵션을 찾을 수 없습니다. 시스템 오류입니다 |
| OUT_OF_STOCK | 409 | 상품 재고가 부족합니다 |
| PRODUCT_OPTION_NOT_ON_SALE | 409 | 판매 중인 상품 옵션이 아닙니다 |
| PRODUCT_OUT_OF_STOCK | 400 | 선택한 상품 옵션의 재고가 부족하여 주문할 수 없습니다. |
| CART_NOT_FOUND | 404 | 존재하지 않는 장바구니입니다 |
| CART_ITEM_NOT_FOUND | 404 | 존재하지 않는 장바구니 상품입니다 |
| INVALID_CART_ITEM_QUANTITY | 400 | 장바구니 상품 수량은 1개 이상이어야 합니다 |
| CART_EMPTY | 400 | 장바구니가 비어 있습니다 |
| ORDER_NOT_FOUND | 404 | 존재하지 않는 주문입니다 |
| INVALID_ORDER_STATUS | 400 | 변경할 수 없는 주문 상태입니다 |
| INVALID_ORDER_QUANTITY | 400 | 주문 수량은 1개 이상이어야 합니다 |
| ORDER_ALREADY_PENDING | 409 | 이미 결제 대기 중인 주문이 있습니다 |
| ALREADY_PROCESSED_PAYMENT | 400 | 이미 결제를 완료하였습니다. |
| INVALID_PAYMENT_STATUS | 400 | 변경할 수 없는 결제 상태입니다 |
| PAYMENT_INVALID_STATUS | 400 | 변경할 수 없는 결제 상태입니다. |
| PAYMENT_NOT_FOUND | 404 | 주문을 찾을 수 없습니다. |
| PORTONE_PAYMENT_NOT_FOUND | 404 | 포트원 결제 아이디를 찾을 수 없습니다. |
| PAYMENT_ALREADY_FAILED | 400 | 이미 실패한 결제입니다. |
| PAYMENT_ALREADY_CANCELED | 400 | 이미 실패한 결제입니다. |
| PG_SERVER_ERROR | 502 | 결제사 서버와 통신 중 오류가 발생했습니다. |
| PAYMENT_AMOUNT_MISMATCH | 400 | 금액이 일치하지 않습니다. |
| PAYMENT_NOT_PAID | 400 | 결제가 완료되지 않았습니다. |
| PAYMENT_ORDER_MISMATCH | 400 | 결제와 주문이 일치하지 않습니다. |
| INVALID_REFUND_STATUS_TRANSITION | 400 | 변경할 수 없는 환불 상태입니다. |
| REFUND_NOT_FOUND | 404 | 해당 환불을 찾을 수 없습니다. |
| INVALID_REFUND_AMOUNT | 400 | 금액이 맞지 않습니다. |
| ALREADY_REQUESTED_REFUND | 400 | 이미 환불된 결제 건입니다. |
| INVALID_REFUND_STATUS | 400 | 환불 상태가 존재하지 않습니다. |
| WEBHOOK_EVENT_NOT_FOUND | 404 | 주문 금액보다 많이 사용할 수 없습니다. |
| WEBHOOK_VERIFICATION_FAILED | 401 | 웹훅 서명 인증에 실패하였습니다. |
| MEMBER_NOT_FOUND | 404 | 존재하지 않는 회원입니다 |
| ADDRESS_NOT_FOUND | 404 | 존재하지 않는 배송지입니다 |
| INVALID_COUPON_EXPIRATION | 400 | 쿠폰 만료 일시는 현재 시간보다 과거일 수 없습니다. |
| COUPON_NOT_FOUND | 404 | 존재하지 않는 쿠폰입니다. |
| COUPON_ALREADY_ISSUED | 400 | 이미 발급받은 쿠폰입니다. |
| COUPON_OUT_OF_STOCK | 400 | 쿠폰 수량이 모두 소진되었습니다. |
| COUPON_EXPIRED | 400 | 유효기간이 만료된 쿠폰입니다. |
| INVALID_COUPON_ORDER_PRICE | 400 | 주문 금액이 쿠폰의 최소 주문 금액 조건을 충족하지 못했습니다. |
| INVALID_COUPON_DISCOUNT_PRICE | 400 | 쿠폰 할인 금액은 주문 금액보다 클 수 없습니다. |
| COUPON_NOT_USED | 400 | 사용 완료 상태의 쿠폰만 복구할 수 있습니다. |
---

### 사용자

| 기능 | Method | API Path | 인증 | 요청 | 응답 |
| --- | --- | --- | --- | --- | --- |
| 이메일 중복 확인 | GET | `/api/members/email-check` | X | 이메일 전달 | 이메일 사용 가능 여부 반환 |
| 회원가입 | POST | `/api/members/signup` | X | 회원가입 정보 전달 | 회원 생성 결과 반환 |
| 로그인 | POST | `/api/members/login` | X | 로그인 정보 전달 | 인증 토큰 반환 |
| 로그아웃 | POST | `/api/members/logout` | O | Authorization 헤더 전달 | 로그아웃 결과 반환 |
| 내 정보 조회 | GET | `/api/members/info` | O | 인증 정보 기준 조회 | 회원 정보 반환 |
| 내 정보 수정 | PUT | `/api/members/info` | O | 수정할 회원 정보 전달 | 수정 결과 반환 |

### 배송지

| 기능 | Method | API Path | 인증 | 요청 | 응답 |
| --- | --- | --- | --- | --- | --- |
| 배송지 등록 | POST | `/api/addresses` | O | 배송지 정보 전달 | 배송지 정보 반환 |
| 배송지 목록 조회 | GET | `/api/addresses` | O | 인증 정보 기준 조회 | 배송지 목록 반환 |
| 배송지 수정 | PATCH | `/api/addresses/{addressId}` | O | 수정할 배송지 정보 전달 | 수정 결과 반환 |
| 기본 배송지 변경 | PATCH | `/api/addresses/{addressId}/default` | O | 배송지 ID 전달 | 변경 결과 반환 |
| 배송지 삭제 | DELETE | `/api/addresses/{addressId}` | O | 배송지 ID 전달 | 삭제 성공 반환 |

### 카테고리

| 기능 | Method | API Path | 인증 | 요청 | 응답 |
| --- | --- | --- | --- | --- | --- |
| 카테고리 목록 조회 | GET | `/api/categories` | X | 카테고리 목록 조회 요청 | 카테고리 목록 반환 |
| 카테고리 생성 | POST | `/api/categories` | 관리자 | 카테고리 정보 전달 | 카테고리 ID 반환 |
| 카테고리 삭제 | DELETE | `/api/categories/{categoryId}` | 관리자 | 카테고리 ID 전달 | 삭제 성공 반환 |

### 상품

| 기능 | Method | API Path | 인증 | 요청 | 응답 |
| --- | --- | --- | --- | --- | --- |
| 상품 생성 | POST | `/api/products` | O | 상품 생성 정보 전달 | 상품 ID 반환 |
| 상품 목록 조회 | GET | `/api/products` | X | `lastProductId`, `size` 전달 | 상품 목록 반환 |
| 상품 상세 조회 | GET | `/api/products/{productId}` | X | 상품 ID 전달 | 상품 상세 정보 반환 |
| 상품 카테고리 조회 | GET | `/api/products/categories/{categoryId}` | X | 카테고리 ID 전달 | 카테고리 상품 목록 반환 |
| 상품 수정 | PUT | `/api/products/{productId}` | 관리자 | 수정할 상품 정보 전달 | 수정 성공 반환 |
| 상품 삭제 | DELETE | `/api/products/{productId}` | 관리자 | 상품 ID 전달 | 삭제 성공 반환 |
| 상품 옵션 일괄 수정 | PUT | `/api/products/{productId}/options` | 관리자 | 옵션 수정 정보 전달 | 수정 성공 반환 |
| 상품 대표 재고 수정 | PUT | `/api/products/{productId}/stock` | 관리자 | 대표 재고 정보 전달 | 수정 성공 반환 |
| 상품 검색 | GET | `/api/products/search` | X | 검색어와 페이징 정보 전달 | 검색 결과 반환 |
| 인기 검색어 조회 | GET | `/api/products/popular-searches` | X | 인기 검색어 조회 요청 | 인기 검색어 목록 반환 |

### 장바구니

| 기능 | Method | API Path | 인증 | 요청 | 응답 |
| --- | --- | --- | --- | --- | --- |
| 상품 담기 | POST | `/api/carts/items` | O | 담을 상품과 수량 전달 | 장바구니 상품 ID 반환 |
| 장바구니 조회 | GET | `/api/carts` | O | 인증 정보 기준 조회 | 장바구니 상품 목록 반환 |
| 장바구니 상품 수량 변경 | PATCH | `/api/carts/{cartItemId}` | O | 변경할 수량 전달 | 변경 결과 반환 |
| 장바구니 전체 비우기 | DELETE | `/api/carts/items` | O | 전체 비우기 요청 | 삭제 성공 반환 |
| 장바구니 상품 개별 삭제 | DELETE | `/api/carts/{cartItemId}` | O | 삭제할 장바구니 상품 ID 전달 | 삭제 성공 반환 |

### 주문

| 기능 | Method | API Path | 인증 | 요청 | 응답 |
| --- | --- | --- | --- | --- | --- |
| 주문서 미리보기 | GET | `/api/orders/preview` | O | 장바구니 상품과 쿠폰 정보 전달 | 결제 예정 금액 반환 |
| 장바구니 주문 생성 | POST | `/api/orders/cart` | O | 장바구니 주문 정보 전달 | 주문 및 결제 정보 반환 |
| 바로 주문 생성 | POST | `/api/orders/direct` | O | 상품 옵션, 수량, 배송지 정보 전달 | 주문 및 결제 정보 반환 |
| 주문 목록 조회 | GET | `/api/orders` | O | 인증 정보 기준 조회 | 주문 목록 반환 |
| 주문 상세 조회 | GET | `/api/orders/{orderId}` | O | 주문 ID 전달 | 주문 상세 정보 반환 |
| 주문 취소 | PATCH | `/api/orders/{orderId}/cancel` | O | 주문 ID 전달 | 주문 취소 결과 반환 |

### 결제

| 기능 | Method | API Path | 인증 | 요청 | 응답 |
| --- | --- | --- | --- | --- | --- |
| PortOne 설정 조회 | GET | `/api/config/portone` | X | 설정 조회 요청 | Store ID, Channel Key 반환 |
| 결제 확정 | POST | `/api/payments/confirm` | O | 결제 결과 정보 전달 | 결제 확정 결과 반환 |
| 웹훅 수신 | POST | `/webhooks/webhook` | X | PortOne 웹훅 본문과 서명 헤더 전달 | 웹훅 처리 결과 반환 |

### 환불

| 기능 | Method | API Path | 인증 | 요청 | 응답 |
| --- | --- | --- | --- | --- | --- |
| 환불 요청 | POST | `/api/refunds` | O | 환불 요청 정보 전달 | 환불 요청 결과 반환 |
| 환불 승인 | POST | `/api/admin/refunds/{refundId}/approve` | 관리자 | 환불 ID 전달 | 환불 승인 결과 반환 |
| 환불 거절 | POST | `/api/admin/refunds/{refundId}/reject` | 관리자 | 환불 ID와 거절 사유 전달 | 환불 거절 결과 반환 |

### 쿠폰

| 기능 | Method | API Path | 인증 | 요청 | 응답 |
| --- | --- | --- | --- | --- | --- |
| 쿠폰 생성 | POST | `/api/admin/coupons` | 관리자 | 쿠폰 생성 정보 전달 | 쿠폰 ID 반환 |
| 쿠폰 관리 목록 조회 | GET | `/api/admin/coupons` | 관리자 | 쿠폰 관리 조회 요청 | 쿠폰 발급/사용 현황 반환 |
| 쿠폰 발급 | POST | `/api/coupons/issue` | O | 발급할 쿠폰 정보 전달 | 발급 결과 반환 |
| 보유 쿠폰 조회 | GET | `/api/coupons` | O | 인증 정보 기준 조회 | 보유 쿠폰 목록 반환 |
| 발급 가능 쿠폰 목록 조회 | GET | `/api/coupons/available` | O | 인증 정보 기준 조회 | 발급 가능 쿠폰 목록 반환 |
| 쿠폰 사용 처리 | POST | `/api/coupons/use` | O | 사용할 쿠폰 정보 전달 | 사용 처리 결과 반환 |
| 쿠폰 복구 처리 | POST | `/api/coupons/restore` | O | 복구할 쿠폰 정보 전달 | 복구 처리 결과 반환 |

### 채팅

| 기능 | Method | API Path | 인증 | 요청 | 응답 |
| --- | --- | --- | --- | --- | --- |
| 채팅방 생성 | POST | `/api/chats` | O | 문의 제목과 내용 전달 | 채팅방 생성 결과 반환 |
| 채팅방 참여 | POST | `/api/chats/{chatRoomId}/join` | O | 채팅방 ID 전달 | 참여 결과 반환 |
| 채팅방 종료 | PATCH | `/api/chats/{chatRoomId}/close` | O | 채팅방 ID 전달 | 종료 결과 반환 |
| 채팅방 목록 조회 | GET | `/api/chats` | O | 인증 정보 기준 조회 | 채팅방 목록 반환 |
| 채팅방 상세 조회 | GET | `/api/chats/{chatRoomId}` | O | 채팅방 ID 전달 | 채팅방 상세 정보 반환 |
| 누락 메시지 조회 | GET | `/api/chats/{chatRoomId}/messages` | O | `afterMessageId` 전달 | 이후 메시지 목록 반환 |

### STOMP

| 기능 | Command | Destination | 인증 | 요청 | 응답 |
| --- | --- | --- | --- | --- | --- |
| STOMP 연결 | CONNECT | `/ws/chat` | O | Authorization 헤더 전달 | WebSocket 세션 인증 |
| 메시지 전송 | SEND | `/app/chat/message` | O | 채팅방 ID와 메시지 내용 전달 | 메시지 저장 후 브로드캐스트 |
| 채팅방 구독 | SUBSCRIBE | `/topic/chat/rooms/{chatRoomId}` | O | 채팅방 destination 구독 | 실시간 채팅 메시지 수신 |
| 관리자 채팅방 이벤트 구독 | SUBSCRIBE | `/topic/admin/chat/rooms` | 관리자 | 관리자 destination 구독 | 채팅방 생성/상태 이벤트 수신 |
---

## ERD 요약

### 핵심 연관관계

| 관계 | 설명 |
| --- | --- |
| `MEMBER 1:1 CART` | 회원은 하나의 장바구니를 가집니다. |
| `MEMBER 1:N MEMBER_ADDRESS` | 회원은 여러 배송지를 등록할 수 있습니다. |
| `MEMBER 1:N PRODUCT` | 회원은 여러 상품을 등록할 수 있습니다. |
| `CATEGORY 1:N PRODUCT` | 카테고리는 여러 상품을 포함합니다. |
| `PRODUCT 1:N PRODUCT_OPTION` | 상품은 여러 옵션을 가질 수 있습니다. |
| `CART 1:N CART_ITEM` | 장바구니는 여러 장바구니 상품을 포함합니다. |
| `PRODUCT_OPTION 1:N CART_ITEM` | 장바구니 상품은 상품 옵션을 참조합니다. |
| `MEMBER 1:N ORDERS` | 회원은 여러 주문을 생성할 수 있습니다. |
| `ORDERS 1:N ORDER_ITEM` | 주문은 여러 주문 상품을 포함합니다. |
| `PRODUCT_OPTION 1:N ORDER_ITEM` | 주문 상품은 주문 당시 상품 옵션을 참조합니다. |
| `ORDERS 1:1 PAYMENT` | 주문은 하나의 결제 정보와 연결됩니다. |
| `PAYMENT 1:1 REFUND` | 결제 건은 하나의 환불 요청과 연결될 수 있습니다. |
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

#### MEMBER_ADDRESS

| 필드명 | 타입 | 키 | 설명 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 배송지 ID |
| member_id | BIGINT | FK | 회원 참조 |
| receiver_name | VARCHAR(50) |  | 수령인 이름 |
| receiver_phone | VARCHAR(20) |  | 수령인 연락처 |
| zipcode | VARCHAR(10) |  | 우편번호 |
| address | VARCHAR(255) |  | 기본 주소 |
| detail_address | VARCHAR(255) |  | 상세 주소 |
| default_address | BOOLEAN |  | 기본 배송지 여부 |
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
| member_coupon_id | BIGINT |  | 사용 쿠폰 ID, Nullable |
| order_price | INTEGER |  | 주문 상품 금액 |
| discount_price | INTEGER |  | 할인 금액 |
| delivery_price | INTEGER |  | 배송비 |
| total_amount | INTEGER |  | 최종 결제 금액 |
| receiver_name | VARCHAR(50) |  | 주문 당시 수령인 이름 스냅샷 |
| receiver_phone | VARCHAR(20) |  | 주문 당시 수령인 연락처 스냅샷 |
| zipcode | VARCHAR(10) |  | 주문 당시 우편번호 스냅샷 |
| address | VARCHAR(255) |  | 주문 당시 기본 주소 스냅샷 |
| detail_address | VARCHAR(255) |  | 주문 당시 상세 주소 스냅샷 |
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

#### REFUND

| 필드명 | 타입 | 키 | 설명 |
| --- | --- | --- | --- |
| id | BIGINT | PK | 환불 ID |
| payment_id | BIGINT | FK, UK | 결제 참조 |
| requester_id | BIGINT | FK | 환불 요청 회원 참조 |
| admin_id | BIGINT | FK | 처리 관리자, Nullable |
| reason | TEXT |  | 환불 요청 사유 |
| rejection_reason | TEXT |  | 환불 거절 사유, Nullable |
| failure_reason | TEXT |  | 환불 실패 사유, Nullable |
| coupon_restored | BOOLEAN |  | 쿠폰 복구 여부 |
| refund_amount | INTEGER |  | 환불 금액 |
| status | VARCHAR(20) |  | REQUESTED, REJECTED, COMPLETED, FAILED |
| requested_at | DATETIME |  | 환불 요청 일시 |
| processed_at | DATETIME |  | 환불 처리 일시, Nullable |
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
| chat_room_id | BIGINT | FK, IDX | 채팅방 참조 |
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

| Enum | 사용 위치 | 상태 값 |
| --- | --- | --- |
| `MemberRole` | 회원 권한 | `USER`, `ADMIN` |
| `ProductStatus` | 상품 상태 | `ON_SALE`, `DISCONTINUED`, `DELETED` |
| `OptionStatus` | 상품 옵션 상태 | `ON_SALE`, `SOLDOUT`, `DISCONTINUED` |
| `OrderStatus` | 주문 상태 | `PENDING_PAYMENT`, `CONFIRMED`, `CANCELED`, `EXPIRED` |
| `PaymentStatus` | 결제 상태 | `PENDING`, `PAID`, `FAILED`, `CANCELED`, `REFUNDED` |
| `RefundStatus` | 환불 상태 | `REQUESTED`, `REJECTED`, `COMPLETED`, `FAILED` |
| `CouponStatus` | 쿠폰 상태 | `ACTIVE`, `DISABLED` |
| `MemberCouponStatus` | 발급 쿠폰 상태 | `UNUSED`, `USED`, `EXPIRED` |
| `ChatRoomStatus` | 채팅방 상태 | `WAITING`, `IN_PROGRESS`, `CLOSED` |
| `WebhookStatus` | 웹훅 처리 상태 | `RECEIVED`, `PROCESSED`, `IGNORED`, `FAILED` |

> `ErrorCode` enum은 공통 예외 응답 표에서 관리합니다.






