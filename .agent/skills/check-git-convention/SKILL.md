---
name: check-git-convention
description: Git 변경 흐름을 프로젝트 Git 컨벤션과 비교해 브랜치명, 전체 커밋, PR base, merge 규칙, hotfix와 Flyway 충돌을 검토합니다.
---

# Git 컨벤션 검토

## 기준

`docs/GIT_CONVENTIONS.md`를 최우선 기준으로 사용합니다.

## 검토 절차

```bash
git fetch origin dev
git branch --show-current
gh pr view --json baseRefName,headRefName,body
git log --oneline origin/dev..HEAD
git diff --name-only origin/dev...HEAD
git diff --name-only
git diff --name-only --cached
git ls-files --others --exclude-standard
```

다음 항목을 확인합니다.

1. PR base가 `dev`인지 확인합니다. 선행 작업 브랜치를 base로 사용하면 PR 본문에 선행 PR과 의존 이유가 있는지 확인합니다.
2. 브랜치명이 `{type}/{issue-number}-{english-kebab-case}` 형식인지 확인합니다.
3. PR에 포함된 전체 커밋이 `type: 작업 내용` 형식인지 확인합니다.
4. 관련 이슈, 주요 변경 내용, 검증 결과가 PR에 포함되는지 확인합니다.
5. CI, 최소 한 명 승인, unresolved thread, squash merge 조건을 확인합니다.
6. hotfix가 `main`과 `dev`에 순서대로 반영되는지 확인합니다.
7. PR 전체와 로컬 변경에 포함된 Flyway migration을 최신 `dev`와 비교합니다.
8. 선행 PR이 병합된 의존 PR은 base가 `dev`로 변경되었고 선행 작업의 커밋과 변경이 중복되지 않는지 확인합니다.

## 결과 작성

- 문제를 우선순위가 높은 순서대로 작성합니다.
- 각 문제에 근거와 수정 방향을 포함합니다.
- 문제가 없으면 확인한 범위와 아직 확인하지 못한 외부 상태를 간단히 작성합니다.
