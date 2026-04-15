# Assignment Requirements Checklist

검증 시점: 2026-04-08

기준 문서:
- `/Users/023s/Downloads/backend_assignment.md`
- `/Users/023s/Downloads/requirements.docx`

상태 표기:
- `✅ 충족`
- `⚠️ 부분 충족`
- `❌ 미충족`

## 1) 과제 안내서(`backend_assignment.md`) 기준

| 항목 | 상태 | 근거 |
|---|---|---|
| 이미지 처리 요청 API 구현 | ✅ | `POST /api/v1/jobs` 구현: `JobV1ApiController#createJob` |
| 작업 식별/추적 가능성 제공 | ✅ | 응답에 `jobId` 포함, 단건/목록 조회 API 존재 |
| 진행 상태 확인 가능 | ✅ | `GET /api/v1/jobs/{jobId}`, `GET /api/v1/jobs`에서 상태 반환 |
| 작업 완료 결과 조회 가능 | ✅ | 단건 조회 응답에 `result/errorMessage/retryCount` 포함 |
| Mock Worker 연동 수행 | ✅ | `MockWorkerClient`에서 `/process`, `/process/{jobId}` 호출 |
| 중복 요청 처리 전략 | ✅ | `idempotency_key` UNIQUE + 충돌 복구 + same key/different payload `409` |
| 상태 전이 및 금지 전이 정의 | ✅ | `JobStatus#canTransitionTo`, `Job#validateTransition` |
| 처리 보장 모델 명시/설명 | ✅ | `README.md`에 at-least-once 근거 명시 |
| 재시작 시 동작 및 정합성 리스크 설명 | ✅ | `JobRecoveryRunner` + `README.md` 리스크 섹션 |
| 테스트 코드 포함 | ✅ | `src/test/java/...` 테스트 스위트 존재 및 실행 통과 |
| 컨테이너 실행 가능 | ✅ | `compose.yaml` (`app + mysql`) |
| README 실행 방법 + 포트 정보 명확성 | ⚠️ | 실행 방법은 명시, 포트는 `README`보다 `compose.yaml`에 명확 |

## 2) 상세 명세(`requirements.docx`) 기준

| 항목 | 상태 | 근거 |
|---|---|---|
| FR-01 POST /jobs + 202/200 멱등 | ✅ | `JobCommandService`, `JobV1ApiController` |
| FR-02 GET /jobs/{jobId} 상세 필드 반환 | ✅ | 단건 조회 응답에 상세 필드(`result/errorMessage/retryCount/updatedAt`) 반환 |
| FR-03 GET /jobs 목록(페이지+상태) | ✅ | `JobQueryService#getList`, `JobPageResponse` |
| FR-04 MQ 기반 비동기 파이프라인 | ❌ | RabbitMQ/consumer 경로 없음 (scheduler-first 구조) |
| FR-04 5초 poll | ✅ | `job.polling-interval-ms` 기본 5000 |
| FR-04 429/500 재시도(backoff 1/2/4s) | ⚠️ | 재시도는 있음. 기본 backoff는 5s 시작(설정값 기준) |
| FR-05 멱등키 UNIQUE + 충돌 시 기존 반환 | ✅ | `JobCommandService` + DB unique 컬럼 |
| FR-06 기동 시 자동 복구(ApplicationRunner) | ✅ | `JobRecoveryRunner` |
| FR-06 5분 고아 PENDING 재발행 + 10분 주기 복구 | ❌ | 해당 임계값/전용 주기 정책 미구현 |
| NFR-01 At-least-once + optimistic lock | ✅ | `README` 설명 + `@Version` 사용 |
| NFR-01 manual ack/DLQ | ❌ | MQ 미도입 |
| NFR-02 batch 100, 스케줄러 페이징 | ✅ | `batch-size` 설정 + `findEligibleJobs(..., limit)` |
| NFR-02 Redis 분산 락 | ❌ | Redis 락 미도입 |
| NFR-03 Compose(MySQL+Redis+Rabbit+App) | ❌ | 현재 `app + mysql`만 구성 |
| NFR-03 API Key 환경변수 주입 | ✅ | `MOCK_WORKER_API_KEY` 환경변수 사용 |

## 3) 결론

- `backend_assignment.md`의 핵심 평가 축(설계 설명, 상태 모델, 중복 처리, 복구, 외부 연동)은 충족 상태다.
- `requirements.docx` 기준에서도 FR-01~03, FR-05, 일부 NFR은 충족했다.
- MQ/Redis/고아 작업 임계 정책은 의도적으로 제외되어 관련 항목은 계속 미충족이다.

## 4) 제출 전 최소 보완 권고

1. `README.md`에 포트(`8080`)와 API 문서 경로를 명시한다.
2. `requirements.docx` 대비 의도적 제외 항목(MQ/Redis/FR-06 고아 기준)을 README에 명시해 평가자 혼동을 줄인다.
