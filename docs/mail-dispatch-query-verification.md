# 메일 발송 결과 조회의 MySQL 검증

운영자는 본인이 요청한 발송 작업과 수신자별 결과만 조회한다. 이 문서는 기존 조회 계약을 실제 MySQL에서 확인하는 검증 범위다. 새 API나 발송 정책을 추가하지 않는다.

## 운영자가 확인할 수 있는 내용

- 본인 작업 목록은 요청 시각 내림차순이며, 같은 시각이면 작업 ID 내림차순이다.
- 모집 공고와 작업 상태 조건을 함께 적용하고 페이지의 전체 건수도 같은 조건을 따른다.
- 다른 관리자의 작업과 없는 작업은 상세·수신자 조회에서 모두 `MAIL-9`로 거부한다.
- 수신자 결과는 대상 ID 오름차순이다. 같은 지원 ID가 다른 작업에도 있어도 결과·시도 정보는 섞이지 않는다.
- `PENDING`, `SENT`, `FAILED`, `UNKNOWN` 필터와 페이지 전체 건수를 구분한다.
- 재시도 대기의 마지막 오류는 최종 실패 사유가 아니다. 대기 중에는 최종 실패 건수가 증가하지 않는다.
- Outbox 없는 과거 이력의 재시도 정보는 `null`이다. 새 작업의 미시도 `0회`와 의미가 다르다.

## 조회 예시

```http
GET /admin/mails/dispatches?recruitId=2&status=PROCESSING&page=0&size=10
GET /admin/mails/dispatches/123
GET /admin/mails/dispatches/123/targets?status=PENDING&page=0&size=10
```

재시도 대기 예시: 첫 시도에서 확실한 요청 제한 거부가 발생하면 대상은 `PENDING`, `attemptCount=1`, `lastAttemptFailureReason=TOO_MANY_EMAIL_REQUESTS`다. 이 단계의 `failureReason`과 `sentAt`은 null이며, 작업은 여전히 처리 중이다. 실제로 배달된 횟수로 해석하지 않는다.

조회 응답에는 권한 범위 안의 주소 snapshot을 제공한다. 저장된 제목·본문·claim token·lease는 추가 노출하지 않는다. 이 검증은 이메일 주소를 일반 로그에 기록할 권한을 부여하지 않는다.

## 자동 검증 경계

`MailDispatchQueryMysqlTest`는 MySQL 8.2 Testcontainers와 기존 QueryDSL·공개 조회 서비스를 사용한다. 결과 상태는 기존 작업 생성·claim·성공·실패·격리 경계로 준비한다. 운영 내부 서비스를 mock으로 바꿔 조회 결과를 미리 반환하지 않는다.

검증 사례는 관리자 격리, 과거 이력 호환, 동률 정렬·복합 필터·페이지, 네 가지 대상 상태, 재시도 정보, JSON 응답의 비공개 데이터 제외다. 별도 HTTP 인증 테스트는 기존 `AdminMailDispatchSecurityTest`가 담당한다. MySQL 테스트를 운영 인증 서버나 전체 HTTP 배포의 검증으로 표현하지 않는다.

이번 확인은 기존 동작의 회귀 검증이다. 처음부터 통과한 사례를 제품 결함의 RED/GREEN 수정으로 표현하지 않는다.

## 통합 의존성과 한계

기반 로컬 후보에는 #687·#688·#693·#696·#698·#700·#702와 dev가 포함된다. #688에 추가된 #691의 회귀 테스트 세 건은 #702 head에 없어 별도 로컬 통합 때 import 충돌을 해결해 보존했다. 이 변경에는 해당 테스트와 조회 검증이 함께 포함되며 운영 코드·migration은 추가로 변경하지 않는다.

MySQL 조회 테스트의 스키마는 테스트 프로필의 JPA 생성 방식이다. 실제 Flyway 업그레이드는 별도 `MailDispatchOutboxMigrationTest`가 담당한다. 집중 테스트 결과만으로 전체 migration·커버리지·배포를 통과했다고 판단하지 않는다.

현재 대기 건수·지연의 신규 gauge, Prometheus 수집·알림 도착, 스테이징과 실제 SES 수락·배달, UNKNOWN 해제·재발송은 검증 범위 밖이다.

## 실행 기록

이번 브랜치의 집중·전체 결과는 검증 종료 후 PR 본문에 기록한다. 이전 기반의 911개 통과·90.83%는 이 브랜치의 신규 실행 결과가 아니다.
