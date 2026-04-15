---
name: test
description: Add or update repository-style tests for this JECT backend, including unit, JPA, MVC, security, and Spring Boot integration coverage that matches existing local patterns.
---

# Test

## Language

모든 응답은 한국어로 작성한다.

## 먼저 읽을 문서

- `AGENTS.md`
- `.codex/README.md`
- `.codex/rules/agent-coding-discipline.md`
- 가장 가까운 기존 테스트
- 필요 시 관련 production 코드

## 수행 규칙

1. 가장 가까운 테스트 파일을 찾는다.
2. 같은 패키지와 작성 스타일을 복제한다.
3. 가능한 경우 가장 작은 failing test 또는 회귀 테스트부터 고정한다.
4. 테스트 범위는 가장 작은 것으로 선택한다.
5. 가장 좁은 Gradle 검증부터 실행한다.

## 현재 저장소 테스트 패턴

- `TestSupport`
  - 공통 test profile 과 display name generation 제공
- `UnitTestSupport`
  - Mockito 기반 단위 테스트
- `@DataJpaTest`
  - repository / Querydsl 검증
- `@WebMvcTest`
  - controller slice 검증
- `@SpringBootTest`
  - 통합 컨텍스트 및 보안 검증
- `@IntegrationTest`
  - MySQL / Redis Testcontainers 기반 통합 검증

## 실행 예시

- `./gradlew test --tests "org.ject.support.domain.project.service.ProjectServiceTest"`
- `./gradlew test --tests "org.ject.support.domain.apply.repository.ApplyRepositoryTest"`
- `./gradlew test --tests "org.ject.support.admin.controller.mail.AdminMailScenarioControllerTest"`
- `./gradlew test --tests "org.ject.support.common.security.config.SecurityConfigTest"`
- `./gradlew test`

## 최소 보고

- 추가/수정한 테스트 파일
- 실행한 Gradle 명령
- 미실행 검증 항목
