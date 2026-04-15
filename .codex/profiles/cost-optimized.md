# Cost Optimized Profile

이 프로파일은 토큰 비용과 응답 속도를 우선한다.
간단한 버그 수정, 작은 테스트 보강, 문서 정리에 적합하다.

## Recommended Model Mapping

- `requirement-extractor`: `gpt-5.4-mini` / `medium`
- `domain-spec-designer`: `gpt-5.4-mini` / `medium`
- `application-flow-designer`: `gpt-5.4-mini` / `medium`
- `architecture-rule-auditor`: `gpt-5.4-mini` / `medium`
- `implementer`: `gpt-5.4-mini` / `medium`
- `spec-conformance-auditor`: `gpt-5.4-mini` / `medium`
- `docs-sync-writer`: `gpt-5.4-mini` / `medium`

## When To Use

- 작은 문서 수정
- 테스트 1~2개만 보강하는 작업
- 단순 Querydsl 조건 수정
- S3 / SES / Security 설정 문구 정리
- 구조 영향이 거의 없는 요청

## Risks

- 구조 판단이나 경계 조건 누락 가능성이 상대적으로 높다
- 여러 레이어가 얽힌 보안 / 캐시 / 외부 연동 변경에는 덜 적합하다
