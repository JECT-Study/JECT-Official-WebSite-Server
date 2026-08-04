---
name: check-code-convention
description: 코드 변경사항을 프로젝트 코드 컨벤션 초안과 비교해 검토합니다. apply/admin apply 패키지 일관성, Controller/Service/DTO/Domain 네이밍, validation, transaction, repository query, Flyway migration, pre-hook 후보를 점검할 때 사용합니다.
---

# 코드 컨벤션 검토

## Purpose

`docs/CODE_CONVENTIONS.md`를 기준으로 코드 변경사항이 프로젝트의 코드 컨벤션 초안과 일관되는지 검토합니다.
이 스킬은 확정된 자동 검사기가 아니라, PR에서 논의할 컨벤션 이슈를 빠르게 발견하기 위한 리뷰 기준입니다.

## Primary Reference

- `docs/CODE_CONVENTIONS.md`

검토 전에 반드시 위 문서를 읽습니다. 문서는 초안이므로, 명확한 위반과 논의가 필요한 항목을 구분합니다.

## Review Scope

- 우선 검토 범위는 `org.ject.support.domain.apply`와 `org.ject.support.admin.apply`입니다.
- 다른 패키지는 컨벤션 적용 범위를 확장할 때 보조 참고 자료로만 사용합니다.
- 문서와 실제 코드가 충돌하면 코드 변경을 바로 요구하지 말고 논의 항목으로 분리합니다.

## Checkpoints

1. **Naming** — Controller, Service, DTO, Domain 메서드명이 계층별 책임과 도메인 의미를 드러내는지 확인합니다.
2. **Controller** — HTTP mapping, 인증, validation, service 호출 외의 비즈니스 로직이 들어가지 않았는지 확인합니다.
3. **DTO/Validation** — Request/Response 분리, wrapper DTO, `@Valid`, `@NotNull`/`@NotBlank`/`@NotEmpty` 사용이 적절한지 확인합니다.
4. **Service/Transaction** — 조회 메서드는 read-only transaction, 상태 변경 메서드는 transaction을 사용하는지 확인합니다.
5. **Repository/Flyway/Test** — QueryDSL 조건, N+1 가능성, migration 파일 수정 여부, 필요한 테스트 또는 수동 검증 기록을 확인합니다.

## Workflow

1. 변경 파일 목록을 확인합니다.

   ```bash
   git diff --name-only
   git ls-files --others --exclude-standard
   ```

2. 변경 파일이 Controller, DTO, Service, Repository, Domain, Exception, Flyway, Test 중 어디에 해당하는지 분류합니다.

3. `docs/CODE_CONVENTIONS.md`의 해당 섹션과 비교합니다.

4. 결과를 다음 기준으로 분리합니다.

   - **Actionable finding**: 현재 초안 기준으로 불일치가 명확하고 유지보수성, 검증, 트랜잭션, 쿼리 안정성에 영향을 줄 수 있는 항목
   - **Discussion item**: 팀 합의가 아직 필요한 항목
   - **No issue**: 초안 기준과 일치하거나, 기존 코드 흐름상 자연스러운 항목

5. pre-hook 후보는 포맷팅, import order, migration filename처럼 기계적으로 검사 가능한 항목만 제안합니다.

## Output

- 코드 리뷰 요청이면 findings를 먼저 작성합니다.
- 각 finding에는 파일과 줄 번호를 포함합니다.
- 논의 항목은 findings와 분리합니다.
- 이슈가 없으면 명확히 없다고 말하고, 남은 논의 항목이나 테스트 갭만 짧게 언급합니다.

## Exceptions

1. 초안 문서 자체를 수정하는 PR은 컨벤션 위반으로 보지 않고 문서 품질과 논의 가능성을 중심으로 검토합니다.
2. 기존 코드와 충돌하는 컨벤션은 바로 수정 요구하지 않고 discussion item으로 분류합니다.
3. apply/admin apply 밖의 변경은 명확히 관련된 경우에만 참고합니다.
