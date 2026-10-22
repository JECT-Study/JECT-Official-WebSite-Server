package org.ject.support.domain.recruit.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.common.response.CursorPageResponse;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.dto.ActiveRecruitmentResponse;
import org.ject.support.domain.recruit.dto.ActiveRecruitmentResponses;
import org.ject.support.domain.recruit.dto.ApplyManagementRecruitProjection;
import org.ject.support.domain.recruit.dto.ApplyManagementRecruitResponse;
import org.ject.support.domain.recruit.dto.ApplyManagementRecruitSearchCondition;
import org.ject.support.domain.recruit.dto.ApplyManagementRecruitsResponse;
import org.ject.support.domain.recruit.dto.RecruitCanceledEvent;
import org.ject.support.domain.recruit.dto.RecruitRegisterRequest;
import org.ject.support.domain.recruit.dto.RecruitResponse;
import org.ject.support.domain.recruit.dto.RecruitUpdateRequest;
import org.ject.support.domain.recruit.dto.RecruitUpdatedEvent;
import org.ject.support.domain.recruit.dto.RecruitTypeCountResponse;
import org.ject.support.domain.recruit.exception.RecruitErrorCode;
import org.ject.support.domain.recruit.exception.RecruitException;
import org.ject.support.domain.recruit.exception.SemesterErrorCode;
import org.ject.support.domain.recruit.exception.SemesterException;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RecruitService implements RecruitUsecase {

    private static final List<RecruitType> APPLY_MANAGEMENT_RECRUIT_TYPES = List.of(
            RecruitType.SEMESTER,
            RecruitType.MAKERS,
            RecruitType.SUPPORTERS
    );

    private final RecruitRepository recruitRepository;
    private final SemesterRepository semesterRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void registerRecruits(List<RecruitRegisterRequest> requests) {
        // 1. 모집중인 기수 ID 조회
        Semester recruitingSemester = semesterRepository.findRecruitingSemester()
                .orElseThrow(() -> new SemesterException(SemesterErrorCode.NOT_FOUND_RECRUITING_SEMESTER));

        // 2. 이미 모집중인 직군인지 검증
        validateDuplicatedJobFamily(requests, recruitingSemester);

        // 3. recruit 엔티티 저장
        List<Recruit> recruits = requests.stream()
                .map(request -> request.toEntity(recruitingSemester))
                .toList();
        recruitRepository.saveAll(recruits);
    }

    // 지원서 관리용 모집공고와 제출 지원서 통계 조회
    @Override
    @Transactional(readOnly = true)
    public ApplyManagementRecruitsResponse searchApplyManagementRecruits(
            ApplyManagementRecruitSearchCondition condition
    ) {
        // 지원서 관리 화면에서 사용하는 상태와 모집 유형별 직군 조합 검증
        if (condition.status() != ApplyStatus.TEMP_SAVED
                && condition.status() != ApplyStatus.SUBMITTED) {
            throw new RecruitException(RecruitErrorCode.INVALID_APPLY_MANAGEMENT_STATUS);
        }
        if (condition.jobFamilies() != null
                && condition.jobFamilies().stream()
                .anyMatch(jobFamily -> !condition.recruitType().supports(jobFamily))) {
            throw new RecruitException(RecruitErrorCode.INVALID_JOB_FAMILY);
        }

        // 다음 페이지 판별을 위해 요청 크기보다 한 건 추가 조회
        int size = condition.getSizeOrDefault();
        List<ApplyManagementRecruitProjection> queriedRecruits =
                recruitRepository.searchApplyManagementRecruits(condition, size + 1);
        boolean hasNext = queriedRecruits.size() > size;
        List<ApplyManagementRecruitProjection> pageContent = hasNext
                ? queriedRecruits.subList(0, size)
                : queriedRecruits;
        Long nextCursor = hasNext && !pageContent.isEmpty()
                ? pageContent.get(pageContent.size() - 1).recruitId()
                : null;
        long totalCount = recruitRepository.countApplyManagementRecruits(condition);
        List<ApplyManagementRecruitResponse> recruits = pageContent.stream()
                .map(ApplyManagementRecruitResponse::from)
                .toList();

        Map<RecruitType, RecruitTypeCountResponse> countsByType =
                recruitRepository.countRecruitsByRecruitType().stream()
                        .collect(Collectors.toMap(
                                RecruitTypeCountResponse::recruitType,
                                Function.identity()));
        // 공고가 없는 모집 유형도 0건으로 응답
        List<RecruitTypeCountResponse> recruitTypeCounts = APPLY_MANAGEMENT_RECRUIT_TYPES.stream()
                .map(recruitType -> countsByType.getOrDefault(
                        recruitType,
                        new RecruitTypeCountResponse(recruitType, 0L)))
                .toList();

        CursorPageResponse<ApplyManagementRecruitResponse> page = CursorPageResponse.of(
                recruits,
                size,
                hasNext,
                nextCursor,
                totalCount);
        return ApplyManagementRecruitsResponse.of(recruitTypeCounts, page);
    }

    // 모집공고 상세정보 조회
    @Override
    @Cacheable(value = "recruit-detail", key = "#recruitId")
    @Transactional(readOnly = true)
    public RecruitResponse getRecruit(Long recruitId) {
        Recruit recruit = recruitRepository.findByIdWithSemester(recruitId)
                .orElseThrow(() -> new RecruitException(RecruitErrorCode.NOT_FOUND_RECRUIT));
        return RecruitResponse.from(recruit);
    }

    @Override
    @Transactional(readOnly = true)
    public ActiveRecruitmentResponses findActiveRecruitments() {
        List<ActiveRecruitmentResponse> responses = recruitRepository.findActiveRecruitments(LocalDateTime.now()).stream()
                .map(ActiveRecruitmentResponse::from)
                .toList();
        return new ActiveRecruitmentResponses(responses);
    }

    @Override
    public void updateRecruit(Long recruitId, RecruitUpdateRequest request) {
        Recruit recruit = getRecruitEntity(recruitId);
        if (recruit.isClosed()) {
            throw new RecruitException(RecruitErrorCode.UPDATE_NOT_ALLOW_FOR_CLOSED);
        }
        JobFamily previousJobFamily = recruit.getJobFamily();
        recruit.update(
                request.title(),
                request.jobFamily(),
                request.startDate(),
                request.endDate(),
                request.summary(),
                request.recruitInformation(),
                request.notice(),
                request.getFaqsOrNull()
        );
        eventPublisher.publishEvent(new RecruitUpdatedEvent(
                recruit.getId(),
                previousJobFamily,
                recruit.getJobFamily(),
                recruit.getStartDate(),
                recruit.getEndDate()));
    }

    @Override
    public void cancelRecruit(Long recruitId) {
        Recruit recruit = getRecruitEntity(recruitId);
        recruitRepository.delete(recruit);
        eventPublisher.publishEvent(new RecruitCanceledEvent(recruit.getId(), recruit.getJobFamily()));
    }

    private void validateDuplicatedJobFamily(List<RecruitRegisterRequest> requests, Semester recruitingSemester) {
        List<JobFamily> jobFamilies = requests.stream()
                .map(RecruitRegisterRequest::jobFamily)
                .toList();
        if (recruitRepository.existsByJobFamilyAndIsNotClosed(recruitingSemester.getId(), jobFamilies)) {
            throw new RecruitException(RecruitErrorCode.DUPLICATED_JOB_FAMILY);
        }
    }

    private Recruit getRecruitEntity(Long recruitId) {
        return recruitRepository.findById(recruitId)
                .orElseThrow(() -> new RecruitException(RecruitErrorCode.NOT_FOUND_RECRUIT));
    }
}
