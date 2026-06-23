# The Tuna Is 4jo Team Project

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