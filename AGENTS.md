# AGENTS.md

이 문서는 `JECT-Official-WebSite-Server` 저장소에서 Codex 계열 에이전트가 따라야 하는 저장소 기준 가이드다.
현재 저장소의 실제 기준은 코드이며, 오래된 과제 문서나 이전 템플릿 문구보다 현재 `org.ject.support` 코드 구조를 우선한다.

## 1. 저장소 정체성

- 단일 Spring Boot 백엔드 애플리케이션
- package root: `org.ject.support`
- 기본 맥락: JECT 공식 웹사이트 및 운영 백오피스 지원 서버
- 기본 타임존: `Asia/Seoul` (`SupportApplication`에서 초기화)

## 2. 기술 스택

- Java 21
- Spring Boot 3.4.1
- Spring Web, Validation, Security
- JWT 기반 인증/인가
- Spring Data JPA, Querydsl
- MySQL
- Redis cache + resilience fallback
- Flyway
- AWS S3, AWS SES
- SpringDoc Swagger
- Actuator, Prometheus
- H2, Testcontainers, Spring Boot Test, Mockito, MockMvc

## 3. 문서 우선순위

우선순위는 아래 순서를 따른다.

1. system / developer / user 지시
2. 이 문서 `AGENTS.md`
3. `.codex/README.md`
4. `.codex/rules/agent-coding-discipline.md`
5. 관련 `.codex/skills/*/SKILL.md`
6. 실제 코드와 테스트

주의:

- `docs/assignment-*`, `docs/progress-log.md`, `docs/rule/*`는 역사적 산출물일 수 있다.
- 새 Codex 작업의 필수 기준 문서는 `AGENTS.md`와 `.codex/README.md`다.
- 문서와 코드가 충돌하면 충돌 사실을 명시하고 현재 코드를 기준으로 판단한다.

## 4. 먼저 확인할 것

작업 시작 전 기본 확인 순서는 아래와 같다.

1. `AGENTS.md`
2. `.codex/README.md`
3. `.codex/rules/agent-coding-discipline.md`
4. 가장 가까운 production 코드 1~3개
5. 가장 가까운 테스트 1~3개

문서 작업이라도 실제 코드 구조를 먼저 확인하고, 코드 작업이라면 테스트를 함께 확인한다.

## 5. 현재 패키지 구조

현재 저장소는 순수 layered package 를 강제하지 않는다.
기본 구조는 feature-first 이며, 각 feature 아래에 역할별 subpackage 를 둔다.

### 핵심 루트

- `src/main/java/org/ject/support/domain`
  - 사용자 기능 도메인
  - 예: `apply`, `auth`, `file`, `jectalk`, `mail`, `member`, `ministudy`, `project`, `recruit`
- `src/main/java/org/ject/support/admin`
  - 백오피스/운영 기능
  - 예: `admin.apply`, `admin.auth`, `admin.member`
- `src/main/java/org/ject/support/common`
  - 공통 설정, 응답, 예외, 보안, 캐시, Querydsl, 로깅, 스케줄, 유틸
- `src/main/java/org/ject/support/external`
  - AWS, 이메일, S3 등 외부 연동 어댑터

### feature 내부 구조

feature 내부에서는 아래 패턴을 우선 따른다.

- `controller`
- `service`
- `repository`
- `dto`
- `exception`
- `entity` 또는 `domain`

규칙:

- 새 기능은 가장 가까운 기존 feature 패턴을 복제한다.
- 없는 계층을 억지로 추가하지 않는다.
- 기존 feature 를 `presentation/application/domain/infrastructure` 식으로 재배치하지 않는다.

## 6. 경계와 책임

### controller

- HTTP request mapping
- request/response DTO 처리
- 인증 principal 또는 request parameter 수집
- service 호출

하지 말 것:

- repository 직접 호출
- 핵심 비즈니스 규칙 구현
- 캐시/외부 연동 세부사항 직접 처리

### service

- 유스케이스 orchestration
- 트랜잭션 경계
- repository / external service 조합
- feature 규칙 실행

하지 말 것:

- 응답 wrapper 직접 처리
- controller 책임을 대신하는 HTTP 세부 처리

### repository

- JPA repository 및 Querydsl 조회 구현
- persistence access 캡슐화

하지 말 것:

- controller 로직 침범
- 외부 연동 책임 혼합

### common

- cross-cutting concern 만 둔다
- 예외, 응답 wrapper, 보안 설정, 캐시 설정, Querydsl 설정, 공통 유틸

하지 말 것:

- 특정 feature 전용 정책을 무분별하게 common 으로 승격

### external

- AWS, 이메일, S3 등 외부 시스템 연동 구현

하지 말 것:

- feature 핵심 규칙 재정의
- controller 역할 대체

## 7. 공통 구현 패턴

### 응답

- 성공 응답은 `ResponseWrapper`를 통해 `ApiResponse`로 래핑된다.
- Swagger 관련 경로는 wrapper 대상에서 제외된다.
- controller 는 가능한 한 순수 DTO 또는 값을 반환하고, 공통 포맷은 wrapper 에 맡긴다.

### 예외

- 공통 계약은 `ErrorCode` 기반이다.
- feature 예외는 각 feature 의 `*ErrorCode` enum + `*Exception` 조합을 따른다.
- 전역 예외는 `GlobalException`, `GlobalErrorCode`, `GlobalExceptionHandler`로 처리한다.
- 새 예외를 추가할 때는 기존 feature 예외 패턴을 우선 복제한다.

### 트랜잭션

- service 계층에서 `@Transactional`을 사용한다.
- 조회는 `@Transactional(readOnly = true)`를 우선 고려한다.
- controller 나 repository 에 트랜잭션 정책을 새로 퍼뜨리지 않는다.

### 캐시

- 조회 캐시는 `@Cacheable` 패턴을 우선 확인한다.
- Redis 관련 예외 처리와 fallback 은 `common.data.redis.resilience` 규칙에 맞춘다.
- 캐시 장애를 비즈니스 실패로 그대로 전파할지, DB fallback 으로 흡수할지는 기존 패턴을 따른다.

### 보안

- JWT 필터 체인과 `SecurityConfig`를 기준으로 판단한다.
- role hierarchy 는 `RoleHierarchySpec`과 `Role` 정의를 따른다.
- controller 테스트는 보안 필터 활성 여부를 테스트 목적에 맞춰 선택한다.

### 외부 연동

- S3, SES, 이메일 인증 등은 `external/*` 구현을 먼저 확인한다.
- 외부 호출 규칙을 바꿀 때는 timeout, 예외 매핑, 운영 영향까지 함께 검토한다.

## 8. 코딩 원칙

- 백엔드 엔지니어링 맥락으로 판단한다.
- 아키텍처, API 디자인, DB 모델링/쿼리, 캐시 전략, 운영 영향까지 함께 본다.
- 가장 가까운 기존 구현을 먼저 찾고 같은 스타일로 맞춘다.
- 필요한 최소 범위만 수정한다.
- unrelated rename, mass formatting, 패키지 대이동은 하지 않는다.
- 불확실한 사실은 추정으로 단정하지 않는다.

## 9. 테스트 원칙

현재 저장소는 여러 테스트 스타일을 함께 사용한다.

### 기본 베이스

- `TestSupport`
  - `@ActiveProfiles("test")`
  - display name generation 기본 제공
- `UnitTestSupport`
  - Mockito 기반 단위 테스트
- `@IntegrationTest`
  - `@SpringBootTest`
  - MySQL/Redis Testcontainers import

### 대표 테스트 스타일

- 단위 테스트: service, domain rule, utility
- `@DataJpaTest`: repository/query 검증
- `@WebMvcTest`: controller slice 검증
- `@SpringBootTest` + `@AutoConfigureMockMvc`: 통합 API 및 보안 검증

### 테스트 규칙

- 코드 변경 시 가장 가까운 테스트를 먼저 보강한다.
- 가능하면 가장 작은 failing test 또는 회귀 테스트부터 추가한다.
- 새 source set 은 요청 없이는 만들지 않는다.
- 검증은 가장 좁은 Gradle 명령부터 실행한다.
- 실행하지 않은 검증은 실행했다고 쓰지 않는다.

### 10. 작업 마무리 및 검증 규칙

Codex는 작업을 완료하고 사용자에게 보고하기 전, 반드시 다음 단계를 거쳐야 한다.

1.  **자동 검증 실행:** `/verify-implementation` 명령을 호출하여 전체 검증(컴파일, 테스트, 커버리지 등)을 수행한다.
2.  **포맷팅 확인:** `Spotless`나 프로젝트 스타일 맞춤형 도구를 실행하여 코드 스타일이 일관됨을 보장한다.
3.  **검증 결과 반영:** 검증 중 발견된 이슈(컴파일 오류, 테스트 실패, 커버리지 미달)는 반드시 해결한 후 작업을 종료한다.
4.  **보고:** `/verify-implementation` 보고서를 요약하여 작업 결과(walkthrough)와 함께 제출한다.

### 11. 의도 기반 스킬 자동화 (Prompt Convention)

Codex는 사용자의 모호한 요청에서도 의도를 파악하여 `.agent/PROMPT_CONVENTION.md`에 정의된 적절한 스킬을 능동적으로 제안하거나 실행해야 한다.

- **작업 시작 의도:** "시작하자", "만들어줘" 등의 요청 시 `github-issue-create` 등을 먼저 제안한다.
- **검증 및 마무리 의도:** "다 됐어", "PR 올려줘" 등의 요청 시 반드시 `/verify-implementation`을 먼저 실행하여 품질을 확인한다.
- **습관적 피드백:** 사용자가 명시적으로 스킬을 언급하지 않더라도, 현재 맥락에서 가장 필요한 스킬이 무엇인지 판단하고 행동한다.

## 12. 자주 쓰는 명령

```bash
# 전체 테스트
./gradlew test

# 특정 테스트 클래스
./gradlew test --tests "org.ject.support.domain.project.service.ProjectServiceTest"

# JPA/Repository 테스트
./gradlew test --tests "org.ject.support.domain.apply.repository.ApplyRepositoryTest"

# MVC 슬라이스 테스트
./gradlew test --tests "org.ject.support.domain.mail.controller.AdminMailScenarioControllerTest"

# 전체 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```

## 11. 문서 동기화

다음이 바뀌면 Codex 문서 동기화가 필요하다.

- package 구조 해석
- 공통 예외/응답 규칙
- 보안 규칙
- 캐시 규칙
- 테스트 전략
- agent / skill 사용 흐름

기본 반영 대상:

- `AGENTS.md`
- `.codex/README.md`
- 필요 시 `.codex/rules/*`
- 필요 시 `.codex/skills/*`
- 필요 시 `.codex/agents/*.toml`

## 12. Feature / Thread 규칙

- 한 feature 는 정확히 하나의 plan 으로 다룬다.
- 한 thread 에서는 하나의 feature 만 다룬다.
- 같은 feature 를 여러 thread 로 분산하지 않는다.
- 설계, 구현, 수정, 리뷰는 현재 feature plan 범위 안에서만 진행한다.
- 범위가 모호하면 확장하기 전에 먼저 명확히 한다.

## 13. 중단 조건

아래 경우에는 임의로 확장 구현하거나 문서를 단정하지 않는다.

- 현재 코드와 문서가 크게 충돌하는데 기준이 불명확한 경우
- feature 범위를 넘는 대규모 구조 변경이 필요한 경우
- 보안/캐시/외부 연동 정책을 바꾸는데 운영 영향 확인이 없는 경우
- 사실 확인이 불가능한 내용을 가이드에 확정 규칙으로 써야 하는 경우
