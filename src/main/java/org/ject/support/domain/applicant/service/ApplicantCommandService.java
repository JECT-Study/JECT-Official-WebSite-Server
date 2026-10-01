package org.ject.support.domain.applicant.service;

import org.ject.support.domain.applicant.dto.DeleteApplicantsRequest;

// 외부 도메인에 제공하는 지원자 변경 기능
public interface ApplicantCommandService {

    // 해당 지원자를 소프트 삭제하며 존재하지 않으면 지원자 조회 예외 발생
    void deleteApplicant(Long applicantId);

    // 요청한 지원자를 소프트 삭제하며 누락된 지원자가 있으면 예외 발생
    void deleteApplicants(DeleteApplicantsRequest request);
}
