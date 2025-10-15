package org.ject.support.domain.apply.service;

import static org.ject.support.domain.apply.domain.Apply.Status.JOINED;
import static org.ject.support.domain.apply.domain.Apply.Status.SUBMITTED;
import static org.ject.support.domain.apply.domain.Apply.Status.TEMP_SAVED;
import static org.ject.support.domain.apply.exception.ApplyErrorCode.ALREADY_SUBMITTED;
import static org.ject.support.domain.apply.exception.ApplyErrorCode.NOT_FOUND_APPLY;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.common.util.PeriodAccessible;
import org.ject.support.common.util.String2MapSerializer;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.Portfolio;
import org.ject.support.domain.apply.dto.ApplyPortfolioDto;
import org.ject.support.domain.apply.dto.ApplyProfileRequest;
import org.ject.support.domain.apply.dto.ApplyStatusResponse;
import org.ject.support.domain.apply.dto.TempApplicationFormResponse;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.apply.repository.ApplicationFormRepository;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.event.TempMemberRegisteredEvent;
import org.ject.support.domain.member.exception.MemberErrorCode;
import org.ject.support.domain.member.exception.MemberException;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.exception.QuestionErrorCode;
import org.ject.support.domain.recruit.exception.QuestionException;
import org.ject.support.domain.recruit.exception.RecruitErrorCode;
import org.ject.support.domain.recruit.exception.RecruitException;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplyService implements ApplyUsecase {
    private final RecruitRepository recruitRepository;
    private final ApplyRepository applyRepository;
    private final ApplicationFormRepository applicationFormRepository;
    private final MemberRepository memberRepository;
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
    public void deleteProfileAndTempApplicationForm(Long memberId) {
        // 지원 정보 조회
        Apply apply = applyRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ApplyException(NOT_FOUND_APPLY));

        // 지원서를 임시 저장하지 않은 경우 실패
        if (apply.isNotTempSaved()) {
            throw new ApplyException(ApplyErrorCode.NOT_FOUND_TEMP_APPLICATION_FORM);
        }

        // 임시 저장한 지원서 제거 및 상태 변경
        apply.deleteApplicationForm();
        apply.updateStatus(JOINED);

        // Apply update status to joined
        // Apply update status joined

        // 프로필 제거
        Member applicant = apply.getMember();
        applicant.deleteProfile();
    }

    @Override
    @PeriodAccessible
    @Transactional
    public void submitApplication(Long memberId,
                                  JobFamily jobFamily,
                                  Map<String, String> answers,
                                  List<ApplyPortfolioDto> portfolios) {
        // 1. jobFamily를 통해 현재 기수 지원양식 id 조회
        Recruit recruit = getPeriodRecruit(jobFamily);

        // 2. 지원양식과 answers의 key를 비교해 올바른 질문 양식인지 점검
        validateQuestions(answers, recruit);

        // 3. 지원 정보 조회
        Apply apply = applyRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ApplyException(NOT_FOUND_APPLY));

        // 4. Portfolio와 ApplicationForm 영속화
        if (apply.isSubmitted()) {
            throw new ApplyException(ALREADY_SUBMITTED);
        }

        String content = map2JsonSerializer.serializeAsString(answers);
        List<Portfolio> newPortfolios = getNewPortfolios(portfolios);

        // 임시 저장한 지원서가 있을 경우 업데이트
        if (apply.isTempSaved()) {
            ApplicationForm applicationForm = apply.getApplicationForm();
            applicationForm.updateContentAndPortfolios(content, newPortfolios);
        } else {
            ApplicationForm applicationForm = createApplicationForm(apply, content, newPortfolios);
            applicationFormRepository.save(applicationForm);
            apply.updateApplicationForm(applicationForm);
        }
        apply.updateStatus(SUBMITTED);
    }

    @Override
    @PeriodAccessible(permitAllJob = true)
    public ApplyStatusResponse checkApplySubmit(Long memberId) {
        return applyRepository.findByMemberId(memberId)
                .map(ApplyStatusResponse::of)
                .orElseThrow(() -> new ApplyException(NOT_FOUND_APPLY));
    }

    @Override
    @PeriodAccessible
    @Transactional
    public void saveProfile(Long memberId, ApplyProfileRequest request) {
        var member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ApplyException(NOT_FOUND_APPLY));

        var memberEditorBuilder = member.toEditor();

        var memberEditor = memberEditorBuilder
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                .jobFamily(request.jobFamily())
                .careerDetails(request.careerDetails())
                .experiencePeriod(request.experiencePeriod())
                .interestedDomains(request.interestedDomains())
                .build();

        member.edit(memberEditor);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTempMemberRegisteredEvent(TempMemberRegisteredEvent event) {
        log.info("임시 회원 가입 이벤트 수신: memberId={}", event.memberId());

        Member member = memberRepository.findById(event.memberId())
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));
        if (applyRepository.findByMemberId(event.memberId()).isPresent()) {
            log.info("이미 Apply 엔티티가 존재하여 생성을 건너뜀: memberId={}", member.getId());
            return;
        }
        Apply apply = Apply.createApply(member);
        applyRepository.save(apply);
        log.info("Apply 엔티티 생성 및 저장 완료: applyId={}", apply.getId());
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
                .toList();
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
