package org.ject.support.admin.semester.service;

import static org.ject.support.domain.recruit.exception.SemesterErrorCode.NOT_FOUND_SEMESTER;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.semester.dto.SemesterEventsResponse;
import org.ject.support.domain.recruit.domain.SemesterEvent;
import org.ject.support.domain.recruit.domain.SemesterEventType;
import org.ject.support.domain.recruit.exception.SemesterException;
import org.ject.support.domain.recruit.repository.SemesterEventRepository;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminSemesterEventService {

    private final SemesterRepository semesterRepository;
    private final SemesterEventRepository semesterEventRepository;

    // 선택한 기수의 행사 목록 조회
    @Transactional(readOnly = true)
    public SemesterEventsResponse getEvents(Long semesterId, SemesterEventType type) {
        validateSemester(semesterId);

        List<SemesterEvent> events =
                semesterEventRepository.findAllBySemesterIdAndTypeOrderByIdAsc(semesterId, type);
        return SemesterEventsResponse.from(type, events);
    }

    // 존재하는 기수인지 검증
    private void validateSemester(Long semesterId) {
        if (!semesterRepository.existsById(semesterId)) {
            throw new SemesterException(NOT_FOUND_SEMESTER);
        }
    }

}
