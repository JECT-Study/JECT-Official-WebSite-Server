# Git Conventions

## Branch

- PR base는 `dev`를 기본으로 합니다.
- 선행 작업이 아직 병합되지 않아 해당 변경에 의존하는 경우에는 선행 작업 브랜치를 임시 base로 사용할 수 있습니다.
- 브랜치명은 `{type}/{issue-number}-{english-kebab-case}` 형식을 사용합니다.
- 이슈 번호에 `#`을 붙이지 않고, 한글 브랜치명은 사용하지 않습니다.
- 기능 개발은 `feature`가 아니라 `feat`를 사용합니다.

```text
feat/621-admin-account-bulk-deactivate
fix/601-apply-soft-delete
refactor/576-member-activity
```

허용 type은 다음과 같습니다.

```text
feat
fix
refactor
test
docs
chore
style
hotfix
merge
```

## Commit

- 커밋 메시지는 `type: 작업 내용` 형식을 사용합니다.
- type은 영어 소문자, 작업 내용은 한국어로 작성합니다.
- 모든 커밋에 이슈 번호를 강제하지 않습니다.
- `[FEAT]`, `[feat]`, `feat/#617` 형식은 사용하지 않습니다.
- 기능 개발은 `feature`가 아니라 `feat`를 사용합니다.
- 커밋은 리뷰 가능한 논리적 책임 단위로 분리합니다.

```text
feat: 메이커스팀 구성원 삭제 API 구현
test: 메이커스팀 구성원 삭제 테스트 추가
db: Apply 재지원 제약 수정
docs: 코드 컨벤션 문서 수정
```

허용 type과 작업 범위는 다음과 같습니다.

- `feat`: 새로운 기능이나 사용자 동작 추가
- `fix`: 의도와 다르게 동작하는 기능이나 오류 수정
- `refactor`: 외부 동작 변경 없는 코드 구조 개선
- `test`: 테스트 추가, 수정 또는 테스트 구조 개선
- `docs`: 문서만 추가하거나 수정
- `db`: schema, migration 또는 데이터 보정 변경
- `chore`: 의존성, 빌드, 개발 설정 등 제품 동작과 직접 관련 없는 유지보수
- `merge`: 브랜치 병합과 충돌 해결
- `deploy`: 배포 환경, 배포 설정 또는 배포 자동화 변경
- `release`: 릴리스 버전과 배포 단위 확정

## Pull Request

- PR base는 `dev`를 기본으로 사용합니다.
- 선행 PR에 의존하는 경우 선행 작업 브랜치를 임시 base로 사용할 수 있습니다.
- 임시 base를 사용하면 PR 본문에 선행 PR 링크와 의존 이유를 작성합니다.
- 선행 PR이 병합되면 base를 `dev`로 변경하고 최신 `dev`를 반영한 뒤 선행 작업의 커밋과 변경이 중복되지 않는지 확인합니다.
- 제목과 본문은 한국어로 작성합니다.
- 관련 이슈를 반드시 연결합니다.
- 주요 변경 내용과 검증 결과를 작성합니다.
- migration, API contract, 배포 설정 변경은 별도로 표시합니다.
- 이슈와 무관한 변경은 별도 PR로 분리합니다.

## Review And Merge

- CI 통과를 merge 필수 조건으로 합니다.
- 최소 한 명의 승인을 받습니다.
- 승인한 리뷰어가 있으면 작성자의 self-merge를 허용합니다.
- unresolved review thread가 있으면 merge하지 않습니다.
- squash merge를 사용합니다.
- 최종 squash commit 메시지는 PR 제목을 기준으로 정리합니다.
- `main`, `dev`에는 직접 push하지 않습니다.

## Hotfix

- 운영 긴급 수정은 `hotfix/{issue-number}-{english-kebab-case}` 브랜치에서 진행합니다.
- hotfix를 `main`에 반영한 담당자가 동일 변경을 `dev`에도 반영합니다.
- `main` 반영 직후 `dev` 반영 PR을 생성하고 두 PR을 서로 연결합니다.

## Flyway

- 공유 브랜치에 반영된 migration 파일은 수정하지 않습니다.
- 변경이 필요하면 새로운 migration을 추가합니다.
- 파일명은 `V{number}__{description}.sql` 형식을 사용합니다.
- migration 번호가 충돌하면 나중에 병합되는 PR이 번호를 변경합니다.
- merge 직전에 최신 `dev`를 기준으로 migration 번호 충돌을 확인합니다.
- migration이 포함된 PR은 본문에 명시합니다.

## Review Checklist

- PR base가 `dev`이거나, 임시 base 사용 이유와 선행 PR이 명시되어 있는가?
- 선행 PR이 병합된 의존 PR은 base를 `dev`로 변경하고 중복 변경을 정리했는가?
- 브랜치명과 커밋 메시지가 정해진 형식에 맞는가?
- PR에 관련 이슈, 주요 변경 내용, 검증 결과가 있는가?
- migration, API contract, 배포 설정 변경을 별도로 표시했는가?
- CI와 승인 조건을 충족하고 unresolved thread가 없는가?
- Flyway migration 번호가 최신 `dev`와 충돌하지 않는가?
- hotfix의 `main`과 `dev` 반영 계획이 연결되어 있는가?
