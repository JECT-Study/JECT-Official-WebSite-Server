---
name: Refactoring
about: Refactoring for performance and architecture improvement
title: "[REFACTOR] 벌크 삭제 성능 최적화 및 계층 구조 개선"
labels: "♻️refactoring"
assignees: ''

---

## 리팩토링 개요

Soft Delete가 적용된 엔티티의 벌크 삭제 시, N개의 개별 UPDATE 쿼리가 발생하는 성능 문제를 해결하고, Admin 계층과 Domain 계층 간의 의존성 구조를 개선합니다.

## 현재 문제 상황

1.  **벌크 삭제 성능 저하**: `deleteAll(members)` 호출 시 Hibernate의 `@SQLDelete` 기능으로 인해 ID 목록 수(N)만큼의 개별 `UPDATE` 쿼리가 실행되어 심각한 DB 부하를 유발합니다. 예를 들어 100명의 회원을 삭제하면 100개의 `UPDATE` 쿼리가 발생합니다.
    - 대상 서비스: `MemberManagementService.deleteMembers()`, `AdminApplyService.deleteApplies()`
2.  **계층 간 양방향 의존성**: `AdminApplyService`가 도메인 계층의 `ApplyRepository`를 직접 사용하여, Admin 계층과 Domain 계층 간의 단방향 의존성 원칙을 위배할 가능성이 존재합니다.

## 개선 Action Plan

1.  **벌크 Soft Delete JPQL 도입**:
    - 각 Repository(`MemberRepository`, `ApplyRepository`)에 `@Modifying`과 `@Query`를 사용하여 단일 벌크 `UPDATE` 쿼리로 Soft Delete를 수행하는 `deleteAllByIdInBatch` 메서드를 추가합니다.
    - `@Modifying(clearAutomatically = true, flushAutomatically = true)` 옵션을 통해 1차 캐시를 자동으로 무효화하여 데이터 정합성을 보장합니다.

2.  **Partial Success 응답 패턴 적용**:
    - 삭제 요청 시, 존재하지 않는 ID가 포함되어도 예외를 발생시키지 않고, 실제 삭제된 ID 목록과 찾지 못한 ID 목록을 분리하여 응답(`MemberDeleteResponse`, `ApplyDeleteResponse`)합니다. 이를 통해 API 안정성을 향상시킵니다.

3.  **Admin/Domain 계층 의존성 개선**:
    - `AdminApplyService`가 `AdminApplyRepository`에만 의존하도록 변경하여 계층 간 의존성을 명확히 합니다. (단, 벌크 삭제와 같이 성능이 중요한 특정 작업에 한해 도메인 Repository 직접 사용은 허용)

## 예상 결과

- **성능 향상**: N개의 개별 `UPDATE` 쿼리가 1개의 벌크 `UPDATE` 쿼리로 최적화되어 DB 부하가 크게 감소하고 삭제 처리 속도가 대폭 향상됩니다.
- **아키텍처 개선**: Admin 계층과 Domain 계층 간의 의존성이 단방향으로 정리되어 코드의 유지보수성과 확장성이 향상됩니다.
- **API 안정성**: 부분 실패를 허용하는 응답 구조로 변경되어 클라이언트가 더 안정적으로 오류를 처리할 수 있습니다.

## 포트폴리오 어필 포인트

> "Soft Delete 기반 벌크 삭제에서 발생하는 N+1 쿼리 문제를 단일 벌크 UPDATE JPQL과 1차 캐시 관리로 해결하여 성능을 최적화했습니다. 동시에 Admin/Domain 간의 의존성 방향을 정리하여 계층형 아키텍처의 단방향 의존 원칙을 확립한 경험이 있습니다."
