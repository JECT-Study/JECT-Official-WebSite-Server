# Progress Log

## 2026-04-08

### Submission Docs Package (Section 5/6/7)

- 변경:
  - `README.md`에 제출 방법 섹션을 추가하고, 요구사항 6의 "상용 계정 불필요" 조건을 명시했다.
  - `docs/submission/5-design-explanation.md`를 추가해 상태 모델/실패 전략/동시성/병목/외부 연동 근거를 별도 문서로 정리했다.
  - `docs/submission/6-run-conditions.md`를 추가해 컨테이너 실행 조건, 포트, 실행 절차, 로컬 인프라 구성을 정리했다.
  - `docs/submission/7-submission-method.md`를 추가해 GitHub 링크 제출 및 README 포함 항목 체크리스트를 정리했다.
- 이유:
  - 과제 안내서의 5/6/7 항목을 평가자가 빠르게 검토할 수 있도록 제출 문서 패키지를 명시적으로 분리하기 위해서다.
  - README 하나만 읽어도 필수 제출 정보가 누락되지 않도록 제출 방법 항목을 고정하기 위해서다.
- 다음 phase 영향:
  - 설계/실행 정책 자체는 변경하지 않았고, 제출 가독성과 검증 편의성만 향상됐다.
  - 실제 제출 시점에는 README의 GitHub 링크 placeholder를 실제 저장소 URL로 교체해야 한다.

### Submission Risk Cleanup (MockBean/Dead Code/Toggle Regression)

- 변경:
  - `JobProcessingServiceUnexpectedErrorTest`의 `@MockBean`을 `@MockitoBean`으로 교체해 deprecation/removal 경고를 제거했다.
  - 더 이상 사용되지 않던 `JobSimpleResult` DTO와 관련 매핑 경로(`JobResultMapper#toSimpleResult`, `JobResponseMapper#toSimpleResponse(JobSimpleResult)`)를 제거했다.
  - `SchedulerConditionalLoadingTest`를 추가해 `job.scheduler.enabled=false`일 때 `PendingJobScheduler`, `ProcessingJobScheduler`, `JobRecoveryRunner` 빈이 로딩되지 않음을 검증하도록 보강했다.
- 이유:
  - 제출/채점 환경에서 불필요한 테스트 경고를 줄이고, 사용되지 않는 코드 경로를 제거해 유지보수 혼선을 줄이기 위해서다.
  - 스케줄러 토글 의미가 코드 조건(`@ConditionalOnProperty`)과 실제 컨텍스트 로딩 결과에서 일치함을 회귀 테스트로 고정하기 위해서다.
- 다음 phase 영향:
  - `MockBean` 제거 흐름을 유지해 Spring Boot 4.x 전환 시 테스트 호환성 리스크를 줄인다.
  - 스케줄러 토글 관련 회귀가 발생하면 새 컨텍스트 테스트가 조기 탐지 지점으로 동작한다.

### List Ordering Fix + Error Message Sanitization + Flaky Test Hardening

- 변경:
  - `JobQueryService` 목록 조회 기본 정렬을 `createdAt DESC, id DESC`로 고정했다.
  - `JobProcessingService`의 `catch (Exception)` 경로에서 `e.getMessage()` 저장을 중단하고, submit/poll 각각 일반화된 고정 메시지를 저장하도록 변경했다.
  - `JobQueryServiceTest`에 기본 정렬 규칙 검증 케이스를 보강했다(필터 유무, 페이지 이동 시 동일 정렬 유지).
  - `JobProcessingServiceTest`, `JobProcessingCircuitBreakerTest`의 고정 `Thread.sleep` 대기를 조건 기반 bounded polling으로 교체했다.
  - 예상 외 예외 메시지 일반화 정책 검증을 위해 `JobProcessingServiceUnexpectedErrorTest`를 추가했다.
  - `README.md`, `docs/assignment-design.md`에 목록 기본 정렬 및 내부 예외 메시지 일반화 정책을 반영했다.
- 이유:
  - 목록 조회 기본 정렬이 고정되지 않으면 동시 생성/갱신 상황에서 페이지 경계의 일관성이 깨질 수 있기 때문이다.
  - 내부 예외 원문을 API `errorMessage`로 직접 노출하면 구현 세부 정보가 외부로 드러날 수 있기 때문이다.
  - 시간 기반 고정 sleep은 CI 환경에 따라 간헐 실패를 유발할 수 있어 조건 기반 대기가 더 안정적이기 때문이다.
- 다음 phase 영향:
  - 목록 API는 기본 정렬을 문서/코드/테스트에서 동일하게 `createdAt DESC, id DESC`로 유지한다.
  - 예상 외 내부 예외 상세는 API 응답이 아니라 서버 로그 중심으로 추적하는 운영 기준을 유지한다.
  - flaky 완화 패턴(bounded polling)은 향후 시간 의존 테스트에도 동일하게 재사용한다.

### Client Error Mapping Hardening (400 Normalization)

- 변경:
  - `ApiExceptionHandler`에 `HttpMessageNotReadableException` 전용 핸들러를 추가해 malformed JSON 요청을 `400 + VALIDATION_FAILED`로 처리하도록 변경했다.
  - `ApiExceptionHandler`에 `HttpMediaTypeNotSupportedException` 전용 핸들러를 추가해 지원하지 않는 `Content-Type` 요청을 `400 + VALIDATION_FAILED`로 처리하도록 변경했다.
  - `JobV1ApiControllerTest`에 malformed JSON 요청과 잘못된 `Content-Type` 요청의 회귀 테스트를 추가했다.
  - `README.md`, `docs/assignment-design.md`에 본문 파싱 실패/Content-Type 오류를 `400`으로 처리한다는 정책을 반영했다.
- 이유:
  - 클라이언트 요청 오류가 fallback 핸들러로 흘러 `500 INTERNAL_ERROR`로 응답되면 API 계약 해석이 불명확해지고, 평가 관점에서 서버 오류와 요청 오류를 구분하기 어렵기 때문이다.
  - 요청 오류를 일관된 `400 + VALIDATION_FAILED`로 고정하면 에러 정책과 테스트 기대값을 단순하게 유지할 수 있기 때문이다.
- 다음 phase 영향:
  - 신규 `ErrorCode`를 추가하지 않고 기존 `VALIDATION_FAILED`를 재사용한다.
  - 추후 에러 코드 세분화가 필요해지면 `400` 범주 내에서 세부 코드를 확장하는 방향으로 검토한다.

### 429 Retry Classification + Poll JobId Integrity

- 변경:
  - `WorkerFailureType`에 `RATE_LIMITED`를 추가했다.
  - `MockWorkerClient`에서 submit/poll 모두 `429 Too Many Requests`를 `RATE_LIMITED`로 분류하도록 추가했다.
  - poll 응답 검증에 `jobId` 무결성 체크를 추가해 요청한 `jobId`와 응답 `jobId`가 다르면 `RESPONSE_INVALID`로 fail-fast 처리하도록 변경했다.
  - `JobProcessingService` 실패 정책에서 `RATE_LIMITED`를 불확실 실패(`PENDING + backoff`) 그룹으로 포함했다.
  - 관련 테스트를 보강했다(`MockWorkerClientTest`, `JobProcessingServiceTest`, `WorkerFailureTypeTest`).
- 이유:
  - 트래픽 급증 상황의 `429`를 즉시 실패로 처리하면 재시도 기회를 잃어 처리 보장 모델 설명과 어긋나기 때문이다.
  - poll 응답 `jobId`가 요청값과 다를 때 상태를 반영하면 외부 이상 응답으로 내부 상태가 오염될 수 있기 때문이다.
- 다음 phase 영향:
  - 현재 정책은 4xx 중 `429`만 재시도 대상으로 유지한다.
  - `Retry-After` 헤더 기반 동적 backoff는 별도 phase에서 필요 시 검토한다.

### Background Toggle Hardening + Runbook Alignment

- 변경:
  - `JobRecoveryRunner`를 `job.scheduler.enabled=true` 조건에서만 로딩되도록 변경했다.
  - Mock Worker submit 응답 검증을 강화해 `status=PROCESSING`이 아닌 경우 `RESPONSE_INVALID`로 fail-fast 처리하도록 변경했다.
  - `compose.yaml`에 `JOB_SCHEDULER_POOL_SIZE`, `APP_SHUTDOWN_TIMEOUT` 전달을 추가했다.
  - `README.md`에 기본 포트(`8080`, `3306`)와 `job.scheduler.enabled`의 실제 동작 범위를 명시했다.
  - 관련 테스트를 보강했다(`JobProcessingServiceTest`, `MockWorkerClientTest`).
- 이유:
  - scheduler 비활성화 시 startup recovery가 별도로 동작하는 혼선을 제거하고, 운영자가 기대한 토글 의미를 코드와 일치시키기 위해서다.
  - 외부 submit 응답 상태를 검증하지 않으면 상태 불일치가 생길 수 있어 명시적 방어가 필요했다.
  - 실행 문서와 compose 환경 전달이 어긋나 있던 부분을 실제 동작 기준으로 정렬하기 위해서다.
- 다음 phase 영향:
  - `job.scheduler.enabled`는 백그라운드 처리 전체 토글로 해석된다.
  - submit 응답 계약이 확장되면(예: 즉시 완료 상태 허용) `start-status` 분기 정책을 별도 설계 항목으로 재정의해야 한다.

### Graceful Shutdown 도입

- 변경:
  - `application.yaml`에 `server.shutdown=graceful`을 추가했다.
  - `spring.lifecycle.timeout-per-shutdown-phase` 기본값을 `35s`로 추가하고 `APP_SHUTDOWN_TIMEOUT` 환경변수로 조정 가능하게 했다.
  - `.env.example`, `README.md`, `docs/assignment-design.md`에 동일 기준을 반영했다.
- 이유:
  - 종료 시점의 in-flight 요청/작업을 먼저 마무리해 `submit/poll 성공 후 DB 반영 전 종료` 정합성 리스크를 완화하기 위해서다.
- 다음 phase 영향:
  - 재시작/복구 정책은 동일하며, graceful shutdown은 리스크 완화 장치로만 동작한다.
  - 강제 종료(`SIGKILL`)까지는 방어하지 못하므로 at-least-once 모델 설명은 그대로 유지한다.

### GET One Detail Fields Restore

- 변경:
  - `GET /api/v1/jobs/{jobId}` 응답을 상세 필드(`result`, `errorMessage`, `retryCount`, `updatedAt`) 포함 형태로 복구했다.
  - application에 `JobDetailResult`를 재도입하고, presentation에 `JobDetailResponse`를 추가했다.
  - controller/spec/mapper를 단건 조회 전용 상세 응답 타입으로 다시 연결했다.
  - 관련 테스트(`JobQueryServiceTest`, `JobV1ApiControllerTest`)에 상세 필드 검증을 추가했다.
- 이유:
  - 과제 요구사항의 "작업 완료 결과 조회 가능" 조건과 FR-02 상세 필드 계약을 충족하기 위해서다.
- 다음 phase 영향:
  - create 응답은 계속 요약 필드 유지, get-one 응답만 상세 필드 유지 정책으로 분리된다.

### Scheduler Thread Pool Sizing

- 변경:
  - `application.yaml`에 `spring.task.scheduling.pool.size` 설정을 추가해 스케줄러 기본 단일 스레드 실행을 멀티 스레드 풀로 확장 가능하게 했다.
  - `.env.example`에 `JOB_SCHEDULER_POOL_SIZE` 환경 변수를 추가했다.
- 이유:
  - `Pending` submit과 `Processing` poll 스케줄이 같은 시각에 겹칠 때 단일 스레드 직렬 실행으로 인해 지연이 누적되는 구간을 줄이기 위해서다.
- 다음 phase 영향:
  - 멀티노드 락 정책은 여전히 도입하지 않으며, 현재 lease + optimistic lock 기반 동시성 제어 모델을 유지한다.
  - 운영 부하에 맞춰 풀 크기만 조정하는 방식으로 스케줄 처리량을 튜닝할 수 있다.

### Scheduler Pool Baseline Guideline

- 변경:
  - 운영 기본값 가이드를 `JOB_SCHEDULER_POOL_SIZE=2`로 명시했다.
  - `JOB_SCHEDULER_POOL_SIZE=4` 증설 검토 조건(스케줄 종류 증가, 지연/backlog 지속, DB/Worker 여유 확인)을 문서에 추가했다.
- 이유:
  - 현재 스케줄러 작업은 `Pending submit`, `Processing poll` 두 종류이며, 기본 운영에서는 과도한 병렬 스레드보다 안정적인 최소 풀 구성이 적합하기 때문이다.
- 다음 phase 영향:
  - 스케줄 종류나 부하 특성이 바뀌면 같은 기준으로 풀 크기를 단계적으로 조정한다.
  - 멀티노드 락 도입 없이도 단일 인스턴스 운영 튜닝 기준을 명확히 유지할 수 있다.

### Naming And DTO Mapping Cleanup

- 변경:
  - `GET /api/v1/jobs/{jobId}` application 반환 DTO를 `JobDetailResult`에서 `JobSimpleResult`로 단순화했다.
  - presentation 응답 DTO 이름을 `JobCreateResponse`에서 `JobSimpleResponse`로 변경해 create/get 공용 응답 의도를 명확히 했다.
  - application DTO 생성 로직(`JobCreateResult`, `JobSummaryResult`)의 `of(Job)` 정적 팩토리를 제거하고 `JobResultMapper`로 매핑 책임을 일원화했다.
  - 외부 클라이언트 설정 클래스명을 `WebClientConfig`에서 `MockWorkerClientConfig`로, 빈 이름을 `mockWorkerWebClient`에서 `mockWorkerRestClient`로 정리했다.
  - `MockWorkerClient.ProcessStartRequest`를 `private record`로 축소해 내부 구현 노출을 줄였다.
- 이유:
  - 메서드/클래스 네이밍이 실제 역할을 더 직접적으로 설명하도록 맞추고, DTO 생성 책임 분산을 줄여 유지보수성을 높이기 위해서다.
- 다음 phase 영향:
  - API 응답 JSON 스키마는 유지된다.
  - 상세 조회 필드(`result`, `errorMessage`, `retryCount`)가 다시 필요해지면 별도 상세 DTO/엔드포인트로 재도입한다.

### Deferred Design Note: WebClient Transition

- 변경:
  - `docs/webclient-transition-minimum-plan.md` 문서를 추가해 WebClient 전환 최소 변경안(서비스 시그니처, 트랜잭션 경계, 테스트 전략)을 정리했다.
- 이유:
  - 현재는 `RestClient`를 유지하되, polling 부하 증가 시 즉시 실행 가능한 설계 초안을 남겨두기 위해서다.
- 다음 phase 영향:
  - WebClient 전환이 필요해지면 해당 문서를 기준으로 feature flag 기반 점진 전환을 진행한다.

### Layer Boundary Hardening (Architecture Review Follow-up)

- 변경:
  - `JobProcessingService`가 `infrastructure` 예외(`MockWorkerClientException`)를 직접 잡지 않고 `application.port.WorkerGatewayException`만 의존하도록 정리했다.
  - `WorkerGateway` 포트에서 Jackson 어노테이션을 제거하고, JSON 필드(`jobId`) 매핑은 `MockWorkerClient` 내부 전송 DTO(`ProcessStartResponse`, `ProcessStatusResponse`)로 이동했다.
  - 실패 유형 enum을 `application.port.WorkerFailureType`으로 승격하고 infra 예외 계층은 이를 사용하도록 정리했다.
  - `domain.repository.JobRepository`에서 `Page/Pageable` 의존을 제거하고, 페이지 조회는 `application.port.JobReadRepository`로 분리했다.
  - 목록 조회 경로를 `Page<JobSummaryResult>`로 바꿔 presentation이 `domain.Job`를 직접 다루지 않도록 정리했다.
  - `JobException`에서 `HttpStatus`를 제거하고, HTTP status 결정은 `ApiExceptionHandler`에서 `ErrorCode` 기준으로 일괄 매핑하도록 변경했다.
- 이유:
  - `presentation -> application -> domain` 방향성과 `infrastructure -> port 구현` 규칙을 코드 레벨에서 더 명확히 맞추기 위해서다.
  - application/domain 경계에 web/infra 세부 타입이 섞이면 이후 어댑터 교체와 테스트 격리가 어려워지기 때문이다.
- 다음 phase 영향:
  - 현재 phase에서는 `Job` 엔티티(JPA annotation 포함)를 domain에 유지한다.
  - domain 순수성(엔티티/영속 모델 분리)을 더 강화하려면 phase 단위로 entity 분리 전략을 별도로 다뤄야 한다.

### API Response Simplification (GET One = Create Shape)

- 변경:
  - `GET /api/v1/jobs/{jobId}` 응답 DTO를 생성 응답과 같은 형태(`jobId`, `status`, `imageUrl`, `createdAt`)로 통일했다.
  - `JobResponse` DTO를 제거하고 controller/spec/mapper의 단건 조회 반환 타입을 `JobCreateResponse`로 맞췄다.
- 이유:
  - 외부 API 응답 형태를 단순화해 필드 해석 비용을 줄이고, 생성/단건 조회 응답 계약을 일관되게 유지하기 위함이다.
- 다음 phase 영향:
  - 결과/실패 상세를 조회 API에서 다시 노출해야 하면 별도 상세 DTO를 재도입하거나 조회 endpoint 분리를 검토한다.

### Assignment Simplification For Submission

- 변경:
  - Redis cache, Redis 설정, Redis 테스트, compose 의 redis 서비스를 제거했다.
  - `POST /jobs` 즉시 submit 경로를 제거하고 `PENDING` 저장 후 반환하는 scheduler-first 구조로 단순화했다.
  - `JobService` 를 `JobCommandService` / `JobQueryService` 로 분리했다.
  - scheduler bean 을 `PendingJobScheduler`, `ProcessingJobScheduler` 로 분리했다.
  - `Job` 엔티티에서 `maxRetryCount` 컬럼을 제거하고, 재시도 한도는 설정값 인자로 받도록 바꿨다.
  - 루트 `README.md` 를 제출용 설계 설명 문서로 추가했다.
  - `HELP.md` 는 간단한 실행 가이드로 축소했다.
- 이유:
  - 과제 요구사항은 중복 요청 처리, 상태 전이, 처리 보장 모델, 재시작 복구, 정합성 설명이 핵심이었다.
  - Redis, 조회 캐시, immediate submit 은 구현 복잡도 대비 제출 설득력에 꼭 필요하지 않았다.
  - scheduler-first 흐름이 `at-least-once`, recovery, 정합성 리스크를 더 직접적으로 설명할 수 있었다.
  - `maxRetryCount` 는 row 데이터보다 설정값 성격이 강해 엔티티 컬럼으로 둘 이유가 약했다.
- 다음 phase 영향:
  - 성능 최적화가 필요해지면 Redis read cache 를 별도 phase 로 다시 도입할 수 있다.
  - 멀티노드 중복 스케줄링 방지가 필요해지면 ShedLock 같은 스케줄러 락을 재검토할 수 있다.
  - 외부 submit 성공 후 DB 반영 전 장애를 더 줄이려면 outbox/MQ 구조가 후속 후보가 된다.

## 2026-04-07

### Phase 3-4 Lightweight Transition (Historical)

- 변경:
  - MQ/outbox submit 경로를 제거하고 `POST /jobs` 즉시 submit 경로로 전환했다.
  - 즉시 submit 경로에 Spring Retry(기본: 최대 3회 시도)를 적용했다.
  - 실패 분류를 `명확 실패=FAILED`, `불확실 실패=PENDING+nextAttemptAt`로 단순화했다.
  - 스케줄러 책임을 `PENDING 재시도 + PROCESSING poll`로 축소했다.
- 이유:
  - 당시 목표는 복잡한 메시징 보장보다 기본 요구사항 안정 동작과 유지보수 단순화에 있었다.
- 다음 phase 영향:
  - 이번 2026-04-08 단순화 작업으로 immediate-submit/Redis 기준은 더 이상 현재 기준이 아니다.
