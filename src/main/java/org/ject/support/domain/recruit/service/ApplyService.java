package org.ject.support.domain.recruit.service;

import java.time.LocalDate;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.util.PeriodAccessible;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.exception.QuestionErrorCode;
import org.ject.support.domain.recruit.exception.QuestionException;
import org.ject.support.domain.recruit.exception.RecruitErrorCode;
import org.ject.support.domain.recruit.exception.RecruitException;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.domain.tempapply.service.TemporaryApplyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ApplyService implements ApplyUsecase {
    private final TemporaryApplyService temporaryApplyService;
    private final RecruitRepository recruitRepository;

    @Override
    @PeriodAccessible
    public Map<String, String> getTemporaryApplication(final Long memberId) {
        return temporaryApplyService.findMembersRecentTemporaryApplication(memberId);
    }

    @Override
    @PeriodAccessible
    public void applyTemporary(JobFamily jobFamily, Long memberId, Map<String, String> answers) {
        //1. jobFamily를 통해 현재 기수 지원양식 id를 가져옴
        Recruit recruit = getPeriodRecruit(jobFamily);

        //2. 지원양식과 answers의 key를 비교해 올바른 질문 양식인지 점검
        validateQuestions(answers, recruit);

        //3. 지원서 저장
        temporaryApplyService.saveTemporaryApplication(memberId, answers, jobFamily);
    }

    private void validateQuestions(final Map<String, String> answers, final Recruit recruit) {
        answers.keySet().stream()
                .map(Long::parseLong)
                .filter(recruit::isInvalidQuestionId)
                .forEach(key -> {
                    throw new QuestionException(QuestionErrorCode.NOT_FOUND);
                });
    }

    //TODO 2025 02 20 17:07:14 : caching
    private Recruit getPeriodRecruit(final JobFamily jobFamily) {
        return recruitRepository.findActiveRecruits(LocalDate.now()).stream()
                .filter(recruit -> recruit.getJobFamily().equals(jobFamily))
                .findAny()
                .orElseThrow(() -> new RecruitException(RecruitErrorCode.NOT_FOUND));
    }
}
