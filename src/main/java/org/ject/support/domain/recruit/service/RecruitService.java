package org.ject.support.domain.recruit.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.dto.ActiveRecruitmentResponse;
import org.ject.support.domain.recruit.dto.ActiveRecruitmentResponses;
import org.ject.support.domain.recruit.dto.RecruitCanceledEvent;
import org.ject.support.domain.recruit.dto.RecruitRegisterRequest;
import org.ject.support.domain.recruit.dto.RecruitResponse;
import org.ject.support.domain.recruit.dto.RecruitUpdateRequest;
import org.ject.support.domain.recruit.dto.RecruitUpdatedEvent;
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

@Service
@RequiredArgsConstructor
@Transactional
public class RecruitService implements RecruitUsecase {

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
                request.jobFamily(),
                request.startDate(),
                request.endDate(),
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
