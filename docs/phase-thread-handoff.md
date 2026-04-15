# Phase Thread Handoff Guide

이 문서는 **컨텍스트 오염 방지**를 위해 phase 별로 채팅 스레드를 분리할 때의 최소 운영 규칙을 정의한다.

## 1. 스레드 분리 원칙

- 한 스레드는 하나의 phase 목표만 다룬다.
- 새 phase 시작 시 새 스레드를 연다.
- 이전 스레드의 임의 기억에 의존하지 않는다.

## 2. 새 스레드 시작 템플릿

새 스레드 첫 메시지는 아래 순서로 고정한다.

1. 현재 phase 목표
2. 이번 phase의 in-scope / out-of-scope
3. 반드시 읽어야 할 문서 목록
4. 완료 기준(acceptance criteria)

예시:

```text
Phase 2를 시작한다.
In-scope: 재시작 복구, 백오프 고도화
Out-of-scope: MQ 도입
먼저 AGENTS.md, docs/assignment-design.md, docs/progress-log.md, docs/phase-thread-handoff.md를 읽고 시작해라.
완료 기준: 복구 시나리오 테스트 포함.
```

## 3. 스레드 종료 체크리스트

phase 작업이 끝나면 반드시 아래를 수행한다.

1. `docs/progress-log.md` 갱신
2. `docs/assignment-design.md` 변경 반영
3. 다음 phase 시작 가이드 3~5줄 추가

## 4. 문서 우선순위

다음 순서로 판단한다.

1. 사용자 지시
2. `AGENTS.md`
3. `docs/assignment-design.md`
4. `docs/rule/*`
5. `docs/progress-log.md`
6. 코드/테스트

## 5. phase 별 산출물 규칙

- Phase 1: 기본 API + Mock Worker + 테스트
- Phase 2: 복구/재시도/동시성 보강
- Phase 3: MQ/분산락 등 확장(필요 시)

각 phase 에서 미완료 항목은 `docs/progress-log.md`의 Deferred 항목으로 넘긴다.
