package org.ject.support.domain.tempapply.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.external.email.service.SesEmailSendService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RemindApplyService implements RemindApplyUsecase {

    private static final String EMAIL_SEND_GROUP = "remind_apply";

    private final RecruitRepository recruitRepository;
    private final MemberRepository memberRepository;
    private final TemporaryApplyService temporaryApplyService;
    private final SesEmailSendService emailSendService;

    @Override
    public void remindApply() {
        // 현재 활성화된 모집 정보 조회
        List<Recruit> activeRecruits = recruitRepository.findActiveRecruits(LocalDateTime.now());

        // 모집 기간 중 지원서 임시 저장한 지원자 ID 모두 조회
        List<Long> applicantIds = temporaryApplyService.findMemberIdsByActiveRecruits(activeRecruits);

        // 지원서 최종 제출하지 않은 지원자 필터링
        List<String> targetEmails = memberRepository.findEmailsByIdsAndNotApply(applicantIds);

        // 필터링한 지원자들에게 리마인드
        emailSendService.sendBulkTemplatedEmail(
                EMAIL_SEND_GROUP,
                targetEmails,
                Map.of("deadline", formatDeadline(activeRecruits.getLast().getEndDate())));
    }

    private String formatDeadline(LocalDateTime recruitEndDate) {
        String[] dayOfWeekNames = {"월", "화", "수", "목", "금", "토", "일"};
        String dayOfWeekName = dayOfWeekNames[recruitEndDate.getDayOfWeek().getValue() - 1];
        String datePart = recruitEndDate.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일"));
        String timePart = recruitEndDate.format(DateTimeFormatter.ofPattern("HH:mm"));
        return String.format("%s(%s) %s", datePart, dayOfWeekName, timePart);
    }
}
