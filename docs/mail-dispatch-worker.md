# 메일 발송 worker 설정과 운영 경계

worker는 DB에 보존된 미처리 메일을 실행하고 중단된 실행을 격리한다. 기본 상태는 비활성이다. 이 문서는 설정 계약이며 운영 환경 활성화·배포·실제 메일 발송 승인을 대신하지 않는다.

## 설정

- `mail.dispatch.worker.enabled`: 기본 `false`. 명시적으로 `true`일 때만 worker와 전용 scheduler를 생성한다.
- `mail.dispatch.worker.batch-size`: 기본 `10`, 허용 범위 `1`부터 `100`. 범위를 벗어나면 애플리케이션 시작을 거부한다. 조회 한도는 격리와 발송 배치에 각각 적용한다.
- `mail.dispatch.worker.poll-delay`: 기본 `1000`밀리초. 이전 배치가 끝난 뒤 다음 배치까지 기다리는 간격이다. Spring이 지원하는 양의 fixed-delay 값만 사용한다.

환경별 프로필 파일은 커밋하지 않는다. 운영 활성화는 별도 승인 후 해당 환경 설정에서 수행한다.

worker 활성화 시 발송 API는 발송 의도를 저장한 뒤 현재 상태를 반환하며 SES 호출 완료를 기다리지 않는다. 비활성 시 기존 즉시 발송을 유지한다. HTTP `201`과 응답 필드는 유지하지만 활성화 응답만으로 발송 완료를 판단하면 안 된다. 같은 관리자·key의 동일 요청은 기존 결과를 재사용한다.

`POST /admin/mails/dispatches` 응답 데이터 예시다. 공통 응답 wrapper는 생략했다. 실행 속도에 따라 `PROCESSING` 또는 이미 완료된 상태가 반환될 수도 있다.

```json
{
  "dispatchJobId": 123,
  "status": "REQUESTED",
  "targetCount": 1,
  "processingCount": 0,
  "successCount": 0,
  "failedCount": 0,
  "unknownCount": 0
}
```

최종 상태는 `GET /admin/mails/dispatches/{dispatchJobId}`와 수신자 결과 조회로 확인한다. 관리자 본인 작업만 조회할 수 있는 기존 권한 경계를 유지한다.

## 실행 흐름

`mailDispatchScheduler`의 단일 스레드가 격리 배치 후 안전한 미처리 발송 배치를 호출한다. 새 실행은 현재 상태를 잠금 안에서 재검증하고 claim을 커밋한 뒤 저장된 수신자별 snapshot으로 SES를 호출한다. 외부 호출 중 DB 트랜잭션을 유지하지 않는다. 모집 scheduler와 실행 스레드를 공유하지 않으며 별도 비동기 발송 큐를 만들지 않는다.

발송 후보는 `REQUESTED` 작업 또는 claim 표식이 있는 `PROCESSING` 작업의 `PENDING` 대상 중 재시도 시각이 도래한 대상이다. 만료 claim과 기존 동기 실행의 미확정 대상은 `UNKNOWN / MAIL-21`로 격리한다. `SENT`, `FAILED`, `UNKNOWN`을 자동 재발송하지 않는다. 종료 신호를 받은 실행자는 새 claim을 만들지 않는다.

SES가 명시적인 429 요청 제한으로 거부한 경우만 재시도 대기로 되돌린다. 총 시도는 최초 포함 3회이며 첫 거부 후 30초, 두 번째 거부 후 60초를 기다린다. `attempt_count`는 claim 획득 시 증가하고 `next_attempt_at`은 거부 결과 기록 시 저장한다. 대기 중 대상은 `PENDING`, 작업은 `PROCESSING`으로 남으며 최종 실패 집계는 세 번째 거부에서 한 번만 수행한다. 이전 token으로 늦게 도착한 결과는 거부한다.

네트워크·5xx·응답 유실은 재시도 후보가 아니다. 확정된 영구 거부는 즉시 실패로 종료한다. 호출 전 허가 대기 실패는 현재 확정 실패로 종료하며 별도의 일시 오류 코드가 없으므로 요청 제한으로 추정하지 않는다. worker 비활성 환경의 즉시 경로는 요청 제한도 기존처럼 최종 실패로 종료하며 새 재시도를 예약하지 않는다. 이미 예약한 재시도 대기가 있는 환경에서 worker를 끄면 해당 작업은 다시 활성화하기 전까지 진행되지 않으므로 운영 전 설정 변경 계획을 확인해야 한다.

worker의 배치 처리 오류는 예외 종류만 기록하고 다음 주기에 다시 후보를 조회한다. SES 수락 후 DB 결과 저장에 실패한 대상은 기존 claim을 유지하므로 다음 주기에 다시 발송하지 않는다. lease가 만료되면 격리한다. 이메일 주소·제목·본문·외부 응답 본문·원본 예외 메시지는 worker 로그에 남기지 않는다.

## 호출 한도와 의미

- 단건 본문 발송의 SES 허가 대기: 최대 10초. 허가를 얻지 못하면 SES 호출 전 확정 실패다. 기존 인증·템플릿 경로의 `consume(int)` 계약은 변경하지 않는다.
- 공유 SES client의 전체 API 호출 한도: 30초. 단일 시도 한도: 20초. 인증·템플릿·단체 발송의 SDK 호출에도 적용된다.
- SDK 자동 재시도: 비활성 유지. 시간 초과·응답 유실 등 불확실 결과는 격리하고 자동 재발송하지 않는다.
- claim lease: 대상별 2분. lease 만료는 SES 미호출 증명이 아니며 재발송 권한을 부여하지 않는다.

이 한도는 DB 연결 획득·잠금·쿼리 시간을 포함한 배치 전체의 실행 시간 보장이 아니다. 단일 worker는 처리량 상한도 갖는다. 운영 활성화 전 DB timeout·SES 한도·처리 지연·shutdown을 해당 환경에서 확인해야 한다. 실제 배달의 정확히 한 번 처리를 보장하지 않는다.

## 검증과 남은 작업

자동 실행·비활성·worker 컨텍스트 재생성·만료 격리·모집 scheduler 격리·DB 저장 장애의 후속 주기 처리는 실제 MySQL과 대체 SES로 검증한다. 실제 서버 프로세스 재기동·운영 배포·AWS 발송 검증과 구분한다. SDK 시간 초과는 로컬 HTTP 서버에서 짧은 한도로 검증하고 30초·20초 기본값은 별도로 확인한다.

재시도 횟수·다음 시각은 Outbox에 저장되며 기존 관리자 응답에는 아직 추가하지 않았다. 재시도 정보 조회, 호출 전 일시 실패의 명시적 분류, 운영자 확인·해제, 운영 메트릭·알림은 후속 작업이다. `UNKNOWN` 해제나 재발송은 이 worker의 권한이 아니다.

## 참고

- [발송 불확실 결과 격리 결정](adr/0002-quarantine-uncertain-mail-delivery.md)
- [AWS SDK timeout 계약](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/timeouts.html)
- [Spring fixed-delay와 scheduler 지정 계약](https://docs.spring.io/spring-framework/docs/6.2.x/javadoc-api/org/springframework/scheduling/annotation/Scheduled.html)
