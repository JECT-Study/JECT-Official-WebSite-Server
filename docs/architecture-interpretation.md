# Architecture Interpretation

이 문서는 현재 `realteeh/api` 저장소의 구조를 Codex 관점에서 어떻게 해석해야 하는지 정리한다.
이 저장소는 단일 애플리케이션이므로, 복잡한 멀티모듈 규칙보다 **레이어 책임 분리**가 핵심이다.

## 1. 구조 요약

```text
HTTP Request
    ->
presentation
    ->
application
    ->
domain
    ->
infrastructure adapter / persistence / external worker
```

핵심 원칙:

- `presentation` 은 HTTP 입출력과 검증, 응답 변환만 담당한다.
- `application` 은 유스케이스 조합, 트랜잭션 경계, 외부 연동 시점 결정을 담당한다.
- `domain` 은 상태와 전이 규칙을 보존한다.
- `infrastructure` 는 DB, 스케줄러, Mock Worker 같은 기술 세부사항을 담당한다.

## 2. 패키지 해석

### `job.presentation`

- Controller, API spec, request/response DTO, 예외 응답 매핑
- 외부 계약의 진입점

### `job.application`

- `service`, `config`, `dto`, `exception`, `port`
- command service 와 query service 가 분리될 수 있지만, 복잡한 CQRS 패턴으로 일반화하지 않는다.
- processing service 는 background flow 전담이다.

### `job.domain`

- `Job`, `JobStatus`, repository port
- 상태 전이와 도메인 불변식의 중심

### `job.infrastructure`

- persistence 구현
- Mock Worker client
- pending / processing scheduler
- startup recovery runner

## 3. 현재 처리 흐름

1. API 가 작업 생성 요청을 받는다.
2. command service 가 멱등성 키를 정규화하고 DB `idempotency_key` row 를 확인한다.
3. 새 작업이면 `PENDING` 으로 저장하고 `202` 를 반환한다.
4. pending scheduler 가 lease 가능한 `PENDING` 작업을 선점해 submit 한다.
5. submit 성공 시 `PROCESSING`, 실패 시 `FAILED` 또는 `PENDING + backoff` 로 남는다.
6. processing scheduler 가 lease 가능한 `PROCESSING` 작업을 선점해 Worker 상태를 polling 한다.
7. 결과에 따라 `COMPLETED`, `FAILED`, `PROCESSING + defer` 로 전이된다.
8. startup recovery runner 는 기동 직후 `PENDING`, `PROCESSING` 작업을 다시 당겨온다.

## 4. 현재 구조에서 하지 않을 해석

- Redis cache
- Redis 분산 락 / Redis 큐 / Redis source of truth 전환
- MQ 중심 비동기 파이프라인
- DB lease 를 넘는 다중 워커 분산 제어

## 5. Codex 판단 기준

1. 이 로직이 HTTP 계층이 아니라 application/domain 에 있어야 하는가
2. 상태 전이 규칙이 domain 에 남아 있는가
3. 외부 연동 세부사항이 infrastructure 로 밀려나 있는가
4. 동시성 제어가 lease + optimistic lock 경계를 지키는가
5. 설계나 계약이 바뀌었다면 `README`, `assignment-design`, `progress-log` 가 같이 수정되었는가
