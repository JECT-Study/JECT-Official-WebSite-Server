---
name: check-git-convention
description: Git 변경 흐름을 프로젝트 Git 컨벤션 초안과 비교해 검토합니다. 브랜치명, 커밋 메시지, PR base, 병합 규칙, hotfix/dev/main 반영 흐름, Flyway migration 충돌 가능성을 점검할 때 사용합니다.
---

# Git 컨벤션 검토

## Purpose

`docs/GIT_CONVENTIONS.md`를 기준으로 현재 작업 브랜치, 커밋, PR 흐름이 프로젝트 Git 컨벤션 초안과 일관되는지 검토합니다.

## Primary Reference

- `docs/GIT_CONVENTIONS.md`

검토 전에 반드시 위 문서를 읽습니다. 문서는 초안이므로, 명확한 위반과 논의 항목을 분리합니다.

## Checkpoints

1. **Branch name** — 작업 브랜치가 `{type}/{issue-number}-{summary}` 형식에 가까운지 확인합니다.
2. **Base branch** — PR base가 현재 기준인 `dev`인지 확인합니다.
3. **Commit message** — 커밋 메시지가 type, summary, issue/PR 번호를 포함하는지 확인합니다.
4. **Merge safety** — CI, conflict, migration 순서, hotfix 역반영 여부를 확인합니다.
5. **PR body** — 이슈, 변경 내용, 검증 결과, 리뷰 요구사항이 작성되어 있는지 확인합니다.

## Workflow

1. 현재 브랜치와 변경 상태를 확인합니다.

   ```bash
   git status --short --branch
   ```

2. 최근 커밋 메시지를 확인합니다.

   ```bash
   git log --oneline --decorate -10
   ```

3. 브랜치명이 컨벤션과 맞는지 확인합니다.

   - type이 허용 목록에 있는지 확인합니다.
   - 이슈 번호가 포함되어 있는지 확인합니다.
   - summary가 짧고 의미 있는지 확인합니다.

4. PR 생성 또는 리뷰 상황이면 base branch와 PR 본문을 확인합니다.

   ```bash
   gh pr view --json baseRefName,headRefName,title,body,mergeable,statusCheckRollup
   ```

5. migration 파일이 포함된 경우 병합 순서 충돌 가능성을 확인합니다.

   ```bash
   git diff --name-only | grep "src/main/resources/db/migration"
   ```

## Output

- 문제는 actionable findings로 먼저 작성합니다.
- 아직 팀 합의가 필요한 내용은 discussion items로 분리합니다.
- 브랜치/커밋/PR이 컨벤션과 맞으면 명확히 문제가 없다고 말합니다.

## Exceptions

1. 기존 브랜치나 이미 열린 PR은 초안 적용 전 생성되었을 수 있으므로, 즉시 수정보다 후속 정리 여부를 제안합니다.
2. hotfix는 긴급성이 우선될 수 있지만, `main` 반영 후 `dev` 반영 계획은 반드시 확인합니다.
3. GitHub 설정에서 허용하는 merge 방식은 로컬 git 정보만으로 확정할 수 없으므로 논의 항목으로 남깁니다.
