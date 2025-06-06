package org.ject.support.domain.recruit.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.service.OngoingSemesterProvider;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.dto.RecruitCanceledEvent;
import org.ject.support.domain.recruit.dto.RecruitRegisterRequest;
import org.ject.support.domain.recruit.dto.RecruitUpdateRequest;
import org.ject.support.domain.recruit.exception.RecruitErrorCode;
import org.ject.support.domain.recruit.exception.RecruitException;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RecruitService implements RecruitUsecase {

    private final RecruitRepository recruitRepository;
    private final OngoingSemesterProvider ongoingSemesterProvider;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void registerRecruits(List<RecruitRegisterRequest> requests) {
        // 1. 모집중인 기수 ID 조회
        Long ongoingSemesterId = ongoingSemesterProvider.getOngoingSemesterId();

        // 2. 이미 모집중인 직군인지 검증
        validateDuplicatedJobFamily(requests, ongoingSemesterId);

        // 3. recruit 엔티티 저장
        List<Recruit> recruits = requests.stream()
                .map(request -> request.toEntity(ongoingSemesterId))
                .toList();
        recruitRepository.saveAll(recruits);
    }

    @Override
    public void updateRecruit(Long recruitId, RecruitUpdateRequest request) {
        Recruit recruit = getRecruit(recruitId);

        if (recruit.isClosed()) {
            throw new RecruitException(RecruitErrorCode.UPDATE_NOT_ALLOW_FOR_CLOSED);
        }

        recruit.update(request.jobFamily(), request.startDate(), request.endDate());
    }

    @Override
    public void cancelRecruit(Long recruitId) {
        Recruit recruit = getRecruit(recruitId);
        recruitRepository.delete(recruit);
        eventPublisher.publishEvent(new RecruitCanceledEvent(recruit.getId(), recruit.getJobFamily()));
    }

    private void validateDuplicatedJobFamily(List<RecruitRegisterRequest> requests, Long ongoingSemesterId) {
        List<JobFamily> jobFamilies = requests.stream()
                .map(RecruitRegisterRequest::jobFamily)
                .toList();
        if (recruitRepository.existsByJobFamilyAndIsNotClosed(ongoingSemesterId, jobFamilies)) {
            throw new RecruitException(RecruitErrorCode.DUPLICATED_JOB_FAMILY);
        }
    }

    private Recruit getRecruit(Long recruitId) {
        return recruitRepository.findById(recruitId)
                .orElseThrow(() -> new RecruitException(RecruitErrorCode.NOT_FOUND));
    }
}
