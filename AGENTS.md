# Codex 작업 규칙

## 개발환경

- IDE: IntelliJ IDEA
- Language: Java 17
- Framework: Spring Boot 4.0.6
- Build Tool: Gradle
- Database: MySQL 8.4
- ORM: Spring Data JPA
- In-memory Data Store: Redis
- Local Cache: Caffeine
- Authentication: JWT 기반 인증
- RTC: Web Socket, STOMP
- Test: POSTMAN, JUnit 5
- Payment Gateway: PortOne
- Infra: Docker, AWS EC2, GitHub Actions

## 작업 시작 규칙

- 프로젝트 작업을 시작할 때 먼저 담당 도메인과 담당 메서드를 사용자에게 물어본다.
- 담당 도메인이 아닌 로직은 수정하거나 추가하지 않는다.
- 사용자가 명시적으로 허용한 경우에만 담당 도메인이 아닌 로직을 수정하거나 추가한다.
- 코드 작성 전 사용자 확인을 받는다.
- 파일 수정 전 사용자에게 변경 내용을 리뷰받고 수정한다.
- 코드 수정 또는 추가는 한 번에 하나의 기능만 진행한다.
- 사용자가 명시적으로 요구한 경우에만 여러 기능을 함께 작성할 수 있다.

## 의존성 규칙

- 의존성 추가는 제한한다.
- 의존성 추가가 필요하면 먼저 사용자에게 이유를 설명하고 확인받는다.
- 사용자 승인 없이 dependency를 추가하지 않는다.

## 설계 원칙

- 단일 책임 원칙을 지킨다.
- 하나의 클래스, 메서드, 기능은 하나의 책임만 갖도록 작성한다.
- 담당 범위 밖의 로직을 임의로 수정하지 않는다.
- 불필요한 리팩터링을 하지 않는다.

## 이슈 작성 규칙

이슈는 아래 템플릿을 사용한다.

```markdown
---
name: 기능 구현
about: PRD 기반 기능 구현 Issue
title: "기능 설명"
labels: ''
assignees: ''
---

## 설명
(이 기능이 왜 필요한지 1~2줄)

## 구현 범위
(해당 Issue 구현 범위)
- [ ] ...

## API 스펙
| Method | URL | 요청 | 응답 |
|--------|-----|------|------|
| | | | |

## 참고
(상태 머신 규칙, 스냅샷 규칙 등)
```

## 공통 Java 규칙

- 객체 생성 과정에서 정적 팩토리 메서드를 사용한다.
- 오버라이딩 시 `@Override` 어노테이션을 사용한다.
- 의존성 추가가 필요하면 먼저 말하고 추가한다.
- 매개변수 이름은 통일한다.
  - DTO 이름 + `request`
  - DTO 이름 + `response`
  - 저장된 사용자 객체는 `savedUser`

## API 메서드 이름 규칙

| 기능 | 메서드명 |
| --- | --- |
| 등록/생성 | `create` |
| 수정 | `update` |
| 전체 조회 | `getAll` |
| 단건 조회 | `getOne` |
| 삭제 | `delete` |

- CRUD 해야 하는 항목이 여러 개인 경우 `create/update/get/delete + 도메인명` 형식으로 작성한다.

## 포맷팅 규칙

### 파일 인코딩

- 파일 인코딩은 UTF-8을 사용한다.

### 중괄호

- `if`, `else`, `for`, `do`, `while` 등 중괄호가 필수가 아닌 경우에도 중괄호를 사용한다.
- 빈 블록이 아닌 경우 중괄호를 연 후 줄바꿈한다.
- 중괄호를 닫기 전에 줄바꿈한다.
- 중괄호를 닫고 줄바꿈한다.
- 단, `else` 또는 콤마가 오는 경우에는 줄바꿈하지 않는다.

### 들여쓰기 및 공백

- 연속된 멤버와 클래스 초기화 사이에는 줄바꿈으로 구분한다.
- 논리적 그룹핑이 필요할 때는 공백으로 구분한다.
- `if`, `for`, `catch` 등의 예약어는 괄호 `(`, 중괄호 `{`와 공백으로 구분한다.
- 닫는 괄호 `)`, 콤마 `,`, 콜론 `:`, 세미콜론 `;` 다음에 문자가 올 경우 공백으로 구분한다.
- 라인의 마지막에 주석 `//`를 작성할 경우 주석 문자와 공백으로 구분한다.
- 타입과 변수 선언 사이는 공백으로 구분한다.

```java
List<String> names;
```

## 변수 선언 규칙

- 변수 선언은 한 번에 하나씩 작성한다.

```java
int count;
int total;
```

- 아래 방식은 사용하지 않는다.

```java
int count, total;
```

## 어노테이션 규칙

- 한 줄에 어노테이션 하나만 작성한다.

## 주석 규칙

- 개발 초기에는 한 줄 주석 `//`으로 작성한다.

## 네이밍 규칙

### 패키지 이름

- 패키지 이름은 전부 소문자로 작성한다.
- 연속된 단어도 언더바를 사용하지 않는다.

```java
com.example.deepspace
```

- 아래 방식은 사용하지 않는다.

```java
com.example.deepSpace
com.example.deep_space
```

### 클래스 이름

- 클래스 이름은 UpperCamelCase를 사용한다.
- 첫 단어는 대문자로 시작한다.
- 주로 명사나 명사구를 사용한다.

### 메서드 이름

- 메서드 이름은 lowerCamelCase를 사용한다.
- 첫 단어는 소문자로 시작한다.
- 주로 동사나 동사구를 사용한다.

### 상수 이름

- 상수 이름은 전부 대문자를 사용한다.
- 각 단어는 언더바로 구분한다.

```java
DEFAULT_PAGE_SIZE
```

### 지역 변수와 파라미터 이름

- 지역 변수와 파라미터 이름은 lowerCamelCase를 사용한다.
- 상수가 아닌 필드도 lowerCamelCase를 사용한다.

### DTO 이름

- DTO 이름은 `[method][도메인][Request/Response]` 형식으로 작성한다.

```java
CreateUserRequest
CreateUserResponse
UpdateProductRequest
GetOrderResponse
```
