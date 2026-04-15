---
name: verify-java-recommended
description: 전체 테스트 실행, Jacoco 커버리지 확인 및 Flyway 마이그레이션 정합성을 검증합니다.
---

# Java Recommended Verification

## Purpose

PR 생성 전 또는 기능 구현 완료 후, 전체 시스템의 안정성과 품질 지표를 확인합니다:
- 전체 테스트 슈트 통과 확인 (단위 및 통합 테스트)
- 라인 커버리지 70% 충족 여부 (Jacoco)
- DB 마이그레이션 스크립트 유효성 검사

## Run Commands

```bash
# 전체 테스트 및 커버리지 검증
./gradlew test jacocoTestCoverageVerification
```

## Workflow

### Step 1: 전체 테스트 및 커버리지 검증

다음 명령어를 실행합니다:
- `./gradlew test`
- `./gradlew jacocoTestCoverageVerification`

**FAIL 기준:**
- 하나 이상의 테스트 실패 시
- 라인 커버리지가 70% 미만일 시 (프로젝트 `build.gradle` 기준)

### Step 2: DB 마이그레이션 검증 (Flyway)

변경된 SQL 마이그레이션 파일(`src/main/resources/db/migration/*.sql`)이 있는 경우, 구문 오류나 파일명 규칙 위반이 없는지 확인합니다.

**체크리스트:**
- [ ] 파일명이 `V{Version}__Description.sql` 형식을 따르는가?
- [ ] 기존 마이그레이션 파일이 수정되지는 않았는가? (Checksum 오류 방지)

## Exceptions

1. `@Disabled` 처리된 테스트는 실패로 간주하지 않습니다.
2. 커버리지 제외 대상(`QClass`, `DTO`, `Exception` 등)은 `build.gradle` 설정을 따릅니다.

## Related Files

- `src/main/java/**/*.java`
- `src/test/java/**/*.java`
- `src/main/resources/db/migration/*.sql`
- `build.gradle`
