# 예약 발송 생성·조회·취소

예약 생성·내용 저장·조회·처리 시작 전 취소와 기존 worker의 예약 자동 실행을 연결한다. worker를 명시적으로 활성화한 환경에서만 자동 실행하며, 운영 제공 전 별도 배포·화면·승인된 수신자 검증이 필요하다. ADR 0003의 구현 상태 설명은 정책 기록 당시의 상태이며, 최신 실행 흐름은 [예약 자동 실행](mail-scheduled-execution.md)을 따른다.

## 요청 예시

```http
POST /admin/mails/dispatches/scheduled
Idempotency-Key: scheduled-mail-001
Content-Type: application/json

{
  "recruitId": 2,
  "scenarioId": 1,
  "applyIds": [10],
  "subjectOverride": null,
  "inputVariables": {},
  "scheduledAt": "2099-10-07T10:00:00+09:00"
}
```

관리자만 생성할 수 있다. 신규 예약 시각은 미래여야 하며, 없는 시각은 HTTP 400, 지난 시각은 `400 / MAIL-22`로 거부한다. 한국 시간 `10:00+09:00`은 UTC `01:00Z`로 저장한다. 시간대 없는 입력은 받지 않는다.

예약 시점의 대상·주소·렌더링된 제목·본문과 메일 유형·선정 결과를 같은 트랜잭션에 저장한다. 템플릿을 수정·삭제해도 저장한 내용은 바뀌지 않는다. 예약 작업 상태는 `SCHEDULED`이며 시각 도래 전에는 발송하지 않는다. 시각 도래 후 worker가 작업 잠금 안에서 `PROCESSING`으로 활성화한 뒤 기존 claim·발송 경로로 처리한다.

동일 관리자·키·내용·예약 시각의 재요청은 기존 결과를 반환한다. 같은 순간을 표현한 다른 시간대는 같은 요청이며, 예약 시각 변경 또는 즉시 발송 요청으로 키를 재사용하면 `409 / MAIL-20`이다. 기존 예약 재조회는 예약 시각이 지난 뒤에도 허용한다.

생성 응답은 기존 `MailDispatchResponse` 필드를 유지하고 `excludedCount`를 추가한다. 예약 시각은 본인 작업 목록·상세의 `scheduledAt`에서 UTC 형식으로 확인한다. 즉시 발송·과거 이력의 `scheduledAt`은 null이다.

```http
GET /admin/mails/dispatches?status=SCHEDULED&page=0&size=10
GET /admin/mails/dispatches/123
```

## 예약 취소

```http
POST /admin/mails/dispatches/123/cancel
```

요청 본문과 `Idempotency-Key`는 필요하지 않다. 관리자 본인의 `SCHEDULED` 작업만 취소하며, 작업·수신자·Outbox를 같은 트랜잭션에서 `CANCELLED`로 변경한다. 기존 주소·제목·본문·예약 시각은 보존하며 발송 실패로 집계하지 않는다. 목록·상세와 수신자 `status=CANCELLED` 필터에서 취소를 확인한다.

동일 예약을 다시 취소하면 `200`과 기존 취소 결과를 반환하고 종료 시각을 바꾸지 않는다. 다른 관리자의 예약과 없는 작업은 동일하게 `404 / MAIL-9`, 즉시 발송 작업 또는 이미 처리 시작·종료된 작업은 `409 / MAIL-23`으로 거부한다. 기존 처리·발송 결과를 취소로 덮어쓰지 않는다.

취소한 작업도 요청 키는 보존한다. 같은 생성 요청 키를 다시 보내면 기존 `CANCELLED` 결과를 반환하므로, 새 예약은 새 요청 키로 생성한다. 취소된 Outbox는 기존 worker와 claim에서 제외한다. 실제 SES에 전달된 메일을 회수하는 기능은 아니다.

## 발송 직전 확인과 출시 경계

서버 복구 후 시각이 지난 예약은 지연 실행한다. claim 직전 삭제·반려 등 자격을 확인하며, 선정 결과 변경은 결과 안내에만 적용하고 일반 안내는 유지한다. 제외된 대상은 `EXCLUDED`와 `exclusionReason`으로 조회한다. 전원 제외는 발송·실패 0건으로 종료한다. 취소와 활성화는 같은 작업 잠금으로 직렬화하며 처리 시작 후 취소를 거부한다. 실제 SES 발송·배포·화면 연결 검증은 별도 출시 단계다.
