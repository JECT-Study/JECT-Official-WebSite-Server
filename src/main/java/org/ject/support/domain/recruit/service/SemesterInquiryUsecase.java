package org.ject.support.domain.recruit.service;

import java.util.List;
import org.ject.support.domain.recruit.dto.SemesterListResponse;

public interface SemesterInquiryUsecase {
    /**
     * 모든 기수 목록을 조회합니다.
     * @return 기수 목록
     */
    List<SemesterListResponse> getAllSemesters();
}
