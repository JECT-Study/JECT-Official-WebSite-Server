# Balanced Profile

이 프로파일은 비용과 정확도의 균형을 맞춘 기본 프로파일이다.
현재 `.codex/agents/*.toml` 은 이 프로파일을 기준으로 작성되어 있다.

## Recommended Model Mapping

- `requirement-extractor`: `gpt-5.4-mini` / `medium`
- `domain-spec-designer`: `gpt-5.3-codex` / `high`
- `application-flow-designer`: `gpt-5.3-codex` / `high`
- `architecture-rule-auditor`: `gpt-5.3-codex` / `high`
- `implementer`: `gpt-5.4` / `medium`
- `spec-conformance-auditor`: `gpt-5.3-codex` / `high`
- `docs-sync-writer`: `gpt-5.4-mini` / `medium`

## When To Use

- 일반적인 controller / service / repository 변경
- JPA / Querydsl 조회 수정
- Security / JWT 수정
- Redis cache fallback 관련 변경
- Admin API 와 사용자 API가 함께 걸린 작업

## Notes

- 현재 저장소 기준 권장 기본값
- 특별한 이유가 없으면 이 프로파일로 충분하다
