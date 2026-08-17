# Code Conventions

## Controller

- Controller는 HTTP 요청 매핑, 인증, 형식 검증, UseCase 또는 Service 호출만 담당합니다.
- 비즈니스 로직과 Entity 상태 변경은 Controller에 작성하지 않습니다.
- 신규 Controller는 Swagger 명세를 `ApiSpec`에 작성하고 이를 구현합니다.
- 기존 Controller는 해당 파일을 수정할 때 점진적으로 `ApiSpec`을 적용합니다.
- Controller 파라미터의 `final`은 강제하지 않습니다.
- 단건 조회는 `get{Domain}`을 사용합니다.
- 단순 목록 조회는 `get{Domains}`를 사용하고, 자연스러운 복수형이 어려우면 `get{Domain}List`를 사용합니다.
- 조건, 필터, 페이징 기반 조회는 `search{Domains}`를 사용합니다.
- 사용자 입력으로 기존 정보를 편집하는 기능은 `edit{Domain}`을 사용합니다.
- 상태값이나 시스템 내부 값의 갱신은 `update{Domain}`을 사용합니다.
- 단건 삭제는 `delete{Domain}`, 다건 삭제는 `delete{Domains}`를 사용합니다.
- 메서드명에 `bulk`를 사용하지 않고 복수 목적어로 다건 작업을 표현합니다.

## UseCase And Service

- 여러 Service의 작업을 조합해야 할 때 UseCase를 둡니다.
- Repository 하나와 단일 도메인만 다루는 기능은 Service에서 처리합니다.
- UseCase는 전체 업무 흐름과 트랜잭션 경계를 담당합니다.
- Service는 도메인별 조회, 검증, 상태 변경, 저장 기능을 제공합니다.
- Controller에서 여러 Service를 직접 조합하지 않습니다.
- UseCase와 Service의 메서드명을 계층 구분만을 위해 억지로 다르게 만들지 않습니다.
- public 메서드는 외부에서 독립적으로 사용할 수 있는 기능만 제공합니다.
- 한 곳에서만 사용하는 단순 조건문은 호출부에 작성합니다.
- 사용자 입력 편집은 `edit`, 상태 또는 시스템 값 갱신은 `update`를 사용합니다.
- 상태 전이는 `submit`, `approve`, `reject`, `cancel`, `restore`처럼 구체적인 도메인 동사를 우선합니다.
- 여러 저장 작업이 하나의 업무로 함께 성공하거나 실패해야 하면 동일한 트랜잭션으로 처리합니다.
- 조회 전용 트랜잭션은 `readOnly = true`를 사용합니다.

## Method Verbs

- `get`: 단건 또는 단순 목록 조회
- `search`: 조건, 필터, 페이징 기반 조회
- `create`: 신규 도메인 객체 생성
- `save`: 임시 저장 또는 저장 자체가 도메인 행위인 경우
- `edit`: 사용자 입력으로 기존 정보 편집
- `update`: 상태값 또는 시스템 내부 값 갱신
- `delete`: 삭제

## DTO

- API Request와 Response는 Entity를 직접 노출하지 않고 DTO로 분리합니다.
- 단순 DTO는 record를 사용합니다.
- Request DTO는 `{Action}{Domain}Request` 형식을 사용합니다.
- Response DTO는 `{Domain}Response` 형식을 사용하고, 목적 구분이 필요하면 `{Purpose}{Domain}Response`를 사용합니다.
- Request Body는 단순 ID 목록이라도 wrapper DTO로 받습니다.
- 값 하나는 메서드 매개변수로 전달할 수 있고, 값이 둘 이상이거나 컬렉션이면 DTO로 묶습니다.
- Service와 UseCase는 API Request DTO를 직접 받을 수 있습니다.
- 중복을 하나로 처리하면 `Set`, 중복을 오류로 처리하면 `List`를 사용합니다.
- 다른 객체 하나에서 현재 DTO로 변환하면 `from(객체)`을 사용합니다.
- 여러 준비된 값을 조합해 DTO를 만들면 `of(값1, 값2, 값3)`를 사용합니다.
- Request DTO의 `toEntity()`는 사용하지 않습니다.
- Service 또는 UseCase가 Request DTO에서 값을 꺼내 Domain의 `create()`를 호출합니다.

## Validation

- 자료형, null, 문자열 길이, 컬렉션 크기 등 형식 검증은 Request DTO에서 처리합니다.
- 존재 여부, 중복 여부, 상태 전이 가능 여부 등 비즈니스 검증은 Service 또는 Domain에서 처리합니다.
- Controller에서 비즈니스 검증을 직접 수행하지 않습니다.
- 필수 컬렉션은 `@NotEmpty`를 사용하고 원소 검증도 명시합니다.

```java
@NotEmpty
Set<@NotNull Long> memberIds
```

## Domain And Entity

- setter를 사용하지 않고 의미 있는 상태 변경 메서드를 제공합니다.
- 기본 생성 정적 팩토리는 `create`를 사용합니다.
- `Apply.createApply()`처럼 클래스명을 반복하지 않습니다.
- 상태가 중요한 생성만 `createJoined`, `createSubmitted`처럼 표현합니다.
- boolean 메서드는 `is`, `has`, `can`, `should`를 사용합니다.
- 범용 `updateStatus()`보다 구체적인 상태 전이 메서드를 우선합니다.

```java
Apply apply = Apply.create(applicant, recruit);
apply.submit(applicationForm);
apply.reject();
apply.restore();
```

## Repository

- 단순 조회는 Spring Data 파생 메서드를 사용합니다.
- 간단한 고정 쿼리는 JPQL `@Query`를 사용할 수 있습니다.
- 동적 조건, 복잡한 projection, 페이징은 QueryDSL custom Repository를 사용합니다.
- N+1이 발생하는 조회는 fetch join으로 해결합니다.
- 페이지 조회는 content와 count 쿼리를 분리합니다.
- `@SQLDelete`가 적용된 Entity는 `delete()` 또는 `deleteAll()`로 삭제합니다.

## Exception And Logging

- 도메인별 예외와 오류 코드를 사용합니다.
- 예외 메시지에 민감한 정보나 전체 요청 본문을 포함하지 않습니다.
- 일괄 처리 실패를 추적해야 하면 대표 실패 식별자 하나와 요청 개수를 로그에 남깁니다.
- 동일 예외를 여러 계층에서 중복으로 기록하지 않습니다.

## Test

- 테스트 메서드명과 `@DisplayName`은 한글 시나리오 형식을 사용합니다.
- 테스트명은 상황, 행위, 기대 결과를 표현합니다.
- `Repository를 호출한다`, `함수를 호출하지 않는다` 같은 구현 표현은 테스트명에 사용하지 않습니다.
- 정상적인 도메인 경로로 만들 수 없는 상태는 과도하게 테스트하지 않습니다.
- Controller 테스트는 HTTP 계약, 인증, validation과 응답을 검증합니다.
- UseCase와 Service 테스트는 업무 흐름, 검증, 삭제 정책과 트랜잭션 결과를 검증합니다.
- Repository 테스트는 파생 메서드로 보장하기 어려운 custom query와 soft delete 조건을 검증합니다.

```java
@DisplayName("유효하지 않은 활동이 포함되면 모든 구성원을 유지한다")
void 유효하지_않은_활동이_포함되면_모든_구성원을_유지한다()
```

## Comments And Format

- 서비스 함수와 핵심 흐름에는 필요한 경우 짧은 한글 주석을 작성합니다.
- 주석은 `~조회`, `~검증`, `~처리`, `~유지` 형태의 요약형으로 작성합니다.
- 코드만 읽어도 명확한 내용은 주석으로 반복하지 않습니다.
- 불필요한 개행을 넣지 않고, 한 줄이 지나치게 길거나 흐름 구분이 필요할 때만 개행합니다.

## Review Checklist

- Controller가 HTTP 계층의 책임만 담당하는가?
- 신규 또는 수정된 Controller의 `ApiSpec` 적용이 규칙에 맞는가?
- UseCase와 Service의 책임과 트랜잭션 경계가 명확한가?
- 메서드 동사가 행위와 조회 성격을 정확히 표현하는가?
- Request와 Response가 Entity를 직접 노출하지 않는가?
- Request DTO가 Entity를 직접 생성하지 않는가?
- 형식 검증과 비즈니스 검증의 위치가 적절한가?
- 다건 작업의 컬렉션 타입과 실패 정책이 일치하는가?
- 테스트명이 도메인 시나리오와 기대 결과를 표현하는가?
