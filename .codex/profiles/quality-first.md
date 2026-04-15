# Quality First Profile

이 프로파일은 정확도와 판단 안정성을 우선한다.
구조 영향이 크거나, 보안/캐시/외부 연동/JPA 경계가 복잡한 작업에 적합하다.

## Recommended Model Mapping

- `requirement-extractor`: `gpt-5.4` / `medium`
- `domain-spec-designer`: `gpt-5.4` / `high`
- `application-flow-designer`: `gpt-5.4` / `high`
- `architecture-rule-auditor`: `gpt-5.4` / `high`
- `implementer`: `gpt-5.4` / `high`
- `spec-conformance-auditor`: `gpt-5.4` / `high`
- `docs-sync-writer`: `gpt-5.4-mini` / `medium`

## When To Use

- `domain/admin/common/external` 경계를 건드리는 작업
- Security / JWT 흐름 수정
- Redis cache resilience 정책 수정
- AWS S3 / SES 연동 동작 변경
- Querydsl / JPA 조회와 API 계약이 함께 바뀌는 작업

## Tradeoff

- 비용 증가
- 응답 속도 저하 가능성
