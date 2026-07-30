package org.ject.support.admin.semester.service;

import static org.ject.support.domain.recruit.exception.SemesterErrorCode.DUPLICATED_SEMESTER_EVENT_ID;
import static org.ject.support.domain.recruit.exception.SemesterErrorCode.EXCEEDED_SEMESTER_EVENT_LIMIT;
import static org.ject.support.domain.recruit.exception.SemesterErrorCode.NOT_FOUND_SEMESTER;
import static org.ject.support.domain.recruit.exception.SemesterErrorCode.NOT_FOUND_SEMESTER_EVENT;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.semester.dto.CreateSemesterEventRequest;
import org.ject.support.admin.semester.dto.EditSemesterEventsRequest;
import org.ject.support.admin.semester.dto.SemesterEventsResponse;
import org.ject.support.admin.semester.dto.UpdateSemesterEventRequest;
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

    private static final int MAX_EVENT_COUNT_PER_TYPE = 10;

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

    // 기수별 행사 추가와 수정
    @Transactional
    public void editEvents(Long semesterId, EditSemesterEventsRequest request) {
        List<CreateSemesterEventRequest> created = request.createdOrEmpty();
        List<UpdateSemesterEventRequest> updated = request.updatedOrEmpty();

        // 기수 및 타입별 생성 가능 개수 검증
        validateSemester(semesterId);
        validateEventLimit(semesterId, request.type(), created.size());

        // 수정 요청의 중복과 대상 범위 검증
        validateDuplicatedIds(updated);
        Map<Long, SemesterEvent> eventsById =
                findUpdateEvents(semesterId, request.type(), updated);

        // 신규 행사 생성 및 저장
        List<SemesterEvent> createdEvents = created
                .stream()
                .map(createRequest ->
                        SemesterEvent.create(semesterId, request.type(), createRequest.name()))
                .toList();

        semesterEventRepository.saveAll(createdEvents);

        // 기존 행사 이름 변경
        updated.forEach(updateRequest ->
                eventsById.get(updateRequest.id()).updateName(updateRequest.name()));

    }

    // 존재하는 기수인지 검증
    private void validateSemester(Long semesterId) {
        if (!semesterRepository.existsById(semesterId)) {
            throw new SemesterException(NOT_FOUND_SEMESTER);
        }
    }

    // 동일 행사에 대한 중복 수정 방지
    private void validateDuplicatedIds(List<UpdateSemesterEventRequest> updated) {
        long distinctIdCount = updated.stream()
                .map(UpdateSemesterEventRequest::id)
                .distinct()
                .count();

        if (distinctIdCount != updated.size()) {
            throw new SemesterException(DUPLICATED_SEMESTER_EVENT_ID);
        }
    }

    // 요청한 기수에 속한 수정 대상 행사 일괄 조회
    private Map<Long, SemesterEvent> findUpdateEvents(Long semesterId, SemesterEventType type, List<UpdateSemesterEventRequest> updated) {
        if (updated.isEmpty()) {
            return Map.of();
        }

        List<Long> eventIds = updated.stream()
                .map(UpdateSemesterEventRequest::id)
                .toList();
        List<SemesterEvent> events = semesterEventRepository
                .findAllByIdInAndSemesterIdAndType(eventIds, semesterId, type);

        if (events.size() != eventIds.size()) {
            throw new SemesterException(NOT_FOUND_SEMESTER_EVENT);
        }

        return events.stream()
                .collect(Collectors.toMap(SemesterEvent::getId, Function.identity()));
    }

    // 기수와 행사 타입별 최대 등록 개수 검증
    private void validateEventLimit(Long semesterId, SemesterEventType type, int createdCount) {
        if (createdCount == 0) {
            return;
        }

        long currentCount = semesterEventRepository.countBySemesterIdAndType(semesterId, type);

        if (currentCount + createdCount > MAX_EVENT_COUNT_PER_TYPE) {
            throw new SemesterException(EXCEEDED_SEMESTER_EVENT_LIMIT);
        }
    }
}
