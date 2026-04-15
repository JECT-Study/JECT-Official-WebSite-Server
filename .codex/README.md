# Codex Usage

이 문서는 현재 `JECT-Official-WebSite-Server` 저장소에서 Codex 문서를 어떻게 읽고 쓸지 빠르게 안내한다.

## 1. 기준 문서

Codex 작업의 공식 기준 문서는 아래 두 개다.

- `AGENTS.md`
- `.codex/README.md`

보조 기준 문서:

- `.codex/rules/agent-coding-discipline.md`
- `.codex/agents/WORKFLOW.md`
- `.codex/skills/*/SKILL.md`
- `.codex/agents/*.toml`

주의:

- `docs/assignment-*`, `docs/progress-log.md`, `docs/rule/*`는 현재 Codex 작업의 필수 선행 문서가 아니다.
- 필요 시 참고할 수는 있지만, 현재 코드보다 우선하지 않는다.

## 2. 시작 순서

일반적인 작업 시작 순서는 아래와 같다.

1. `AGENTS.md`
2. `.codex/README.md`
3. `.codex/rules/agent-coding-discipline.md`
4. 가장 가까운 production 코드
5. 가장 가까운 테스트
6. 필요 시 관련 skill

문서 작업이어도 실제 코드 구조를 먼저 확인한다.

## 3. 기본 작업 흐름

1. 요청의 feature 범위를 고정한다.
2. 가장 가까운 코드와 테스트를 찾는다.
3. 코드 작업이면 test-first 를 우선한다.
4. 최소 범위만 수정한다.
5. 가장 좁은 검증부터 실행한다.
6. 구조나 규칙이 바뀌면 Codex 문서를 같이 맞춘다.

상세 실행 규율은 `.codex/agents/WORKFLOW.md`와 `.codex/rules/agent-coding-discipline.md`를 본다.

## 4. 언제 어떤 skill 을 쓰나

### `implementation`

이럴 때:

- Spring Boot production code 를 수정할 때
- controller / service / repository / security / cache / external 연동을 바꿀 때

### `test`

이럴 때:

- 단위 테스트, JPA 테스트, MVC 테스트, SpringBootTest 를 추가하거나 보강할 때
- 회귀 테스트를 먼저 고정할 때

### `docs-sync`

이럴 때:

- `AGENTS.md` 또는 `.codex/**` 문서를 현재 코드 기준으로 맞출 때
- 구현 변경 때문에 Codex 문서 기준이 바뀌었을 때

## 5. 언제 어떤 agent 를 쓰나

### `requirement-extractor`

- 요청을 구현 가능한 요구사항, 제약, 가정으로 정리할 때

### `domain-spec-designer`

- feature 규칙, 상태, 제약, 도메인 용어를 먼저 정리할 때

### `application-flow-designer`

- service orchestration, transaction 경계, cache / security / external 흐름을 정리할 때

### `architecture-rule-auditor`

- 변경안이 `domain/admin/common/external` 경계를 지키는지 점검할 때

### `implementer`

- 실제 코드와 테스트를 수정할 때

### `spec-conformance-auditor`

- 구현이 승인된 요구사항을 만족하는지 근거 기반으로 검토할 때

### `docs-sync-writer`

- 구현 이후 Codex 문서를 동기화할 때

## 6. 가장 실용적인 사용법

대부분의 경우 아래처럼 요청하면 충분하다.

```text
이 버그 수정해줘. implementation 흐름으로 진행하고 테스트도 같이 맞춰줘.
```

```text
먼저 requirement-extractor 와 architecture-rule-auditor 관점으로 범위부터 정리해줘.
```

```text
이번 변경으로 Codex 문서 기준이 달라졌으면 docs-sync 도 같이 반영해줘.
```

즉, agent 이름이나 skill 이름을 요청에 직접 넣으면 Codex 가 해당 역할 문서를 기준으로 움직이기 쉽다.

## 7. 문서 유지 원칙

- 새 Codex 문서는 현재 코드 현실을 설명해야 한다.
- 역사적 문서를 현재 규칙처럼 강제하지 않는다.
- `org.ject.support` 구조와 실제 테스트 패턴을 우선 반영한다.
- stale 키워드나 다른 저장소 템플릿 흔적은 남기지 않는다.
