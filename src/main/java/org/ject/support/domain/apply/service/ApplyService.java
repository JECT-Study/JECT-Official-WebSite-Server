package org.ject.support.domain.apply.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.common.util.PeriodAccessible;
import org.ject.support.common.util.String2MapSerializer;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.domain.Portfolio;
import org.ject.support.domain.apply.dto.ApplyPortfolioDto;
import org.ject.support.domain.apply.dto.ApplyProfileRequest;
import org.ject.support.domain.apply.dto.ApplyStatusResponse;
import org.ject.support.domain.apply.dto.TempApplicationFormResponse;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.apply.repository.ApplicationFormRepository;
import org.ject.support.domain.apply.repository.ApplyRepository;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.ject.support.domain.apply.domain.ApplyStatus.JOINED;
import static org.ject.support.domain.apply.domain.ApplyStatus.SUBMITTED;
import static org.ject.support.domain.apply.domain.ApplyStatus.TEMP_SAVED;
import static org.ject.support.domain.apply.exception.ApplyErrorCode.ALREADY_SUBMITTED;
import static org.ject.support.domain.apply.exception.ApplyErrorCode.NOT_FOUND_APPLY;

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
    public TempApplicationFormResponse findTempApplicationForm(final Long memberId, final Long recruitId) {
        Apply apply = applyRepository.findByMemberIdAndRecruitIdInActiveRecruit(memberId, recruitId, LocalDateTime.now())
                .orElseThrow(() -> new ApplyException(NOT_FOUND_APPLY));

        if (apply.isNotTempSaved()) {
            throw new ApplyException(ApplyErrorCode.NOT_FOUND_TEMP_APPLICATION_FORM);
        }

        ApplicationForm tempApplicationForm = apply.getApplicationForm();
        return TempApplicationFormResponse.from(
                apply.getRecruit().getJobFamily(),
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
                                           Long recruitId,
                                           Map<String, String> answers,
                                           List<ApplyPortfolioDto> portfolios) {
        // 1. memberId와 recruitId를 바탕으로 apply 조회
        Apply apply = applyRepository.findByMemberIdAndRecruitIdInActiveRecruit(memberId, recruitId, LocalDateTime.now())
                .orElseThrow(() -> new ApplyException(NOT_FOUND_APPLY));

        ApplyStatus applyStatus = apply.getStatus();

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
    public void deleteProfileAndTempApplicationForm(Long memberId, Long recruitId) {
        // 지원 정보 조회
        Apply apply = applyRepository.findByMemberIdAndRecruitIdInActiveRecruit(memberId, recruitId, LocalDateTime.now())
                .orElseThrow(() -> new ApplyException(NOT_FOUND_APPLY));

        // 지원서를 임시 저장하지 않은 경우 실패
        if (apply.isNotTempSaved()) {
            throw new ApplyException(ApplyErrorCode.NOT_FOUND_TEMP_APPLICATION_FORM);
        }

        // 임시 저장한 지원서 제거 및 상태 변경
        apply.deleteApplicationForm();
        apply.updateStatus(JOINED);

        // 프로필 제거
        Member applicant = apply.getMember();
        applicant.deleteProfile();
    }

    @Override
    @PeriodAccessible(permitAllJob = true)
    @Transactional
    public void submitApplication(Long memberId,
                                  Long recruitId,
                                  Map<String, String> answers,
                                  List<ApplyPortfolioDto> portfolios) {
        try {
            // 1. 지원 정보 조회 (낙관적 락 - @Version 기반 동시성 제어)
            Apply apply = applyRepository.findByMemberIdAndRecruitIdInActiveRecruit(
                            memberId, recruitId, LocalDateTime.now())
                    .orElseThrow(() -> new ApplyException(NOT_FOUND_APPLY));

            // 2. 지원양식과 answers의 key를 비교해 올바른 질문 양식인지 점검
            validateQuestions(answers, apply.getRecruit());

            String content = map2JsonSerializer.serializeAsString(answers);
            List<Portfolio> newPortfolios = getNewPortfolios(portfolios);

            // 3. ApplicationForm 준비
            ApplicationForm applicationForm;
            if (apply.isTempSaved()) {
                applicationForm = apply.getApplicationForm();
                applicationForm.updateContentAndPortfolios(content, newPortfolios);
            } else {
                applicationForm = createApplicationForm(apply, content, newPortfolios);
                applicationFormRepository.save(applicationForm);
            }

            // 4. Apply 엔티티에 제출 위임 (검증 및 상태 변경 포함)
            apply.submit(applicationForm);

            // 명시적 flush를 통해 낙관적 락 충돌을 메서드 내부에서 감지하고 catch 블록으로 전달합니다.
            applyRepository.flush();
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("지원서 제출 중 낙관적 락 충돌 발생. memberId: {}", memberId);
            throw new ApplyException(ALREADY_SUBMITTED);
        }
    }

    @Override
    @PeriodAccessible(permitAllJob = true)
    public ApplyStatusResponse checkApplyStatus(Long memberId, Long recruitId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        return applyRepository.findByMemberIdAndRecruitIdInActiveRecruit(memberId, recruitId, LocalDateTime.now())
                .map(ApplyStatusResponse::of)
                .orElseThrow(() -> new ApplyException(NOT_FOUND_APPLY));
    }

    @Override
    @PeriodAccessible(permitAllJob = true)
    @Transactional
    public void saveProfile(Long memberId, Long recruitId, ApplyProfileRequest request) {
        var member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));
        Recruit recruit = getActiveRecruit(recruitId);

        createApplyIfNotExists(member, recruit);

        var memberEditorBuilder = member.toEditor();
        var memberEditor = memberEditorBuilder
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                .jobFamily(recruit.getJobFamily())
                .region(request.region())
                .careerDetails(request.careerDetails())
                .experiencePeriod(request.experiencePeriod())
                .interestedDomains(request.interestedDomains())
                .build();

        member.edit(memberEditor);
    }

    private void createApplyIfNotExists(Member member, Recruit recruit) {
        boolean exists = applyRepository.existsByMemberIdAndRecruitIdInActiveRecruit(
                member.getId(), recruit.getId(), LocalDateTime.now());
        if (!exists) {
            try {
                var newApply = Apply.createApply(member, recruit);
                applyRepository.save(newApply);
            } catch (DataIntegrityViolationException e) {
                log.warn("지원서 생성 중 중복 생성 충돌 발생. memberId: {}, recruitId: {}. 이미 지원서가 존재합니다.",
                        member.getId(), recruit.getId());
            }
        }
    }

    private void validateQuestions(final Map<String, String> answers, final Recruit recruit) {
        answers.keySet().stream()
                .map(Long::parseLong)
                .filter(recruit::isInvalidQuestionId)
                .forEach(key -> {
                    throw new QuestionException(QuestionErrorCode.NOT_FOUND_QUESTION);
                });
    }

    private Recruit getActiveRecruit(final Long recruitId) {
        return Optional.ofNullable(recruitRepository.findActiveRecruitById(recruitId, LocalDateTime.now()))
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
