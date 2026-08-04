# Code Conventions Draft

이 문서는 `apply` 및 `admin/apply` 패키지를 기준으로 작성한 코드 컨벤션 초안입니다.
현재 코드에서 반복되는 패턴을 먼저 문서화하고, PR 리뷰를 통해 동의하는 항목은 유지하며 맞지 않는 항목은 수정하거나 제거합니다.

## Scope

- 우선 적용 범위는 `org.ject.support.domain.apply`와 `org.ject.support.admin.apply`입니다.
- 다른 패키지에 바로 강제하지 않습니다.
- 신규 코드 작성과 리뷰에서 논의 기준으로 사용합니다.
- pre-hook, Checkstyle, Spotless 같은 자동화는 컨벤션 합의 후 별도 PR에서 검토합니다.

## Package Structure

- 사용자 기능은 `domain/{domain-name}` 하위에 둡니다.
  - 예: `domain/apply/controller`, `domain/apply/service`, `domain/apply/dto`, `domain/apply/domain`, `domain/apply/repository`, `domain/apply/exception`
- 관리자 기능은 `admin/{domain-name}` 하위에 둡니다.
  - 예: `admin/apply/controller`, `admin/apply/service`, `admin/apply/dto`, `admin/apply/repository`
- 공통 응답, 예외, 보안, 유틸은 `common` 하위에 둡니다.
- 외부 시스템 연동은 `external` 하위에 둡니다.

## Naming

### Common

- 이름은 계층의 책임과 도메인 의미가 함께 드러나게 작성합니다.
- 포괄적인 이름은 피하고, 실제 행위와 대상이 드러나는 이름을 우선합니다.
  - 피할 이름: `process`, `handle`, `manage`, `data`, `info`, `doSomething`
- 같은 도메인 안에서는 같은 의미에 같은 동사를 사용합니다.
  - 예: 수정 의미로 `edit`과 `update`를 섞어 쓰지 않습니다.

### Controller

- Controller 메서드는 API 사용자가 요청하는 행위를 기준으로 이름을 짓습니다.
- HTTP method 이름을 그대로 메서드명에 쓰지 않습니다.
  - 피할 이름: `getApply`, `postApply`, `patchApply`, `processApply`
- 단건 조회는 `find{Domain}`을 사용합니다.
  - 예: `findApply`, `findTempApplicationForm`
- 목록 조회는 기본적으로 `find{Domains}`를 사용합니다.
  - 예: `findApplies`
- 검색 조건이 많고 필터링/페이징 의미가 강하면 `search{Domains}`를 논의합니다.
  - 예: `searchApplies`
- 상태 또는 가능 여부 확인은 `check{Domain}{State}`를 사용합니다.
  - 예: `checkApplyStatus`
- 생성/저장은 API 사용자의 행위가 저장이면 `save{Domain}`, 제출이면 `submit{Domain}`처럼 도메인 행위를 사용합니다.
  - 예: `saveProfile`, `saveApplicationTemporarily`, `submitApplication`
- 수정은 사용자 입력 기반 편집 의미가 강하면 `edit{Domain}`, 상태/시스템 값 갱신 의미가 강하면 `update{Domain}`을 사용합니다.
  - 예: `editSubmittedApply`
- 단건 삭제는 `delete{Domain}`을 사용합니다.
  - 예: `deleteApply`
- 다건 삭제/수정은 `bulkDelete{Domain}`보다 `delete{Domains}`, `update{Domains}`처럼 복수 목적어를 기본으로 사용합니다.
  - 예: `deleteSubmittedApplies`
  - 단, bulk 작업임을 API 이름에서 반드시 드러내야 한다면 `bulkDelete{Domains}`를 논의합니다.

### Service

- Service 메서드는 내부 유스케이스와 비즈니스 행위를 기준으로 이름을 짓습니다.
- Controller와 이름이 같아도 되지만, Service에서는 도메인 상태 변화나 비즈니스 의미를 더 우선합니다.
- 단건 조회는 `find{Domain}`을 사용합니다.
  - 예: `findApply`
- 목록/페이지 조회는 `find{Domains}`를 기본으로 사용하고, 검색 조건 중심이면 `search{Domains}`를 논의합니다.
  - 예: `findApplies`
- 상태 확인은 `check{Domain}{State}`를 사용합니다.
  - 예: `checkApplyStatus`
- 신규 객체 생성은 `create{Domain}`을 사용합니다.
- 생성/수정 여부보다 저장 행위가 중요한 경우 `save{Domain}`을 사용합니다.
  - 예: `saveProfile`
- 도메인 등록 행위가 명확하면 `register{Domain}`을 사용합니다.
- 사용자 입력으로 기존 내용을 편집하는 경우 `edit{Domain}`을 사용합니다.
- 영속 상태, 시스템 상태, 계산된 값을 갱신하는 경우 `update{Domain}`을 사용합니다.
  - 예: Controller `editSubmittedApply` -> Service `updateSubmittedApply`
- 단건 삭제는 `delete{Domain}`, 다건 삭제는 `delete{Domains}`를 사용합니다.
  - 예: `deleteApply`, `deleteApplies`
- 다건 수정은 `update{Domains}`를 기본으로 사용하고, bulk 의미를 강조해야 하면 `bulkUpdate{Domains}`를 논의합니다.
- 상태 전이는 `submit`, `reject`, `pass`, `approve`, `cancel`처럼 도메인 행위 동사를 우선합니다.
- private helper 메서드는 구현 세부보다 의도가 보이게 작성합니다.
  - 예: `validateQuestions`, `getActiveRecruit`, `createApplicationForm`, `getNewPortfolios`
  - 단순 조회인데 실패 시 예외를 던지면 `getXxx`보다 `findXxxOrThrow` 같은 이름을 논의합니다.

### DTO

- DTO 이름은 `대상 + 행위/상태 + 역할` 또는 `행위 + 대상 + 역할` 중 더 자연스러운 형태를 선택합니다.
- Request DTO는 API 요청 행위가 명확하면 동사를 앞에 둡니다.
  - 생성: `Create{Domain}Request`
  - 수정: `Update{Domain}Request` 또는 `Edit{Domain}Request`
  - 제출/승인/거절 같은 도메인 행위: `Submit{Domain}Request`, `Pass{Domain}Request`, `Reject{Domain}Request`
- 대상의 상태가 핵심이면 상태/대상을 앞에 둘 수 있습니다.
  - 예: `SubmittedApplyEditRequest`, `SubmittedApplyBulkDeleteRequest`
- 도메인에서 이미 자연스럽게 쓰이는 표현은 도메인 중심 이름을 허용합니다.
  - 예: `ApplyProfileRequest`, `ApplyTemporaryRequest`
- 검색 조건 DTO는 `{Domain}SearchCondition`을 사용합니다.
  - 예: `AdminApplySearchCondition`
- 다건 작업 Request는 `{Domain}BulkDeleteRequest`, `{Domain}BulkUpdateRequest`처럼 bulk 의미를 이름에 포함합니다.
  - 예: `SubmittedApplyBulkDeleteRequest`
- Response DTO는 기본적으로 `{Domain}Response`를 사용합니다.
  - 예: `AdminApplyResponse`, `AdminApplyDetailResponse`
- 여러 항목을 담는 이름은 복수형을 기본으로 사용합니다.
  - 예: `findApplies`, `deleteApplies`, `portfolios`, `responses`
- 복수형이 어색하거나 wrapper 성격을 분명히 해야 할 때만 `List` suffix를 논의합니다.
  - 예: `ApplyListResponse`는 `ApplyResponses`보다 의미가 분명할 때만 사용합니다.
- DTO 정적 팩토리 메서드는 변환 방향과 source가 드러나게 작성합니다.
  - `from`: 다른 객체 또는 값에서 현재 타입으로 변환할 때 사용합니다. 예: `AdminApplyResponse.from(Apply apply)`
  - `of`: 이미 현재 타입의 핵심 값이 준비되어 있고 단순 조합으로 생성할 때 사용합니다.
  - `toEntity`: DTO에서 Entity로 변환할 때 사용합니다. 예: `ApplyPortfolioDto.toEntity()`

### Domain And Entity

- 도메인 메서드는 상태 변경과 상태 질의를 명확히 구분합니다.
  - 상태 변경: `submit`, `reject`, `deleteApplicationForm`, `updateStatus`
  - 상태 질의: `isTempSaved`, `isNotTempSaved`, `isSubmitted`, `isInvalidQuestionId`
- boolean 메서드는 `is`, `has`, `can`, `should`처럼 참/거짓 의미가 바로 읽히는 prefix를 사용합니다.
- 도메인 Entity 정적 팩토리 메서드는 클래스명을 반복하지 않고 생성 의미를 드러냅니다.
  - 기본 생성은 `create`를 우선 고려합니다. 예: `Apply.create(applicant, recruit)`
  - 상태가 중요한 생성은 `createJoined`, `createTempSaved`, `createSubmitted`처럼 상태나 목적을 포함합니다.
  - `Apply.createApply(...)`처럼 호출부에서 클래스명과 의미가 중복되는 이름은 신규 코드에서 피할지 논의합니다.
- 정적 팩토리 메서드가 validation, 기본 상태 설정, 연관관계 연결을 수행한다면 builder 직접 호출보다 의미 있는 이름을 우선합니다.
- 단순히 생성자 파라미터를 그대로 넘기는 수준이라면 정적 팩토리 메서드가 꼭 필요한지 검토합니다.

### Test

- 테스트 메서드명은 행위와 기대 결과가 드러나게 작성합니다.
- 현재 한글 테스트명이 사용되고 있으므로, 인코딩이 깨지지 않는 환경을 전제로 유지합니다.

## Controller

- Controller는 HTTP 요청/응답 매핑과 인증, validation, service 호출에 집중합니다.
- 비즈니스 로직은 Controller에 두지 않고 Service 또는 Domain으로 위임합니다.
- 요청 본문 검증은 `@RequestBody @Valid`를 사용합니다.
- path variable, request param은 가능한 `final`로 선언합니다.
- 관리자 API는 `/admin/{resources}` 경로를 사용합니다.
  - 예: `/admin/applies`
- Swagger 문서는 Controller 또는 ApiSpec에 작성하되, 실제 동작과 어긋나지 않게 유지합니다.

## DTO And Validation

- 외부 API request/response는 Entity를 직접 노출하지 않고 DTO로 분리합니다.
- 단순 전달 목적의 DTO는 record 사용을 우선 고려합니다.
- 필수 컬렉션은 wrapper DTO 안에서 `@NotEmpty` 등으로 검증합니다.
- `@NotNull`, `@NotBlank`, `@NotEmpty`는 값의 의미에 맞게 선택합니다.
- Entity에서 Response DTO를 만들면 `from(entity)`를 우선합니다.
- DTO에서 Entity를 만들면 `toEntity()`를 우선합니다.
- 여러 primitive 또는 collection을 묶어 Response를 만들면 `of(...)`와 `from(...)` 중 더 자연스러운 이름을 PR에서 논의합니다.

## Service

- Service는 유스케이스 단위 public 메서드를 제공합니다.
- 조회 메서드는 `@Transactional(readOnly = true)`를 사용합니다.
- 상태 변경 메서드는 `@Transactional`을 사용합니다.
- 외부 입력 검증, 엔티티 조회, 도메인 상태 변경, 저장 흐름이 한 메서드 안에서 읽히도록 유지합니다.
- 반복되는 생성/검증 로직은 private 메서드로 분리합니다.
- 예외는 해당 도메인의 `ErrorCode`와 `Exception`을 사용합니다.

## Repository

- 단순 조회/저장은 Spring Data Repository 메서드를 우선 사용합니다.
- 조건이 많거나 fetch join이 필요한 조회는 QueryDSL custom repository로 분리합니다.
- QueryDSL 조건 메서드는 `eqXxx` 형태로 작성하고, 값이 없으면 `null`을 반환해 where 절에서 제외합니다.
- 페이지 조회는 content 쿼리와 count 쿼리를 분리하고, 공통 `PageResponse` 변환을 사용합니다.
- N+1 가능성이 있는 조회는 fetch join 사용 여부를 리뷰에서 확인합니다.

## Domain And Entity

- Entity 상태 변경은 setter 대신 의미 있는 메서드로 표현합니다.
  - 예: `submit`, `updateStatus`, `deleteApplicationForm`
- 생성 로직은 builder 또는 정적 팩토리 메서드로 의도를 드러냅니다.
- 도메인 Entity의 정적 팩토리 메서드는 클래스명을 반복하지 않고 도메인 의미를 드러냅니다.
  - 예: `Apply.create(applicant, recruit)`, `Apply.createJoined(applicant, recruit)`
- 도메인 상태 검증 메서드는 boolean 의미가 명확하게 읽히도록 작성합니다.
  - 예: `isTempSaved`, `isNotTempSaved`, `isInvalidQuestionId`

## Exception

- 도메인 예외는 `XxxException`과 `XxxErrorCode` 조합을 사용합니다.
- 공통 예외는 `common.exception`의 구조를 따릅니다.
- 존재하지 않는 리소스, 잘못된 상태, 권한 문제는 각각 명확한 ErrorCode로 구분합니다.
- 예외 발생 지점에서는 원인을 추적할 수 있는 코드와 메시지를 선택합니다.

## Flyway

- 이미 공유 브랜치에 반영된 migration 파일은 수정하지 않습니다.
- 스키마 변경은 새 버전 migration으로 추가합니다.
- migration 파일명은 `V{number}__{description}.sql` 형식을 사용합니다.
- dump 기반 로컬 세팅 시 `flyway_schema_history` 포함 여부를 확인합니다.

## Test

- Controller 테스트는 request mapping, parameter binding, validation, service 호출 인자를 검증합니다.
- Service 테스트는 비즈니스 분기, 예외, 도메인 상태 변경을 검증합니다.
- Repository 테스트는 QueryDSL 조건, fetch join, paging/count 결과를 검증합니다.
- 테스트 fixture나 helper는 테스트 의도를 가리지 않는 범위에서 사용합니다.
- 신규 API, validation, migration이 포함된 PR은 관련 테스트 또는 수동 검증 기록을 함께 남깁니다.

## PR Review Checklist

- 변경 범위가 `apply` 또는 `admin/apply` 책임 안에 있는가?
- Controller에 비즈니스 로직이 들어가지 않았는가?
- Request/Response DTO가 Entity를 직접 노출하지 않는가?
- 입력값 validation 위치와 annotation이 적절한가?
- 함수명이 계층별 책임, 변환 방향, 도메인 생성 의미를 드러내는가?
- 상태 변경 메서드에 transaction이 선언되어 있는가?
- QueryDSL 조회에서 조건 누락, count 불일치, N+1 가능성이 없는가?
- 기존 migration을 수정하지 않고 새 migration을 추가했는가?
- 실패 케이스 테스트가 필요한 변경인가?

## Discussion Items

- ApiSpec 인터페이스를 모든 Controller에 일관되게 둘지 논의합니다.
- Controller method parameter에 `final`을 항상 붙일지 논의합니다.
- Controller/Service 메서드에서 `get`, `find`, `search`, `check`의 경계를 팀 기준으로 확정합니다.
- 수정 메서드에서 `update`와 `edit` 중 어떤 표현을 우선할지 논의합니다.
- 목록/복수 네이밍에서 복수형을 기본으로 할지, wrapper DTO에는 `List` suffix를 허용할지 논의합니다.
- query parameter를 Controller에 `@RequestParam`으로 모두 명시할지, 검색 조건 DTO로 묶어 `@ModelAttribute`를 사용할지 논의합니다.
- DTO 정적 팩토리 메서드에서 `from`과 `of`의 경계를 팀 기준으로 확정합니다.
- 도메인 Entity 생성 메서드에서 `createApply`처럼 클래스명을 반복하는 이름을 `create` 또는 상태 기반 이름으로 바꿀지 논의합니다.
- 테스트 메서드명을 한글로 유지할지, 영어 또는 `given_when_then` 형식으로 바꿀지 논의합니다.
- bulk API request는 항상 wrapper DTO를 사용할지 논의합니다.
- validation을 Controller DTO에 둘지, Service 내부 검증을 병행할지 기준을 더 구체화합니다.
- pre-hook으로 자동화할 항목과 리뷰로 남길 항목을 분리합니다.



