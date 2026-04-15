# Agent Coding Discipline

이 문서는 이 저장소에서 Codex 가 실제 작업할 때 따라야 하는 실행 규율을 정의한다.
`AGENTS.md`가 저장소 기준을 고정하고, 이 문서는 구현 행동을 고정한다.

## 1. 우선순위

1. system / developer / user 지시
2. `AGENTS.md`
3. `.codex/README.md`
4. 이 문서
5. `.codex/skills/*/SKILL.md`
6. `.codex/agents/*.toml`

## 2. 시작 전 필수 확인

코드 또는 문서 수정 전 아래를 확인한다.

1. `AGENTS.md`
2. `.codex/README.md`
3. 가장 가까운 관련 소스 파일 1~3개
4. 가장 가까운 관련 테스트 1~3개
5. 필요 시 `.codex/agents/WORKFLOW.md`
6. 필요 시 관련 skill 문서

규칙:

- 가장 가까운 기존 구현을 확인하기 전 새 패턴을 만들지 않는다.
- 문서만 바꾸는 작업이어도 실제 코드 구조를 먼저 확인한다.
- 역사적 문서를 현재 규칙처럼 단정하지 않는다.

## 3. 기본 원칙

- feature-first 구조를 유지한다.
- 필요한 최소 범위만 수정한다.
- unrelated rename, mass formatting, 패키지 대이동은 하지 않는다.
- 실행하지 않은 검증은 실행한 것처럼 보고하지 않는다.
- 불확실한 내용은 사실처럼 적지 않는다.

## 4. 패키지 규율

현재 저장소의 기본 경계는 아래와 같다.

- `domain/*`: 사용자 기능
- `admin/*`: 백오피스 기능
- `common/*`: 공통 설정과 cross-cutting concern
- `external/*`: 외부 연동 어댑터

규칙:

- 새 코드도 가장 가까운 feature package 안에 둔다.
- `common`에는 정말 공통인 설정, 응답, 예외, 보안, 캐시, 유틸만 둔다.
- 특정 feature 전용 정책을 조기에 `common`으로 올리지 않는다.
- `external`은 외부 시스템 연동 구현에만 사용한다.

## 5. 역할 분리 규율

### controller

- HTTP 입출력과 request mapping 담당
- service 호출까지만 수행

금지:

- repository 직접 호출
- 핵심 비즈니스 규칙 구현

### service

- orchestration 과 transaction 담당
- repository, external, cache 사용 조합

금지:

- 응답 wrapper 직접 처리
- controller 세부 책임 흡수

### repository

- JPA / Querydsl 접근 담당

금지:

- 외부 연동 책임 혼합

## 6. 공통 기술 경계 점검

### 예외 / 응답

- 성공 응답은 `ResponseWrapper` + `ApiResponse` 기준을 따른다.
- 예외는 `ErrorCode`, `BusinessException`, `GlobalException`, `GlobalExceptionHandler` 패턴을 따른다.

### 보안

- JWT 필터 체인과 `SecurityConfig`를 기준으로 판단한다.
- role hierarchy 변경은 `Role`, `RoleHierarchySpec`, 보안 테스트까지 함께 본다.

### 캐시

- Redis 관련 변경은 `common.data.redis`와 resilience 패키지를 함께 확인한다.
- 캐시 장애 시 fallback 동작이 기존 정책과 맞는지 검토한다.

### 외부 연동

- S3 / SES / 이메일 인증 변경은 `external/*` 구현과 예외 정책을 함께 본다.

### JPA / Querydsl

- repository 또는 query 구현을 바꾸면 관련 `@DataJpaTest` 또는 Querydsl 테스트 필요성을 판단한다.

## 7. 테스트 규율

기본 순서는 아래와 같다.

1. 변경할 요구사항 또는 버그 조건을 고정한다.
2. 가장 가까운 테스트를 찾는다.
3. 가능한 경우 가장 작은 failing test 또는 회귀 테스트를 먼저 추가한다.
4. 그 테스트를 통과시키는 최소 코드를 수정한다.
5. 가장 좁은 검증을 실행한다.

현재 저장소의 대표 패턴:

- `TestSupport`
- `UnitTestSupport`
- `@DataJpaTest`
- `@WebMvcTest`
- `@SpringBootTest`
- `@IntegrationTest`

## 8. 문서 동기화 규율

아래가 바뀌면 Codex 문서를 같이 본다.

- package 구조 해석
- 공통 예외/응답 규칙
- 보안 규칙
- 캐시 규칙
- 테스트 전략
- agent / skill 사용 흐름

기본 반영 대상:

- `AGENTS.md`
- `.codex/README.md`
- `.codex/rules/*`
- `.codex/skills/*`
- `.codex/agents/*.toml`

## 9. 보고 규율

최소 보고에는 아래가 포함되어야 한다.

- 어떤 feature 또는 문제를 기준으로 작업했는지
- 추가/수정한 테스트
- 수정한 파일
- 실행한 검증
- 남은 gap 또는 미실행 항목

## 10. 중단 조건

아래 경우에는 임의 우회하지 않는다.

- feature 범위가 불명확한 경우
- 코드와 문서가 충돌하지만 어느 쪽도 신뢰하기 어려운 경우
- 보안 / 캐시 / 외부 연동 정책을 크게 바꾸는데 근거가 부족한 경우
- 작업 범위 밖 구조 변경 없이는 진행이 어려운 경우
