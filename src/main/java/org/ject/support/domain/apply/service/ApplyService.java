package org.ject.support.domain.apply.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.common.util.PeriodAccessible;
import org.ject.support.common.util.String2MapSerializer;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.Portfolio;
import org.ject.support.domain.apply.dto.ApplyPortfolioDto;
import org.ject.support.domain.apply.dto.TempApplicationFormResponse;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.apply.repository.ApplicationFormRepository;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.exception.MemberErrorCode;
import org.ject.support.domain.member.exception.MemberException;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.exception.QuestionErrorCode;
import org.ject.support.domain.recruit.exception.QuestionException;
import org.ject.support.domain.recruit.exception.RecruitErrorCode;
import org.ject.support.domain.recruit.exception.RecruitException;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.domain.tempapply.service.TemporaryApplyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.ject.support.domain.apply.domain.Apply.Status.JOINED;
import static org.ject.support.domain.apply.domain.Apply.Status.SUBMITTED;
import static org.ject.support.domain.apply.domain.Apply.Status.TEMP_SAVED;
import static org.ject.support.domain.apply.exception.ApplyErrorCode.ALREADY_SUBMITTED;
import static org.ject.support.domain.apply.exception.ApplyErrorCode.NOT_FOUND_APPLY;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplyService implements ApplyUsecase {
    private final TemporaryApplyService temporaryApplyService;
    private final RecruitRepository recruitRepository;
    private final ApplyRepository applyRepository;
    private final MemberRepository memberRepository;
    private final ApplicationFormRepository applicationFormRepository;
    private final Map2JsonSerializer map2JsonSerializer;
    private final String2MapSerializer string2MapSerializer;

    @Override
    @PeriodAccessible(permitAllJob = true)
    @Transactional(readOnly = true)
    public TempApplicationFormResponse findTempApplicationForm(final Long memberId) {
        Apply apply = applyRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ApplyException(NOT_FOUND_APPLY));

        if (apply.isNotTempSaved()) {
            throw new ApplyException(ApplyErrorCode.NOT_FOUND_TEMP_APPLICATION_FORM);
        }

        ApplicationForm tempApplicationForm = apply.getApplicationForm();
        return TempApplicationFormResponse.from(
                string2MapSerializer.serializeAsMap(tempApplicationForm.getContent()),
                tempApplicationForm.getPortfolios()
                        .stream()
                        .map(ApplyPortfolioDto::from)
                        .toList());
    }

    @Override
    @PeriodAccessible(permitAllJob = true)
    @Transactional
    public void saveApplicationTemporarily(Long memberId,
                                           Map<String, String> answers,
                                           List<ApplyPortfolioDto> portfolios) {
        // 1. memberId를 바탕으로 apply 조회
        Apply apply = applyRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ApplyException(NOT_FOUND_APPLY));

        Apply.Status applyStatus = apply.getStatus();

        // 2. 지원서 제출 여부 검증
        if (applyStatus.equals(SUBMITTED)) {
            throw new ApplyException(ALREADY_SUBMITTED);
        }

        // 3. 해당 모집건의 question id와 answers의 key를 비교해 올바른 질문 양식인지 점검
        validateQuestions(answers, apply.getRecruit());

        // 4. 새로운 본문 내용과 포트폴리오 생성
        String newContent = map2JsonSerializer.serializeAsString(answers);
        List<Portfolio> newPortfolios = getNewPortfolios(portfolios);

        // 5-1. 지원서를 최초 작성하는 경우, 새로운 applicationForm 저장 및 상태 업데이트
        if (applyStatus.equals(JOINED)) {
            ApplicationForm applicationForm = createApplicationForm(apply, newContent, newPortfolios);
            applicationFormRepository.save(applicationForm);

            apply.updateApplicationForm(applicationForm);
            apply.updateStatus(TEMP_SAVED);
            return;
        }

        // 5-2. 이미 저장했던 상태라면 applicationForm 업데이트
        ApplicationForm applicationForm = apply.getApplicationForm();
        applicationForm.updateContentAndPortfolios(newContent, newPortfolios);
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

        // 2. jobFamily를 통해 현재 기수 지원양식 id 조회
        Recruit recruit = getPeriodRecruit(jobFamily);

        // 3. 지원양식과 answers의 key를 비교해 올바른 질문 양식인지 점검
        validateQuestions(answers, recruit);

        // 4. 지원 정보 조회
        Apply apply = applyRepository.findByMember(applicant)
                .orElseThrow(() -> new ApplyException(NOT_FOUND_APPLY));

        // 5. Portfolio와 ApplicationForm 영속화
        // TODO 임시 저장한 지원서가 있을 경우 업데이트
        String content = map2JsonSerializer.serializeAsString(answers);
        ApplicationForm applicationForm = createApplicationForm(apply, content, getNewPortfolios(portfolios));
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

    private List<Portfolio> getNewPortfolios(List<ApplyPortfolioDto> portfolios) {
        return portfolios.stream()
                .map(ApplyPortfolioDto::toEntity)
                .collect(Collectors.toList());
    }

    private ApplicationForm createApplicationForm(Apply apply, String content, List<Portfolio> portfolios) {
        ApplicationForm applicationForm = ApplicationForm.builder()
                .apply(apply)
                .content(content)
                .portfolios(portfolios)
                .build();
        portfolios.forEach(portfolio -> portfolio.setApplicationForm(applicationForm));
        return applicationForm;
    }
}
