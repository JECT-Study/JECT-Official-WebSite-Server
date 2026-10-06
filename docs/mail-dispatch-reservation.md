# 메일 예약 생성·조회

현재 구현은 예약 생성·내용 저장·조회까지다. **예약 시각에 자동으로 보내거나 취소하는 기능은 아직 연결되지 않았다.** 자동 실행과 대상 재검증을 연결하기 전에는 운영 예약 발송 기능으로 제공하지 않는다.

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

예약 시점의 대상·주소·렌더링된 제목·본문과 선정 결과를 같은 트랜잭션에 저장한다. 템플릿을 수정해도 저장한 내용은 바뀌지 않는다. 예약 작업 상태는 `SCHEDULED`이며 기존 즉시 발송·worker·claim으로 실행하지 않는다.

동일 관리자·키·내용·예약 시각의 재요청은 기존 결과를 반환한다. 같은 순간을 표현한 다른 시간대는 같은 요청이며, 예약 시각 변경 또는 즉시 발송 요청으로 키를 재사용하면 `409 / MAIL-20`이다. 기존 예약 재조회는 예약 시각이 지난 뒤에도 허용한다.

생성 응답은 기존 `MailDispatchResponse` 구조를 유지한다. 예약 시각은 본인 작업 목록·상세의 `scheduledAt`에서 UTC 형식으로 확인한다. 즉시 발송·과거 이력의 `scheduledAt`은 null이다.

```http
GET /admin/mails/dispatches?status=SCHEDULED&page=0&size=10
GET /admin/mails/dispatches/123
```

## 후속 연결

예약 시각 도래·복구 후 지연 실행, 발송 직전 대상 자격 확인과 제외 사유, 전원 제외 시 0건 종료, 발송 처리 시작 전 취소를 연결한다. 선정 결과 변경을 어느 메일 유형에 적용할지는 별도 정책 확인이 필요하다. 실제 SES 발송·배포 검증은 이번 단계에서 수행하지 않는다.
