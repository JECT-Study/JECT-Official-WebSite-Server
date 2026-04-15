# Model Profiles

이 디렉터리는 이 저장소에서 사용할 수 있는 모델 배치 프로파일을 정리한다.

## Available Profiles

- `balanced`
  - 현재 기본값
- `cost-optimized`
  - 빠르고 가벼운 작업용
- `quality-first`
  - 구조 판단과 검증 안정성 우선 작업용

## Current Default

현재 `.codex/agents/*.toml` 에 반영된 기본 가이드는 `balanced` 다.

## How To Use

대부분은 프롬프트에 아래처럼만 적으면 된다.

```text
이번 작업은 quality-first 기준으로 판단해서 진행해줘.
```

```text
이건 작은 문서 정리니까 cost-optimized 느낌으로 가볍게 진행해줘.
```

명시하지 않으면 기본적으로 `balanced` 로 해석한다.
