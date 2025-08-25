package org.ject.support.domain.recruit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.common.util.PeriodAccessible;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.exception.MemberErrorCode;
import org.ject.support.domain.member.exception.MemberException;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.domain.recruit.domain.ApplicationForm;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.dto.ApplyPortfolioDto;
import org.ject.support.domain.recruit.dto.ApplyTemporaryResponse;
import org.ject.support.domain.recruit.exception.QuestionErrorCode;
import org.ject.support.domain.recruit.exception.QuestionException;
import org.ject.support.domain.recruit.exception.RecruitErrorCode;
import org.ject.support.domain.recruit.exception.RecruitException;
import org.ject.support.domain.recruit.repository.ApplicationFormRepository;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.domain.tempapply.service.TemporaryApplyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplyService implements ApplyUsecase {
    private final TemporaryApplyService temporaryApplyService;
    private final RecruitRepository recruitRepository;
    private final MemberRepository memberRepository;
    private final ApplicationFormRepository applicationFormRepository;
    private final Map2JsonSerializer map2JsonSerializer;

    @Override
    @PeriodAccessible(permitAllJob = true)
    @Transactional(readOnly = true)
    public ApplyTemporaryResponse getTemporaryApplication(final Long memberId) {
        return temporaryApplyService.findMembersRecentTemporaryApplication(memberId);
    }

    @Override
    @PeriodAccessible
    @Transactional(readOnly = true)
    public void applyTemporary(JobFamily jobFamily,
                               Long memberId,
                               Map<String, String> answers,
                               List<ApplyPortfolioDto> portfolios) {
        // 1. jobFamily를 통해 현재 기수 지원양식 id를 가져옴
        Recruit recruit = getPeriodRecruit(jobFamily);

        // 2. 지원양식과 answers의 key를 비교해 올바른 질문 양식인지 점검
        validateQuestions(answers, recruit);

        // 3. 지원서 저장
        temporaryApplyService.saveTemporaryApplication(memberId, answers, jobFamily, portfolios);
    }

    @Override
    @PeriodAccessible(permitAllJob = true)
    @Transactional
    public void deleteTemporaryApplications(Long memberId) {
        // memberId를 통해 기존 임시 지원서 모두 제거
        temporaryApplyService.deleteTemporaryApplicationsByMemberId(memberId);
    }

    @Override
    @PeriodAccessible
    @Transactional
    public void submitApplication(Long memberId,
                                  JobFamily jobFamily,
                                  Map<String, String> answers,
                                  List<ApplyPortfolioDto> portfolios) {
        // 1. 지원자 조회
        Member applicant = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        // 2. jobFamily를 통해 현재 기수 지원양식 id를 가져옴
        Recruit recruit = getPeriodRecruit(jobFamily);

        // 3. 지원양식과 answers의 key를 비교해 올바른 질문 양식인지 점검
        validateQuestions(answers, recruit);

        // 4. Portfolio와 ApplicationForm 영속화
        ApplicationForm applicationForm = createApplicationForm(answers, applicant, recruit);
        portfolios.stream()
                .map(ApplyPortfolioDto::toEntity)
                .forEach(applicationForm::addPortfolio);
        applicationFormRepository.save(applicationForm);
    }

    @Override
    @PeriodAccessible(permitAllJob = true)
    public boolean checkApplySubmit(Long memberId) {
        return applicationFormRepository.existsByMemberId(memberId, LocalDateTime.now());
    }

    private void validateQuestions(final Map<String, String> answers, final Recruit recruit) {
        answers.keySet().stream()
                .map(Long::parseLong)
                .filter(recruit::isInvalidQuestionId)
                .forEach(key -> {
                    throw new QuestionException(QuestionErrorCode.NOT_FOUND_QUESTION);
                });
    }

    //TODO 2025 02 20 17:07:14 : caching
    private Recruit getPeriodRecruit(final JobFamily jobFamily) {
        return recruitRepository.findActiveRecruits(LocalDateTime.now()).stream()
                .filter(recruit -> recruit.getJobFamily().equals(jobFamily))
                .findAny()
                .orElseThrow(() -> new RecruitException(RecruitErrorCode.NOT_FOUND_RECRUIT));
    }

    private ApplicationForm createApplicationForm(Map<String, String> answers, Member applicant, Recruit recruit) {
        return ApplicationForm.builder()
                .content(map2JsonSerializer.serializeAsString(answers))
                .member(applicant)
                .recruit(recruit)
                .build();
    }
}
