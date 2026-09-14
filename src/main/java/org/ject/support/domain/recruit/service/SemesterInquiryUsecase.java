package org.ject.support.domain.recruit.service;

import java.util.List;

import org.ject.support.domain.recruit.domain.SemesterEvent;
import org.ject.support.domain.recruit.dto.SemesterResponse;
import org.ject.support.domain.recruit.dto.SemesterResponses;

public interface SemesterInquiryUsecase {
    /**
     * 모든 기수 목록을 조회합니다.
     *
     * @return 기수 목록
     */
    SemesterResponses getAllSemesters();

    SemesterResponse getSemester(Long id);

	List<SemesterEvent> getSemesterEvents(Long semesterId);

	void validateSemesterEvent(Long semesterId, Long semesterEventId);

    Long getSemesterIdByRecruitId(Long recruitId);
}
