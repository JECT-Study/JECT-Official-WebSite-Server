# WebClient Transition Minimum Plan

이 문서는 현재 `RestClient` 기반 외부 연동을 유지하면서, **필요 시** `WebClient` 기반 비동기 polling으로 전환하기 위한 최소 변경안을 정리한다.

현재 상태:

- 외부 호출 클라이언트: `RestClient`
- scheduler 실행 모델: 동기 loop
- 처리 보장 모델: `at-least-once`

이 문서는 즉시 구현 지시가 아니라 **다음 phase용 설계 메모**다.

## 1. 전환 트리거

아래 조건이 반복 관측되면 WebClient 전환을 착수 후보로 본다.

1. `PROCESSING` 작업량 증가로 polling 1회 사이클이 `job.polling-interval-ms`를 자주 초과한다.
2. 외부 I/O 대기 시간이 길어 worker 조회 throughput이 스레드 점유에 막힌다.
3. batch size를 키워도 CPU보다 I/O wait 비율이 높아 처리량 개선이 제한된다.

## 2. 목표와 비목표

목표:

1. 외부 submit/poll I/O를 비동기로 처리해 polling throughput을 높인다.
2. 현재 도메인 규칙, 실패 분류, 상태 전이, API 계약은 유지한다.
3. 기존 scheduler/recovery 구조는 유지하고 변경 범위를 최소화한다.

비목표:

1. 처리 보장 모델을 `exactly-once`로 바꾸지 않는다.
2. Redis/MQ/분산락을 도입하지 않는다.
3. 패키지 대규모 재배치나 전면 reactive 아키텍처 전환을 하지 않는다.

## 3. 최소 변경안 (권장 순서)

### 3.1 WorkerGateway 시그니처

현재 포트는 동기 반환이다.

- `WorkerStartResult requestProcessing(String imageUrl)`
- `WorkerStatusResult fetchProcessingResult(String externalJobId)`

전환 시 최소 변경안:

1. 포트에 reactive 메서드를 추가한다.
  - `Mono<WorkerStartResult> requestProcessingAsync(String imageUrl)`
  - `Mono<WorkerStatusResult> fetchProcessingResultAsync(String externalJobId)`
2. 기존 동기 메서드는 유지한다.
3. 동기 경로는 기본 구현으로 `async + block(timeout)` 래핑 가능하게 둔다.

이 방식이면 application/service 진입 시그니처를 한 번에 깨지 않고 점진 전환이 가능하다.

### 3.2 Infrastructure Client

`MockWorkerClient`를 `WebClient` 기반으로 바꾼다.

유지할 것:

1. 실패 타입 매핑(`CONNECT_FAILURE`, `READ_TIMEOUT`, `SERVER_ERROR`, `RESPONSE_INVALID`, `CIRCUIT_OPEN`, `CONFIGURATION_ERROR`)
2. `CircuitBreaker.executeSupplier(...)` 수동 경계
3. 응답 DTO 검증 로직

추가할 것:

1. `WebClient` bean 및 timeout 설정
2. `onStatus`, `timeout`, `onErrorMap` 기반 예외 매핑

### 3.3 JobProcessingService

외부 호출 부분만 비동기화하고, 상태 업데이트 트랜잭션 경계는 기존 방식 유지한다.

1. `findEligibleJobs` + `tryClaimJob`는 현재와 동일하게 동기 처리
2. claim 완료된 job 목록에 대해 `Flux.flatMap(..., concurrency)`로 외부 호출 병렬화
3. 각 결과 반영은 기존 `updateClaimedJob(...)`로 개별 트랜잭션 처리
4. scheduler 진입은 기존 `void` 유지, 내부에서 `blockLast()`로 주기당 완료 보장

권장 기본 설정:

- `job.reactive.enabled` (default: false)
- `job.reactive.concurrency` (예: 32)
- `job.reactive.timeout-ms`

### 3.4 Scheduler

`PendingJobScheduler`, `ProcessingJobScheduler` 시그니처는 그대로 둔다.

1. feature flag가 꺼져 있으면 기존 동기 경로
2. 켜져 있으면 비동기 경로 실행 후 주기 내 완료까지 대기

이 방식은 운영 중 즉시 롤백이 쉽다.

## 4. 트랜잭션 전략

현재의 핵심 경계를 유지한다.

1. claim 트랜잭션: `tryClaimJob`
2. 외부 I/O: 트랜잭션 밖
3. 상태 반영 트랜잭션: `updateClaimedJob`

주의:

1. reactive 체인 안에서 JPA 엔티티를 길게 붙잡지 않는다.
2. 결과 반영은 `jobId` 기준 재조회 후 적용한다.
3. 낙관적 락 충돌은 기존처럼 skip 처리한다.

## 5. 테스트 전략

최소 테스트 세트:

1. Infrastructure 단위:
  - WebClient 응답 파싱/에러 매핑/timeout/circuit open
2. Application 통합:
  - Pending submit 성공/명확 실패/불확실 실패
  - Processing poll 성공/실패/defer
  - lease reclaim + optimistic lock 충돌 skip
3. 회귀:
  - API create/get/list + idempotency (`202/200/409`)

추가 권장:

1. concurrency 설정별 요청 수/상태 반영 일관성 검증
2. feature flag on/off 양쪽 경로 동일 결과 검증

## 6. 롤아웃/롤백

롤아웃:

1. 코드 배포 후 `job.reactive.enabled=false` 기본 유지
2. 스테이징에서 `true`로 부하/지표 확인
3. 운영에서 점진 활성화

롤백:

1. `job.reactive.enabled=false` 즉시 전환
2. 기존 동기 경로로 복귀

## 7. 수용 기준 (다음 phase)

1. 기존 테스트 바 깨지지 않을 것
2. `PROCESSING` backlog 구간에서 polling 처리량 지표 개선 확인
3. 장애 시 실패 분류/재시도 정책이 기존과 동일하게 동작할 것
