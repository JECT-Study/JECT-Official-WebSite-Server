package org.ject.support.domain.apply.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.common.util.DateTimeUtil;
import org.ject.support.domain.applicant.repository.ApplicantRepository;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.external.email.domain.EmailTemplate;
import org.ject.support.external.email.service.SesEmailSendService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RemindApplyService implements RemindApplyUsecase {

    private static final String EMAIL_SEND_GROUP = "remind_apply";

    private final RecruitRepository recruitRepository;
    private final ApplicantRepository applicantRepository;
    private final ApplyRepository applyRepository;
    private final SesEmailSendService emailSendService;

    @Override
    public void remindApply(Long recruitId) {
        // 현재 활성화된 모집 정보 조회
        Recruit recruit = recruitRepository.findActiveRecruitById(recruitId, LocalDateTime.now());

        // 모집 기간 중 지원서 임시 저장한 지원자 ID 모두 조회
        List<Long> applicantIds = applyRepository.findByRecruitAndStatus(recruit, ApplyStatus.TEMP_SAVED)
                .stream()
                .map(apply -> apply.getApplicant().getId())
                .toList();

        // 지원서 최종 제출하지 않은 지원자 필터링
        List<String> targetEmails = applicantRepository.findEmailsByIdsAndNotSubmitted(applicantIds);

        // 필터링한 지원자들에게 리마인드
        emailSendService.sendBulkTemplatedEmail(
                EmailTemplate.REMIND_APPLY,
                targetEmails,
                Map.of("deadline", DateTimeUtil.formatWithDayOfWeek(recruit.getEndDate())));
    }
}
