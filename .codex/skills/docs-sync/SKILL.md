---
name: docs-sync
description: Sync AGENTS and Codex-facing repository docs after structure, workflow, package, testing, response, exception, security, or cache conventions change in this repository.
---

# Docs Sync

## Language

모든 응답은 한국어로 작성한다.

## 목적

코드 변경이나 운영 규칙 변경과 Codex 문서가 어긋나지 않도록 아래 문서를 함께 맞춘다.

- `AGENTS.md`
- `.codex/README.md`
- `.codex/agents/WORKFLOW.md`
- `.codex/rules/*`
- `.codex/skills/*/SKILL.md`
- `.codex/agents/*.toml`
- `.codex/profiles/*.md`

필요 시에만 실제 기능 문서를 별도로 맞춘다.

## 갱신이 필요한 경우

- package 구조 해석 변경
- 공통 응답 / 예외 규칙 변경
- 보안 규칙 변경
- 캐시 규칙 변경
- 테스트 전략 변경
- agent / skill / workflow 정의 변경
- 저장소 정체성이나 기술 스택 설명 변경

## 규칙

- 현재 코드에 근거한 내용만 문서화한다.
- 역사적 문서를 현재 규칙으로 복구하려고 하지 않는다.
- 실제 계약 또는 규칙 변화가 없으면 no-op 로 유지한다.
- docs 단계에서는 프로덕션 코드를 수정하지 않는다.

## 최소 보고

- 수정한 문서
- no-op 여부
- 남은 문서 불일치
