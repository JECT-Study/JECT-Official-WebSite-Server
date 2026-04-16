# 📧 미래 요구사항: 비동기 대량 메일 발송 시스템 사양 (Archived)

본 문서는 `feat/bulk-mail-dispatch-future` 브랜치에서 구현되었으나, 현재 요구사항 미확정으로 인해 `feat/mail-preview-only` 브랜치에서 제외된 비동기 대량 메일 발송 시스템의 아키텍처와 설계를 기록합니다. 향후 필요 시 본 문서를 참고하여 기능을 복구할 수 있습니다.

## 1. 개요
대량 메일 발송 시 사용자 대기 시간을 최소화하고 시스템 부하를 관리하기 위해 Spring의 `@Async`와 전용 Executor를 사용하여 백그라운드에서 메일을 순차적으로 발송합니다.

## 2. 데이터베이스 설계
### 2.1 메일 발송 작업 (mail_dispatch_job)
- **ID**: 자동 생성 PK
- **시나리오_ID**: 참조된 `mail_scenario` ID
- **상태**: `REQUESTED` (요청됨), `PROCESSING` (처리 중), `COMPLETED` (완료됨), `FAILED` (실패함)
- **수신자_수**: 전체 발송 대상 인원 수
- **공통_변수_JSON**: 발송 시점의 공통 치환 변수 데이터 (JSON 형식)
- **생성/시작/종료 일시**: 작업 추적을 위한 타임스탬프

### 2.2 메일 발송 대상 (mail_dispatch_target)
- **ID**: 자동 생성 PK
- **작업_ID**: 소속된 `mail_dispatch_job` ID
- **수신자_ID**: `member` 테이블 ID (선택 사항)
- **이메일**: 실제 수신 이메일 주소
- **상태**: `PENDING`, `SENT`, `FAILED`
- **실패_사유**: 발송 실패 시 에러 메시지 보관

## 3. 핵심 아키텍처
### 3.1 MailDispatchAsyncExecutor (비동기 수행자)
- **책임**: `MailDispatchService`로부터 위임받아 실제 메일 루프를 실행합니다.
- **순환 참조 방지**: 서비스 계층과 분리된 별도 컴포넌트로 구성되어 `@Async` 프록시가 정상 동작하도록 설계되었습니다.
- **흐름**: 
  1. 작업 상태를 `PROCESSING`으로 변경
  2. 타겟 목록을 순회하며 `templateEngine`으로 렌더링 후 `emailSendService` 호출
  3. 각 타겟 상태 및 최종 작업 상태 기록

### 3.2 설정 레이어
- **AsyncConfig**: `@EnableAsync`를 활성화하고 발송용 스레드 풀을 설정할 수 있는 곳입니다.
- **JpaAuditConfig**: `SupportApplication`에서 JpaAuditing을 분리하여 슬라이스 테스트(`@WebMvcTest`) 시의 빈 생성 오류를 방지합니다.

## 4. API 엔드포인트 사양 (미구현)
- `POST /admin/mail-dispatches`: 발송 작업 및 타겟 생성 (응답: Job ID)
- `POST /admin/mail-dispatches/{id}/execute`: 비동기 발송 시작 트리거
- `GET /admin/mail-dispatches`: 발송 이력 목록 조회
- `GET /admin/mail-dispatches/{id}`: 작업 상세 및 진행률 조회
- `GET /admin/mail-dispatches/{id}/failed-targets`: 실패자 목록 및 사유 조회

## 5. 비고 (구현 시 주의사항)
- **트랜잭션 격리**: 비동기 작업 내의 `@Transactional` 범위를 주의해야 하며, 가급적 개별 발송 단위로 예외를 잡아 한 타겟의 실패가 전체 작업에 영향을 주지 않도록 구현되었습니다.
- **개인화 변수**: 현재는 공통 변수만 지원하나, `dispatchToTarget` 메서드 내에 개인화 변수(Personal Variables) 병합 로직을 추가할 수 있는 구조입니다.
