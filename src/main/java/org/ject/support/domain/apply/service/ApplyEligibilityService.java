package org.ject.support.domain.apply.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.member.entity.MemberActivity;
import org.ject.support.domain.member.service.MemberQueryService;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplyEligibilityService {

    private final ApplyRepository applyRepository;
    private final MemberQueryService memberQueryService;

    // 지원 대상 유형과 기수의 활동 여부 및 다른 공고의 진행 중인 지원 검증
    public void validateApplication(String email, Recruit recruit) {
        for (MemberActivity activity : memberQueryService.searchActiveActivities(email)) {
            if (activity.getMemberType() == recruit.getRecruitType().toMemberType()
                    && (recruit.getRecruitType() != RecruitType.SEMESTER
                    || activity.getMemberSemester().getSemesterId().equals(recruit.getSemester().getId()))) {
                throw new ApplyException(ApplyErrorCode.ACTIVE_MEMBER_APPLICATION_NOT_ALLOWED);
            }
        }
        validateDuplicateApplication(email, recruit.getId());
    }

    // 현재 공고를 제외하고 OPEN 공고의 임시저장 및 제출완료 지원 여부 검증
    public void validateDuplicateApplication(String email, Long recruitId) {
        if (applyRepository.existsActiveApplicationInOtherRecruit(email, recruitId)) {
            throw new ApplyException(ApplyErrorCode.APPLY_EXISTS_IN_OTHER_RECRUIT);
        }
    }
}
