---
name: verify-java-essential
description: Java 메인 및 테스트 코드의 빌드 가능성과 기본적인 코드 린트 작업을 수행합니다.
---

# Java Essential Verification

## Purpose

Java 프로젝트의 기본적인 건강 상태를 빠르게 확인합니다:
- 메인 코드 및 테스트 코드 컴파일 가능 여부 확인
- 디버깅용 잔여물(printStacktrace, System.out) 탐지
- 미정의 FIXME/TODO 주석 확인

## Run Commands

```bash
# 컴파일 검사
./gradlew classes testClasses
```

## Workflow

### Step 1: 컴파일 검증

다음 명령어를 실행하여 빌드가 깨지지 않았는지 확인합니다:
- `./gradlew classes` (메인 코드)
- `./gradlew testClasses` (테스트 코드)

**FAIL 기준:**
- 컴파일 에러 발생 시 즉시 실패

### Step 2: 코드 클린업 검증

변경된 파일들을 대상으로 다음 패턴을 탐지합니다:

| 검사 항목 | 패턴 (Grep) | 설명 |
|-----------|-------------|------|
| StackTrace | `\.printStackTrace\(\)` | `log.error` 대신 사용된 흔적 |
| Standard UI | `System\.(out|err)\.print` | 디버깅용 출력 잔여물 |
| FIXME | `FIXME` | 해결되지 않은 치명적 이슈 |

### Step 3: 어노테이션 및 설정 확인

- `@Transactional`이 누락된 서비스 메서드나, 비정상적인 QueryDSL 생성 파일 포함 여부를 체크합니다.

## Exceptions

1. `build/`, `out/`, `generated/` 디렉토리 내의 파일은 검사에서 제외합니다.
2. 테스트 코드(`src/test`) 내에서의 `System.out.println`은 유동적으로 허용할 수 있습니다.

## Related Files

- `src/main/java/**/*.java`
- `src/test/java/**/*.java`
- `build.gradle`
