package org.ject.support.domain.recruit.service;

import org.ject.support.domain.recruit.dto.SemesterResponses;

public interface SemesterInquiryUsecase {
    /**
     * 모든 기수 목록을 조회합니다.
     *
     * @return 기수 목록
     */
    SemesterResponses getAllSemesters();
}
