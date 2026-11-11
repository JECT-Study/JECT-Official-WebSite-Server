package org.ject.support.domain.recruit.service;

import jakarta.validation.Validator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ject.support.admin.apply.service.AdminApplyService;
import org.ject.support.admin.member.dto.request.CreateRecruitMemberRequest;
import org.ject.support.admin.member.service.AdminMemberActivityService;
import org.ject.support.admin.member.service.AdminMemberService;
import org.ject.support.common.exception.BusinessException;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.exception.RecruitErrorCode;
import org.ject.support.domain.recruit.exception.RecruitException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecruitCompleteUseCase {

    private final RecruitUsecase recruitUsecase;
    private final AdminApplyService adminApplyService;
    private final AdminMemberService adminMemberService;
    private final AdminMemberActivityService adminMemberActivityService;
    private final Validator validator;

    // 제출완료 지원자의 구성원 반영과 선정 결과 정리 및 공고 종료를 일괄 처리
    @Transactional
    public void completeRecruit(Long recruitId) {
        Recruit recruit = recruitUsecase.getRecruitEntity(recruitId);
        if (recruit.isClosed()) {
            throw new RecruitException(RecruitErrorCode.ALREADY_CLOSED);
        }
        List<Apply> applies = adminApplyService.getSubmittedApplies(recruitId);
        if (applies.stream().anyMatch(apply -> apply.getSelectionResult() == null
                || apply.getSelectionResult() == SelectionResult.UNDECIDED)) {
            throw new RecruitException(RecruitErrorCode.UNDECIDED_SELECTION_RESULT);
        }
        List<Apply> passedApplies = applies.stream()
                .filter(apply -> apply.getSelectionResult() == SelectionResult.PASSED).toList();
        Long failedApplyId = null;
        try {
            for (Apply apply : passedApplies) {
                failedApplyId = apply.getId();
                CreateRecruitMemberRequest request = CreateRecruitMemberRequest.from(apply);
                if (!validator.validate(request).isEmpty()) {
                    throw new RecruitException(RecruitErrorCode.RECRUIT_COMPLETION_FAILED);
                }
                Long memberId = adminMemberService.findOrCreateMember(request);
                adminMemberActivityService.createRecruitMemberActivity(request, memberId);
            }
            failedApplyId = null;
            adminApplyService.rejectWaitlistedApplies(recruitId);
            recruitUsecase.completeRecruit(recruitId);
        } catch (RuntimeException exception) {
            String cause = exception instanceof BusinessException businessException
                    ? businessException.getErrorCode().getCode() : exception.getClass().getSimpleName();
            log.error("모집확정 실패. recruitId: {}, applyId: {}, targetCount: {}, cause: {}",
                    recruitId, failedApplyId, passedApplies.size(), cause);
            throw new RecruitException(RecruitErrorCode.RECRUIT_COMPLETION_FAILED);
        }
    }
}
