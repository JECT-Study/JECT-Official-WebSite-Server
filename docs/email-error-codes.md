# 이메일 오류 코드 전환

외부 이메일 오류의 응답 코드를 `EMAIL-N` 형식으로 통일한다. enum 이름·HTTP 상태·메시지·재시도 분류는 유지한다. 기존 `MAIL-N` 번호는 변경하지 않는다.

| 기존 코드와 enum 이름 | 새 응답 코드 | HTTP 상태 |
| --- | --- | --- |
| `INVALID_EMAIL_TEMPLATE` | `EMAIL-1` | 400 |
| `EMAIL_SEND_FAILURE` | `EMAIL-2` | 503 |
| `EMAIL_SEND_RESULT_UNKNOWN` | `EMAIL-3` | 503 |
| `TOO_MANY_EMAIL_REQUESTS` | `EMAIL-4` | 429 |

새 오류 응답과 신규 발송 실패 기록은 새 코드를 사용한다. 기존 발송 DB 기록은 일괄 갱신하지 않는다. 수신자 결과의 `failureReason`, `lastAttemptFailureReason`은 알려진 옛 문자열만 새 코드로 변환해 반환한다. 새 코드·`MAIL-N`·알 수 없는 값·null은 그대로 유지한다. 조회가 DB 값을 다시 저장하지 않는다.

FE는 오류 응답과 실패 사유에서 기존 문자열로 분기하는 곳을 위 코드로 전환해야 한다. 인증 메일 등 기존 `EmailErrorCode` 호출 경로도 응답 코드가 바뀐다. 이전 서버와 새 서버가 함께 실행되는 배포 구간에는 양쪽 코드를 수용해야 한다. 이 문서는 전환 계약이며 FE 전달·반영 완료를 뜻하지 않는다.

DDL 변경 없음. DB 실패 기록을 직접 읽는 운영 도구는 기존 문자열과 새 코드가 공존함을 고려한다. 재시도 횟수·429 재시도·UNKNOWN 자동 재발송 금지 정책은 변경하지 않는다.
