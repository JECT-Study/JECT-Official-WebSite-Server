---
name: github-pr
description: 현재 작업 중인 변경사항을 Git 커밋하고 원격 저장소에 푸시한 뒤, "Pull Request(PR)"를 생성합니다. "작업 끝났어 PR 올려줘", "리뷰 요청할게" 등의 상황에서 사용하십시오.
---

# Create GitHub PR

Creates a Pull Request for the current work.

## Instructions

1.  **Branch Check**
    -   If current branch is `main` or `develop`:
        -   Ask user for branch name (e.g., `feature/...`).
        -   `git checkout -b <new-branch>`
    -   Else: Continue.

2.  **Push Changes**
    -   Ensure all changes are committed.
    -   `git push origin HEAD`

3.  **Draft PR Body**
    -   **Format**: Markdown (Korean).
    -   **Content**: Follow `.github/PULL_REQUEST_TEMPLATE.md`.
        -   `## #️⃣연관된 이슈`: e.g. `close #ID`.
        -   `## 📝 작업 내용`: Summary of changes (Why & What).
        -   `## 🙏 리뷰 요구사항 (선택)`: Specific things to review.
    -   **Show draft to user**.

4.  **Create PR**
    -   `gh pr create --base develop --title "제목" --body "본문"`