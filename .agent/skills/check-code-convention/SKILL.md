---
name: check-code-convention
description: 코드 변경을 프로젝트 코드 컨벤션과 비교해 Controller, UseCase, Service, DTO, Domain, Repository, Test 책임과 네이밍을 검토합니다.
---

# 코드 컨벤션 검토

## 기준

`docs/CODE_CONVENTIONS.md`를 최우선 기준으로 사용합니다.

## 검토 범위 확인

PR 전체 변경과 로컬 변경을 모두 확인합니다.

```bash
base_ref="$(gh pr view --json baseRefName --jq '.baseRefName')"
git fetch origin "${base_ref}"
git diff --name-only "origin/${base_ref}...HEAD"
git diff --name-only
git diff --name-only --cached
git ls-files --others --exclude-standard
```

## 검토 항목

1. Controller가 HTTP 계층 책임만 담당하고 `ApiSpec` 적용 규칙을 지키는지 확인합니다.
2. UseCase와 Service의 책임, public 기능, 트랜잭션 경계를 확인합니다.
3. `get`, `search`, `create`, `edit`, `update`, `delete`와 상태 전이 동사의 의미를 확인합니다.
4. Request/Response 분리, wrapper DTO, 컬렉션 타입과 validation을 확인합니다.
5. Request DTO가 `toEntity()`를 사용하지 않고 Domain의 `create()`를 통해 생성되는지 확인합니다.
6. Entity 상태 변경, Repository 조회 방식, soft delete 처리가 규칙에 맞는지 확인합니다.
7. 테스트명이 구현 표현이 아닌 도메인 시나리오를 나타내는지 확인합니다.
8. 주석과 개행이 필요한 만큼만 사용되었는지 확인합니다.

## 결과 작성

- 문제를 우선순위가 높은 순서대로 작성합니다.
- 각 문제에 파일과 줄 번호, 영향, 수정 방향을 포함합니다.
- 문제가 없으면 확인한 범위와 남은 검증 위험을 간단히 작성합니다.
