# 메일 발송 상태 관측과 알림 기준

발송 상태가 DB에 커밋된 뒤 로그와 상태별 counter를 남긴다. 발송 결과의 원본은 DB와 관리자 조회 API이며, counter는 현재 대기 인원이나 전체 발송 이력이 아니다. 운영 수집·알림 배포와 실제 메일 발송은 별도 승인 대상이다.

## 기록 경계

`MailDispatchClaimService`는 claim·결과·격리 상태 변경 시 주소·본문·token이 없는 `MailDispatchTransitionEvent`를 발행한다. `MailDispatchTransitionTelemetryService`는 `AFTER_COMMIT`에서 처리한다. 롤백된 변경은 집계하지 않으며, 이미 처리한 결과나 오래된 token으로 거부된 결과는 새 전이로 집계하지 않는다.

단, 만료된 claim에 늦게 도착한 결과를 거부하면서 `UNKNOWN`으로 격리한 경우에는 실제 격리 전이를 집계한다. 메서드의 성공 여부가 아니라 커밋된 상태를 기준으로 한다. 최초 작업 생성의 `PENDING`은 이 counter에 포함하지 않는다.

## 로그와 메트릭

상태 로그 형식:

```text
[MAIL_DISPATCH_STATE] jobId=123 targetId=456 status=PENDING attemptCount=1 nextAttemptAt=2026-10-04T12:00:31
```

`jobId`·`targetId`로 관리자 조회 결과와 연결한다. 주소·제목·본문·claim token·원본 예외 메시지는 포함하지 않는다. 메트릭 이름은 `mail.dispatch.transitions`, 애플리케이션에서 추가하는 태그는 `status` 하나다. ID·주소·시각을 태그로 사용하지 않는다. 환경 공통 태그는 기존 수집 설정에 따른다.

- `PROCESSING`: 발송 시도 획득이 커밋됨. 외부 수락이나 배달 완료가 아니다.
- `PENDING`: 안전한 요청 제한 거부 후 재시도 대기가 커밋됨. 모든 미처리 대상 수가 아니다.
- `SENT`: 외부 호출 성공과 DB 결과 기록이 완료됨. 수신함 도착이나 열람 증명이 아니다.
- `FAILED`: 확정 실패 또는 재시도 한도 초과가 커밋됨.
- `UNKNOWN`: 발송 여부를 확정할 수 없어 격리가 커밋됨. 자동 재발송하지 않는다.

counter는 프로세스의 상태 전이 관측 횟수다. 재기동하면 초기화되며 한 대상의 여러 시도를 포함할 수 있다. 커밋 직후 프로세스 중단이나 수집 장애에서는 관측이 유실될 수 있으므로 영속 감사 기록·정확히 한 번 이벤트 전달로 해석하지 않는다. 관리자 조회의 `attemptCount`, `nextAttemptAt`, `lastAttemptFailureReason`과 최종 결과를 함께 확인한다.

계측 중 오류가 발생하면 `[MAIL_DISPATCH_TELEMETRY_FAILURE]`에 작업·대상 ID, 상태, 예외 종류만 남긴다. 계측 오류 때문에 이미 커밋한 발송 결과를 실패로 바꾸거나 다시 보내지 않는다. `MeterRegistry`가 없는 실행 환경에서는 상태 로그만 남긴다.

## 알림 기준 제안

아래는 적용할 기준이며 배포된 알림 규칙이 아니다. 기존 Prometheus 수집이 연결된 환경에서 counter의 실제 노출 이름과 공통 태그를 확인한 뒤 적용한다. 별도 endpoint 공개나 보안 설정 변경은 하지 않는다.

- 최근 구간에 `UNKNOWN` 전이가 발생하면 [운영자 확인 절차](mail-dispatch-unknown-runbook.md)의 확인 대상으로 알린다. 재발송 명령을 자동 실행하지 않는다.
- `FAILED` 전이가 발생하면 관리자 조회에서 실패 사유와 시도 횟수를 확인한다. 대기 중인 `PENDING`을 최종 실패로 알리지 않는다.
- `[MAIL_DISPATCH_TELEMETRY_FAILURE]` 발생 시 수집 경로를 점검한다. 메트릭 부재를 발송 실패나 정상 처리의 증거로 보지 않는다.
- 기존 worker 배치 오류 로그가 반복되면 DB 연결·잠금·실행 오류를 점검한다. 횟수·시간 임계값은 운영 환경과 담당자가 별도로 확정한다.

Prometheus counter 이름이 `mail_dispatch_transitions_total`로 노출되는 환경의 확인 필요 알림 예시:

```promql
sum(increase(mail_dispatch_transitions_total{status="UNKNOWN"}[5m])) > 0
```

관측 구간·수집 간격·알림 채널은 운영 환경에서 검증한다. 이 알림만으로 오래된 미처리 대상의 부재를 증명할 수 없다. 현재 backlog·지연 측정과 운영 수집·알림 배포는 후속 검증 대상이다.

## 검증

`MailDispatchTransitionMetricsMysqlTest`는 실제 MySQL의 저장·claim·결과·조회 경계와 로그·MeterRegistry 출력을 검증한다. 운영 AWS·Prometheus 연결 또는 알림 도착 검증은 아니다.

## 참고

- [worker 실행과 관리자 조회 계약](mail-dispatch-worker.md)
- [불확실 결과 격리 결정](adr/0002-quarantine-uncertain-mail-delivery.md)
- [Spring 트랜잭션 이벤트](https://docs.spring.io/spring-framework/reference/6.2/data-access/transaction/event.html)
- [Micrometer counter와 시간 구간별 해석](https://docs.micrometer.io/micrometer/reference/concepts/counters.html)
- [Micrometer 이름과 태그 기준](https://docs.micrometer.io/micrometer/reference/concepts/naming.html)
