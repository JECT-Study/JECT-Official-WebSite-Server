# Git Conventions Draft

이 문서는 현재 저장소의 브랜치와 커밋 경향을 기준으로 작성한 Git 컨벤션 초안입니다.
PR 리뷰를 통해 팀원들이 동의하는 항목은 유지하고, 실제 운영 방식과 맞지 않는 항목은 수정하거나 제거합니다.

## Scope

- 우선 적용 범위는 JECT 공식 웹사이트 서버 저장소입니다.
- 현재 로컬에 존재하는 branch/log 정보를 기준으로 작성했습니다.
- 원격 브랜치 목록이 최신 fetch 상태가 아닐 수 있으므로, GitHub 설정과 다른 부분은 PR에서 논의합니다.

## Observed Patterns

- 기본 통합 브랜치는 현재 `origin/HEAD -> origin/dev`로 관찰됩니다.
- 배포 흐름은 문서 기준으로 `dev`는 개발 서버, `main`은 운영 배포에 연결되어 있습니다.
- 작업 브랜치는 주로 다음 prefix를 사용합니다.
  - `feat/`
  - `fix/`
  - `refactor/`
  - `chore/`
  - `hotfix/`
  - `style/`
  - `test/`
- 이슈 번호 포함 방식은 혼재되어 있습니다.
  - 예: `feat/556-member-separation`
  - 예: `feat/#617-관리자-계정-일괄-비활성화-api-추가`
  - 예: `feature/issue-450-github-templates`
- 최근 커밋 메시지는 `[FEAT] ... (#618)`, `feat: ... (#589)`, `feat/#617 ... (#620)`처럼 형식이 혼재되어 있습니다.

## Branch Naming

### Default Format

- 작업 브랜치는 `{type}/{issue-number}-{summary}` 형식을 기본으로 사용합니다.
  - 예: `feat/617-admin-account-bulk-deactivate`
  - 예: `fix/601-add-activity-status-filter`
  - 예: `style/621-redefine-code-conventions`
- 이슈 번호에는 `#`을 붙이지 않습니다.
  - 권장: `feat/617-admin-account-bulk-deactivate`
  - 지양: `feat/#617-admin-account-bulk-deactivate`
- summary는 kebab-case 영어를 기본으로 사용합니다.
- 한글 summary는 이슈/도메인 의미 전달이 더 명확한 경우에만 논의 후 사용합니다.

### Branch Types

- `feat`: 신규 기능 또는 API 추가
- `fix`: 버그 수정
- `refactor`: 동작 변경 없이 구조 개선
- `style`: 코드/문서 컨벤션, 포맷, 네이밍 정리 등 동작 변경이 없는 변경
- `chore`: 빌드, 설정, 배포, 운영성 작업
- `test`: 테스트 추가 또는 테스트 구조 개선
- `hotfix`: 운영 긴급 수정

### Protected Branches

- `main`과 `dev`에는 직접 커밋하지 않습니다.
- 작업은 항상 작업 브랜치에서 진행하고 PR로 병합합니다.
- 공유 브랜치에서는 force push를 금지합니다.

## Commit Messages

### Default Format

- 커밋 메시지는 `{type}: {summary} (#{issue-or-pr})` 형식을 우선합니다.
  - 예: `feat: 관리자 계정 일괄 비활성화 API 추가 (#620)`
  - 예: `refactor: 지원서 제출 낙관적 락 적용 (#524)`
- scope가 필요하면 `{type}({scope}): {summary}` 형식을 사용합니다.
  - 예: `feat(mail): 관리자 메일 템플릿 관리 기능 추가 (#523)`
- `[FEAT]`, `[fix]`, `feat/#617` 같은 prefix는 신규 커밋에서 사용하지 않을지 논의합니다.

### Commit Types

- `feat`: 기능 추가
- `fix`: 버그 수정
- `refactor`: 리팩터링
- `style`: 포맷, 컨벤션, 문서 스타일
- `chore`: 설정, 빌드, 배포, 잡무성 변경
- `test`: 테스트 변경
- `docs`: 문서 변경

### Commit Scope

- 하나의 커밋은 하나의 논리적 변경 단위를 담습니다.
- migration, API, service 로직, 테스트 변경이 강하게 결합되어 있으면 같은 커밋에 둘 수 있습니다.
- 무관한 변경은 별도 커밋 또는 별도 PR로 분리합니다.

## Pull Request

- PR base branch는 현재 관찰 기준으로 `dev`를 기본으로 사용합니다.
- PR 제목과 본문은 한국어로 작성합니다.
- PR 본문은 `.github/PULL_REQUEST_TEMPLATE.md`를 따릅니다.
- 관련 이슈가 있으면 `close #<issue-number>` 또는 `relates to #<issue-number>`를 명시합니다.
- PR에는 변경 목적, 주요 변경 사항, 검증 결과를 포함합니다.
- migration, 배포 설정, API contract 변경이 있으면 리뷰 요구사항에 명시합니다.

## Merge Rules

- 작업 브랜치에서 `dev`로 PR을 생성합니다.
- PR 승인 전에는 `dev`에 직접 merge하지 않습니다.
- 병합 전 다음을 확인합니다.
  - CI 통과
  - 리뷰 승인
  - conflict 없음
  - migration 순서 충돌 없음
  - PR 본문에 검증 결과 작성
- conflict 해결용 브랜치는 `merge/{summary}` 형식을 사용할 수 있습니다.
  - 예: `merge/resolve-conflict`
- `dev`에서 충분히 검증된 변경만 `main`으로 병합합니다.
- `main` 병합은 운영 배포와 연결되므로 별도 승인 기준을 둡니다.
- 운영 긴급 수정은 `hotfix/{summary}` 브랜치에서 진행하고, `main` 반영 후 `dev`에도 반드시 반영합니다.

## Do Not

- `main`, `dev`에 직접 push하지 않습니다.
- 공유된 커밋을 rebase, amend, force push로 변경하지 않습니다.
- 이미 공유된 Flyway migration 파일을 수정하지 않습니다.
- PR 하나에 서로 무관한 기능과 리팩터링을 함께 넣지 않습니다.
- conflict 해결 커밋에 기능 변경을 섞지 않습니다.

## Review Checklist

- 브랜치명이 `{type}/{issue-number}-{summary}` 형식에 가까운가?
- PR base branch가 올바른가?
- 커밋 메시지가 type과 요약을 포함하는가?
- PR 본문에 이슈, 변경 내용, 검증 결과가 포함되어 있는가?
- migration 충돌 가능성이 확인되었는가?
- `main` 또는 `dev`에 직접 반영하지 않았는가?
- hotfix가 `main`과 `dev` 양쪽에 반영될 계획이 있는가?

## Discussion Items

- PR base branch를 `dev`로 통일할지, 기존 workflow 문서의 `develop` 표기를 수정할지 논의합니다.
- 브랜치명에서 이슈 번호에 `#`을 허용할지 금지할지 논의합니다.
- 브랜치 summary를 영어 kebab-case로 통일할지, 한글을 허용할지 논의합니다.
- 커밋 메시지 prefix를 `feat:` 형식으로 통일할지, `[FEAT]` 형식을 허용할지 논의합니다.
- squash merge, merge commit, rebase merge 중 어떤 방식을 기본으로 할지 논의합니다.
- PR 승인 수와 self-merge 허용 여부를 논의합니다.
- `dev`에서 `main`으로 병합하는 주기와 책임자를 논의합니다.
- hotfix 이후 `dev` 반영 책임과 순서를 논의합니다.
