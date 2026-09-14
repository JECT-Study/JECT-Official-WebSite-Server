package org.ject.support.domain.recruit.service;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.domain.SemesterEvent;
import org.ject.support.domain.recruit.dto.SemesterResponse;
import org.ject.support.domain.recruit.dto.SemesterResponses;
import org.ject.support.domain.recruit.exception.SemesterErrorCode;
import org.ject.support.domain.recruit.exception.SemesterException;
import org.ject.support.domain.recruit.exception.RecruitErrorCode;
import org.ject.support.domain.recruit.exception.RecruitException;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.ject.support.domain.recruit.repository.SemesterEventRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SemesterInquiryService implements SemesterInquiryUsecase {

    private final SemesterRepository semesterRepository;
    private final SemesterEventRepository semesterEventRepository;

    @Override
    @Cacheable(value = "semester", key = "'all'")
    @Transactional(readOnly = true)
    public SemesterResponses getAllSemesters() {
        return new SemesterResponses(semesterRepository.findAll()
                .stream()
                .map(SemesterResponse::from)
                .toList());
    }

    @Override
    public SemesterResponse getSemester(Long id) {
        Semester semester = semesterRepository.findById(id).orElseThrow(
            () -> new SemesterException(SemesterErrorCode.NOT_FOUND_SEMESTER)
        );
        return SemesterResponse.from(semester);
    }

	// 기수에 속한 행사 목록 조회
	@Override
	@Transactional(readOnly = true)
	public List<SemesterEvent> getSemesterEvents(Long semesterId) {
		return semesterEventRepository.findAllBySemesterIdOrderByIdAsc(semesterId);
	}

	// 기수에 속한 행사 존재 여부 검증
	@Override
	@Transactional(readOnly = true)
	public void validateSemesterEvent(Long semesterId, Long semesterEventId) {
		if (!semesterEventRepository.existsByIdAndSemesterId(semesterEventId, semesterId)) {
			throw new SemesterException(SemesterErrorCode.NOT_FOUND_SEMESTER_EVENT);
		}
	}

    @Override
    @Transactional(readOnly = true)
    public Long getSemesterIdByRecruitId(Long recruitId) {
        return semesterRepository.findSemesterIdByRecruitId(recruitId)
                .orElseThrow(() -> new RecruitException(RecruitErrorCode.NOT_FOUND_RECRUIT));
    }
}
