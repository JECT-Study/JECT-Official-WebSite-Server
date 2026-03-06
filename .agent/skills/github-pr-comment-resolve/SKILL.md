---
name: github-pr-comment-resolve
description: PR 리뷰에 달린 코멘트들을 조회하고, 수정 계획을 세우거나 해결(Resolve) 처리를 돕습니다. "리뷰 반영할게", "PR 코멘트 확인해줘" 등의 상황에서 사용하십시오.
---

# Resolve PR Comments

Systematically address code review comments.

## Steps

1.  **Fetch Comments**
    -   Get PR number: `gh pr view --json number`
    -   Get unresolved comments using GraphQL:
        ```bash
        gh api graphql -F owner=':owner' -F name=':repo' -F pr=<PR_NUM> -f query='
        query($owner: String!, $name: String!, $pr: Int!) {
          repository(owner: $owner, name: $name) {
            pullRequest(number: $pr) {
              reviewThreads(last: 50) {
                nodes {
                  id
                  isResolved
                  path
                  comments(first: 5) { nodes { body } }
                }
              }
            }
          }
        }' --jq '.data.repository.pullRequest.reviewThreads.nodes[] | select(.isResolved == false)'
        ```

2.  **Analyze & Plan**
    -   List comments and propose actions (Accept/Question/Refuse).

3.  **Fix & Verify**
    -   Apply code changes.
    -   Run verification: `./scripts/check-all.sh`.

4.  **Push**
    -   `git push origin HEAD`

5.  **Reply & Resolve**
    -   Reply to comments using `gh api`.
    -   Resolve threads using GraphQL (advanced) or manual verification.

## Tip
-   Always verify before pushing.
-   Write replies in Korean.