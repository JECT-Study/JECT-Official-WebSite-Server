# Assignment Design

## Scope

이 문서는 현재 과제 제출용 단순화 구조의 설계 기준을 고정한다.

- Language: Java 21
- Framework: Spring Boot 3.x
- 핵심 목표:
  - `POST /jobs` 는 `PENDING` 저장만 수행
  - background scheduler 가 `PENDING submit + PROCESSING poll` 담당
  - 멱등성은 DB unique 중심으로 단순하게 설명 가능해야 함
  - 상태 전이, 재시작 복구, 처리 보장 모델을 README에 명확히 설명 가능해야 함
- 이번 구조에서 제외:
  - Redis cache / Redis 분산 락 / Redis 큐
  - RabbitMQ / Outbox
  - 멀티노드 완전 보장
  - 대규모 패키지 재배치

## API Contract

Base path: `/api/v1/jobs`

1. `POST /api/v1/jobs`
  - optional header: `X-Idempotency-Key`
  - body: `{ "imageUrl": "..." }`
  - new job: `202 Accepted`
  - 새 job 상태: `PENDING`
  - response fields: `jobId`, `status`, `imageUrl`, `createdAt`
  - idempotency hit: `200 OK`
  - same key + different payload: `409 Conflict`
2. `GET /api/v1/jobs/{jobId}`
  - response fields: `jobId`, `status`, `imageUrl`, `result`, `errorMessage`, `retryCount`, `createdAt`, `updatedAt`
3. `GET /api/v1/jobs?page=0&size=20&status=PROCESSING`
  - default sort: `createdAt DESC, id DESC`

## State Model

- allowed states: `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`
- allowed transitions:
  - `PENDING -> PROCESSING`
  - `PENDING -> FAILED`
  - `PROCESSING -> COMPLETED`
  - `PROCESSING -> FAILED`
- terminal states: `COMPLETED`, `FAILED`

## Processing Flow

### 1) Create Path

1. API 가 `imageUrl` 과 optional `X-Idempotency-Key` 를 받는다.
2. application command service 가 멱등키를 정규화한다.
3. 멱등키가 있으면 DB 의 `idempotency_key` unique row 를 확인한다.
4. 같은 key + 같은 정규화 `imageUrl` 이면 기존 job 을 반환한다.
5. 같은 key + 다른 정규화 `imageUrl` 이면 `409` 를 반환한다.
6. 새 작업이면 `PENDING` 으로 저장하고 `202` 를 반환한다.

### 2) Background Path

1. Pending scheduler 가 `PENDING` 작업을 submit 한다.
2. submit 응답은 `status=PROCESSING`만 정상으로 인정한다.
3. submit 성공 시 `PROCESSING` 으로 전이하고 worker job id 를 저장한다.
4. submit 실패 시:
  - 명확 실패 -> `FAILED`
  - 불확실 실패 -> `PENDING + nextAttemptAt`
5. Processing scheduler 가 `PROCESSING` 작업을 poll 한다.
6. poll 결과에 따라 `COMPLETED`, `FAILED`, `PROCESSING + defer` 로 전이한다.

## Idempotency Policy

- DB unique constraint: `idempotency_key`
- `X-Idempotency-Key` 는 optional
- image URL 비교 규칙:
  - `trim`
  - `URI.normalize().toString()`
- duplicate key 요청:
  - same key + same normalized imageUrl -> 기존 job 반환, HTTP `200 OK`
  - same key + different normalized imageUrl -> HTTP `409 Conflict`
- 신규 요청:
  - 새 job 생성
  - HTTP `202 Accepted`

## Failure Classification Policy

### 명확 실패 (즉시 `FAILED`)

- response invalid / empty
- poll 응답 `jobId` 무결성 위반(요청 `jobId`와 불일치)
- configuration error
- 비즈니스/계약 위반 계열 오류

### 불확실 실패 (`PENDING + backoff`)

- `CONNECT_FAILURE`
- `READ_TIMEOUT`
- `SERVER_ERROR`
- `RATE_LIMITED` (`429 Too Many Requests`)
- `CIRCUIT_OPEN`
- 예상 외 시스템 예외

## Recovery / Concurrency Policy

- execution lease, startup recovery runner, optimistic-lock 충돌 흡수 로직은 유지한다.
- optimistic lock 충돌은 비즈니스 실패가 아니라 동시성 이벤트로 처리한다.
- backoff 기반 `nextAttemptAt` 스케줄은 재사용한다.
- named lock / ShedLock 은 도입하지 않는다.

## Processing Guarantee Model

- 본 시스템은 `at-least-once` 처리 보장 모델로 설명한다.
- 이유:
  - 재시도와 recovery 로 작업 유실 가능성을 줄인다.
  - 그러나 외부 worker 호출 성공 후 DB 반영 전 장애가 나면 중복 submit 가능성이 남는다.

## Server Restart Policy

- 기동 직후 recovery runner 가 `PENDING`, `PROCESSING` 작업을 다시 스캔한다.
- `PENDING` 은 재전송 후보, `PROCESSING` 은 poll 재개 후보다.
- terminal 상태는 복구 대상이 아니다.
- 종료 시 in-flight 정리를 위해 `server.shutdown=graceful`을 적용한다.
- shutdown 대기 시간은 `spring.lifecycle.timeout-per-shutdown-phase`로 제어한다.

## Data Consistency Risks

- 외부 submit 성공 후 DB 저장 전 장애
- poll 완료 응답 수신 후 DB 저장 전 장애
- lease 만료 후 재선점에 따른 재처리 가능성

## DB Index Policy

스케줄러 스캔과 목록 조회 부하를 줄이기 위해 아래 인덱스를 유지한다.

- `(status, next_attempt_at, lease_expires_at, updated_at)`
- `(status, created_at)`

## Configuration Policy

### 유지되는 설정

- `job.scheduler.enabled`
- `spring.task.scheduling.pool.size`
- `server.shutdown`
- `spring.lifecycle.timeout-per-shutdown-phase`
- `job.polling-interval-ms`
- `job.execution-lease-ms`
- `job.initial-backoff-ms`
- `job.max-backoff-ms`
- `job.batch-size`
- `job.recovery-batch-size`
- `job.max-retry-count`

### Scheduler Pool 운영 기준

- 기본 권장값: `JOB_SCHEDULER_POOL_SIZE=2`
- 증설(`4`) 검토 조건:
  - 스케줄러 작업 종류가 3개 이상으로 늘어난 경우
  - 스케줄 시작 지연 또는 backlog 증가가 지속적으로 관측되는 경우
  - DB 및 외부 Mock Worker 처리 여유가 확인된 경우

### Scheduler Enabled 의미

- `job.scheduler.enabled=true`일 때만 아래 백그라운드 처리가 실행된다.
  - `Pending` submit scheduler
  - `Processing` poll scheduler
  - startup recovery runner
- `job.scheduler.enabled=false`면 위 3개가 모두 비활성화된다.

### 제거되는 설정

- `spring.data.redis.*`
- `job.redis.*`
- `job.immediate-submit-*`

과제 기준 로컬 환경은 `application.yml` 기준 MySQL 을 사용한다.

## Local Development

- 로컬 인프라는 `compose.yaml` 기준 `app + mysql` 을 함께 띄운다.
- `.env.example` 을 기반으로 `.env` 를 준비하고 `MOCK_WORKER_API_KEY` 를 채워 compose 가 읽게 한다.
- 기본 명령은 `./scripts/setup-env.sh "<candidateName>" "<email>"` 이다.
- 전체 기동 기본 명령은 `docker compose up --build` 이다.
- Mock Worker 는 로컬 compose 에 두지 않고, 기본 `mock-worker.base-url` 이 가리키는 실제 경로를 사용한다.

## Error / Exception Policy

- 공통 에러 응답 형식: `{ code, message, timestamp }`
- 커스텀 예외 계층(`JobException` 기반)을 사용한다.
- 상태 전이 오류는 `InvalidJobStatusTransitionException` 으로 관리한다.
- same key + different payload 는 `JobIdempotencyConflictException` 으로 관리한다.
- 요청 본문 파싱 실패(`HttpMessageNotReadableException`)는 `400 Bad Request` + `VALIDATION_FAILED`로 처리한다.
- 지원하지 않는 `Content-Type`(`HttpMediaTypeNotSupportedException`)도 `400 Bad Request` + `VALIDATION_FAILED`로 처리한다.
- 백그라운드 처리 경로의 예상 외 내부 예외는 `errorMessage`를 일반화된 문구로 저장하고, 상세 원인은 서버 로그에만 남긴다.

## Circuit Breaker Policy

- 외부 worker 호출의 circuit breaker 는 `CircuitBreaker.executeSupplier(...)` 수동 방식을 유지한다.
- 어노테이션 기반 retry/circuit 전환은 하지 않는다.

## Test Minimum Bar

1. create / idempotency
  - new `202`, duplicate `200`, conflict `409`
2. state transition
  - 허용/금지 전이 검증
3. scheduler / processing
  - Pending submit 성공/실패
  - Processing poll 성공/실패
  - backoff / recovery / lease reclaim
4. controller contract
  - POST / GET one / GET list
  - error response format

## Documentation Sync Rule

설계가 바뀌면 아래 문서를 같은 흐름에서 함께 갱신한다.

- `README.md`
- `AGENTS.md`
- `docs/assignment-design.md`
- `docs/progress-log.md`
