---
name: implementation
description: Modify Java and Spring Boot production code in this repository while preserving the current feature-first package structure and existing response, exception, security, cache, and transaction conventions.
---

# Implementation

## Language

모든 응답은 한국어로 작성한다.

## 먼저 읽을 문서

- `AGENTS.md`
- `.codex/README.md`
- `.codex/rules/agent-coding-discipline.md`
- 가장 가까운 기존 구현
- 가장 가까운 기존 테스트

## 수행 규칙

1. 가장 가까운 기존 구현 1~3개를 찾는다.
2. 같은 feature package 와 예외/응답 스타일을 맞춘다.
3. 코드 변경이면 테스트를 먼저 보강한다.
4. 필요한 최소 코드만 수정한다.
5. 구조나 규칙이 바뀌면 Codex 문서 영향도 판단한다.

## 현재 저장소 기준 빠른 체크

- package root 는 `org.ject.support`
- 구조는 feature-first 이다.
- controller 는 HTTP 입출력만 담당한다.
- service 는 orchestration 과 transaction 경계를 담당한다.
- 성공 응답은 `ResponseWrapper`를 통해 `ApiResponse`로 래핑된다.
- 예외는 `ErrorCode` + feature/global exception 패턴을 따른다.
- 조회 캐시는 기존 `@Cacheable` + Redis fallback 패턴을 우선 확인한다.
- 보안은 JWT 필터 체인과 role hierarchy 기준을 따른다.

## 금지

- 새 generic layered package 구조 도입
- controller 에 비즈니스 로직 추가
- feature 전용 정책을 근거 없이 `common`으로 승격
- 문서 근거 없이 보안/캐시/외부 연동 규칙 재정의

## 최소 보고

- 수정한 파일
- 추가/수정한 테스트
- 실행한 검증
- 남은 assumption 또는 gap
