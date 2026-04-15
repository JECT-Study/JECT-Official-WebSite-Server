# Coding Workflow

이 문서는 이 저장소에서 Codex 가 구현 작업을 진행할 때의 권장 절차를 정의한다.

## 1. 시작 순서

1. `AGENTS.md`
2. `docs/assignment-design.md`
3. `docs/progress-log.md`
4. `docs/phase-thread-handoff.md`
5. 관련 구조 문서
6. 가장 가까운 기존 구현
7. 가장 가까운 기존 테스트

## 2. 구현 원칙

- 새 패턴을 만들기 전에 가장 가까운 기존 코드를 복제한다.
- 필요한 최소 범위만 수정한다.
- unrelated rename, mass formatting, 대규모 패키지 재배치는 하지 않는다.
- 현재 phase 범위를 넘는 일반화는 피한다.

## 3. 테스트 우선 규율

기본 순서는 아래와 같다.

1. 바뀌는 요구사항 또는 버그 조건을 고정한다.
2. 가장 가까운 테스트를 찾아 같은 스타일로 보강한다.
3. 필요한 최소 프로덕션 코드를 수정한다.
4. 가장 좁은 Gradle 검증부터 실행한다.

예외:

- 문서만 수정하는 작업
- 순수 라우팅/설명 문서 정리

## 4. 테스트 위치 기준

- `src/test/java`
    - domain 규칙
    - application service
    - MVC/JPA 를 포함하는 현 저장소의 통합 성격 테스트도 현재는 여기에 존재

새로운 source set 을 추가하는 것은 요청 없이는 하지 않는다.

## 5. 검증 순서

- 특정 테스트 클래스: `./gradlew test --tests "..."`
- 전체 테스트: `./gradlew test`
- 전체 빌드: `./gradlew build`

실행하지 않은 검증은 실행했다고 보고하지 않는다.

## 6. 문서 동기화

다음 변경은 문서 동기화가 필요하다.

- 상태 전이 규칙
- API 계약
- Mock Worker 처리 흐름
- 로그/예외 정책
- Phase 범위 판단

반영 대상:

- `docs/assignment-design.md`
- `docs/progress-log.md`
- 필요 시 `AGENTS.md` 또는 `docs/rule/*`
