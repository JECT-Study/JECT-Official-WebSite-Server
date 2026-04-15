# Dynamic Mail MVP Plan

## Goal
- JECT의 기존 고정형 메일 발송 방식을 시나리오 기반 동적 메일 시스템으로 전환한다.
- 첫 구현 시나리오는 `일반 구성원 - 불합격 통지`로 한정한다.
- 이번 MVP 범위는 시나리오 CRUD, 변수 사전 조회, 미리보기, 테스트 발송, 실제 발송, 발송 이력 조회까지로 제한한다.

## Why
- 운영진이 DB 구조를 몰라도 안전하게 메일 템플릿을 조립할 수 있어야 한다.
- AWS SES 네이티브 템플릿 의존도를 줄이고, 서비스 내부 도메인 규칙에 맞는 템플릿 제어가 필요하다.
- 대량 발송 이전에 문법 오류, 누락 변수, 잘못된 바인딩을 사전에 차단해야 한다.
- 실제 발송 결과를 남겨야 운영과 장애 대응이 가능하다.

## Fixed Decisions
- `MailScenario`는 `scenarioCode`, `subjectTemplate`, `bodyTemplate`로 분리한다.
- 템플릿 문법은 MVP에서 `${}` 치환 방식을 유지한다.
- 동적 메일은 서버에서 렌더링한 뒤 SES로 직접 발송한다.
- MVP부터 발송 이력을 저장한다.

## First Scenario
- 시나리오명: `일반 구성원 - 불합격 통지`
- 시나리오 코드: `MEMBER_REJECT_NOTICE`
- 공통 변수:
  - `모집명`
  - `모집 알림 신청 URL`
- 개인 변수:
  - `이름`
- 예상 입력:
  - `scenarioId`
  - `receiverIds`
  - `commonVariables`
- 예상 결과:
  - 미리보기 subject/body 확인 가능
  - 테스트 발송 가능
  - 실제 발송 가능
  - 발송 이력 조회 가능

## Scope
### Included
- 메일 시나리오 저장 구조 정비
- 메일 시나리오 CRUD
- 시나리오별 변수 목록 조회
- 템플릿 문법 및 변수 검증
- 미리보기 API
- 테스트 발송 API
- 실제 발송 API
- 발송 작업 및 대상 이력 저장
- 발송 이력 조회 API

### Excluded
- 인증 메일, PIN 재설정 메일의 레거시 제거
- 대규모 워커 분리
- 재시도 스케줄러 자동화
- 템플릿 버전 관리
- 첨부파일 발송

## Current Baseline
- `domain/mail`에 시나리오/변수 도메인과 기본 CRUD가 이미 존재한다.
- `external/email`에 SES 발송 어댑터와 rate limiter가 이미 존재한다.
- 현재 템플릿 엔진은 `${}` 문자열 치환 수준이며, 운영용 검증은 부족하다.
- 현재 SES 발송은 AWS 네이티브 템플릿 enum 중심이라 동적 HTML 직접 발송 경로가 없다.
- `mail_scenario_variables`는 JPA 매핑과 Flyway 컬럼명이 다르므로 선행 수정이 필요하다.

## Tasks
- [ ] 1. 메일 스키마 정규화
  - `mail_scenario` 구조를 `scenario_code`, `subject_template`, `body_template`, `active` 중심으로 재정의
  - `mail_scenario_variables` 컬럼명 mismatch 수정
  - `mail_dispatch_job`, `mail_dispatch_target` 테이블 추가

- [ ] 2. MailScenario 엔티티와 DTO 리팩터링
  - `MailScenario`, `MailScenarioRequest`, `MailScenarioResponse` 변경
  - 시나리오 코드와 템플릿 본문 역할 분리
  - 첫 시나리오 변수에 `RECRUIT_NAME` 반영

- [ ] 3. 템플릿 검증 로직 추가
  - 허용되지 않은 placeholder 검출
  - 필수 공통 변수 누락 검출
  - preview와 dispatch에서 공통 검증 사용

- [ ] 4. 미리보기 API 구현
  - 시나리오 + 공통 변수 + mock 개인 변수로 렌더링
  - subject/body/html 반환

- [ ] 5. 테스트 발송 API 구현
  - 단건 렌더링 후 SES로 실제 발송
  - 실패 시 명확한 에러 반환

- [ ] 6. SES 직접 렌더링 발송 경로 추가
  - rendered subject/body/html 기반 발송 메서드 추가
  - 기존 `EmailTemplate` enum 기반 발송은 유지

- [ ] 7. 첫 시나리오 전용 context resolver 구현
  - `NAME`은 DB 조회
  - `RECRUIT_NAME`, `RECRUIT_ALERT_APPLY_URL`은 공통 변수 사용

- [ ] 8. 발송 작업/대상 이력 저장 구현
  - job 단위 상태 관리
  - receiver 단위 상태 및 실패 사유 저장

- [ ] 9. 실제 발송 API 구현
  - job 생성
  - receiver별 context 조립
  - 렌더링 및 SES 발송
  - target 상태 기록

- [ ] 10. 발송 이력 조회 API 구현
  - job 목록 조회
  - job 상세 조회
  - 실패 대상 조회

- [ ] 11. 메일 도메인 에러코드 보강
  - 템플릿 문법 오류
  - 미지원 변수
  - 누락 변수
  - 발송 작업 조회 실패

- [ ] 12. 테스트 정비
  - 단위 테스트
  - MVC 테스트
  - 서비스 테스트
  - 첫 시나리오 기준 통합 흐름 검증

## Recommended Order
1. 메일 스키마 정규화
2. MailScenario 엔티티와 DTO 리팩터링
3. 템플릿 검증 로직 추가
4. SES 직접 렌더링 발송 경로 추가
5. 미리보기 API 구현
6. 테스트 발송 API 구현
7. 첫 시나리오 전용 context resolver 구현
8. 발송 작업/대상 이력 저장 구현
9. 실제 발송 API 구현
10. 발송 이력 조회 API 구현
11. 메일 도메인 에러코드 보강
12. 테스트 정비

## Risks
- `mail_scenario_variables` 컬럼명 불일치로 인한 런타임 오류 가능성
- 기존 SES 네이티브 템플릿 경로와 신규 동적 메일 경로의 공존 복잡도
- `code` 필드 의미가 모호한 상태에서 기능 확장을 진행하면 설계 혼선 발생
- 개인화 데이터 조회 로직이 시나리오별로 분산될 가능성

## Done Definition
- `일반 구성원 - 불합격 통지` 시나리오를 조회하고 수정할 수 있다.
- 공통 변수와 개인 변수를 분리해 조회할 수 있다.
- 미리보기 결과를 관리자 화면에서 확인할 수 있다.
- 테스트 발송을 1건 수행할 수 있다.
- 실제 발송 요청을 수행할 수 있다.
- 발송 작업과 수신자별 결과를 조회할 수 있다.

## Working Notes
- 구현 중 범위가 늘어나면 이 문서의 `Scope`와 `Tasks`를 먼저 수정하고 코드를 변경한다.
- 첫 시나리오가 안정화되기 전까지 다른 시나리오 확장은 하지 않는다.
- 레거시 인증 메일 플로우는 이번 MVP의 변경 범위에서 제외한다.
