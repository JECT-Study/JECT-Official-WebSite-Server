# 예약 메일 자동 실행

예약 작업은 시간대가 포함된 입력을 UTC `Instant`로 저장한다. worker는 `SCHEDULED`이며 `scheduledAt <= now`인 작업을 ID 오름차순 batch로 조회하고, 각 작업을 `findByIdForUpdate`로 잠근 뒤 상태와 시각을 다시 확인한다. 활성화와 취소는 같은 작업 잠금을 사용한다. 활성화가 먼저면 `PROCESSING` 상태에서 취소할 수 없고, 취소가 먼저면 worker가 실행하지 않는다. 즉시 발송의 `REQUESTED` 경로는 유지한다.

활성화된 예약의 각 수신자는 Outbox claim을 획득하기 직전, claim 트랜잭션 안에서 `ApplyQueryService.findCurrentEligibilityFactsByApplyIds`로 재검증한다. 지원이 없거나 다른 모집 공고에 속하거나 `SUBMITTED`가 아니면 제외한다. 선정 결과 비교는 `FIRST_PASS`, `FINAL_PASS`, `STANDBY_PASS`, `REJECT`에서만 한다. `ETC`는 선정 결과 변경만으로 제외하지 않는다. 재시도 claim도 같은 검증을 다시 수행한다. 예약 때 저장한 주소·제목·본문을 발송하며 템플릿을 다시 조회하거나 대상을 추가하지 않는다.

과거 예약의 `scenario_type_snapshot`이 없거나 결과 안내의 `selection_result_snapshot`이 없거나 해석할 수 없으면 fail-closed로 제외한다. 자격 조회 자체가 실패하면 claim과 발송을 진행하지 않고 worker의 다음 poll에서 재시도한다. 제외 사유는 개인정보를 포함하지 않는 `MailDispatchExclusionReason` 코드로 대상 조회에 노출한다.

제외 대상은 target 및 Outbox에서 `EXCLUDED`로 기록하고 같은 트랜잭션에서 작업의 `processing_count`를 줄이고 `excluded_count`를 늘린다. `target_count`는 예약 당시 수를 유지한다. 모든 대상이 제외되면 SES 호출 없이 `COMPLETED`로 종료하며 `failed_count`는 늘지 않는다. `UNKNOWN`은 기존 정책대로 자동 재발송하지 않는다.
