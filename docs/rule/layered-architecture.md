# Layered Architecture Rules

이 문서는 현재 저장소의 레이어 규칙을 짧고 강하게 고정한다.

## 1. 기본 방향

- `presentation -> application -> domain`
- `infrastructure` 는 `domain/application` 이 요구하는 계약을 구현한다.
- 패키지 루트는 `com.realteeh.api`

## 2. 레이어별 책임

### presentation

- Request/Response DTO
- Controller
- API 명세 인터페이스
- 예외를 HTTP 응답으로 매핑

하지 말 것:

- repository 직접 호출
- 비즈니스 상태 전이 구현
- 외부 worker 호출 구현

### application

- 유스케이스 orchestration
- 트랜잭션 경계
- 멱등성 처리 흐름
- 도메인 객체와 인프라 구현 연결
- 필요 최소 범위의 외부 연동 port 정의

하지 말 것:

- web request/response 타입 전파
- 영속성 세부사항을 public contract 로 노출

### domain

- 핵심 엔티티와 값
- 상태 전이 규칙
- repository port

하지 말 것:

- Spring MVC/JPA/WebClient 같은 기술 프레임워크 의존

### infrastructure

- JPA repository 구현
- 외부 API client
- scheduler
- 기타 기술 구현

하지 말 것:

- presentation 역할 대체
- domain 규칙 자체를 다시 정의

## 3. 상태 모델 규칙

- 허용 상태: `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`
- 허용 전이:
    - `PENDING -> PROCESSING`
    - `PENDING -> FAILED`
    - `PROCESSING -> COMPLETED`
    - `PROCESSING -> FAILED`
- terminal:
    - `COMPLETED`
    - `FAILED`

새 상태나 전이 규칙을 추가하면 다음 문서를 같이 수정한다.

- `docs/assignment-design.md`
- `docs/progress-log.md`

## 4. API 계약 규칙

- `POST /api/v1/jobs`
    - 새 작업: `202 Accepted`
    - 중복 멱등키: `200 OK`
- `GET /api/v1/jobs/{jobId}`
    - 단건 조회
- `GET /api/v1/jobs`
    - 페이지 조회
- 공통 에러 응답:
    - `{ code, message, timestamp }`

## 5. 금지되는 빠른 우회

- Controller 에서 repository 직접 접근
- application service 에서 DTO 응답 직접 조립
- infrastructure 구현에 맞춰 domain model 을 오염시키는 변경
- 현재 phase 범위를 넘는 추상화 선행 도입

## 6. 변경 기준

다음이 바뀌면 문서를 먼저 갱신한다.

- API request/response/error contract
- 상태 전이
- Mock Worker 연동 방식
- polling scheduler 흐름
- Phase 1 범위 해석
