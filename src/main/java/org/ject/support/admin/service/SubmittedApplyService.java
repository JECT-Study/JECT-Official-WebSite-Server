package org.ject.support.admin.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.common.data.PageResponse;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.common.util.String2MapSerializer;
import org.ject.support.admin.dto.SubmittedApplyCountResponse;
import org.ject.support.admin.dto.SubmittedApplyDetailResponse;
import org.ject.support.admin.dto.SubmittedApplyEditRequest;
import org.ject.support.admin.dto.SubmittedApplyResponse;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.Apply.Status;
import org.ject.support.domain.apply.domain.Portfolio;
import org.ject.support.domain.apply.dto.ApplyPortfolioDto;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.admin.repository.AdminApplyQueryRepository;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.entity.MemberEditor;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.exception.QuestionErrorCode;
import org.ject.support.domain.recruit.exception.QuestionException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubmittedApplyService {

    private final ApplyRepository applyRepository;
    private final AdminApplyQueryRepository adminApplyQueryRepository;
    private final String2MapSerializer string2MapSerializer;
    private final Map2JsonSerializer map2JsonSerializer;

    @Transactional(readOnly = true)
    public Page<SubmittedApplyResponse> findSubmittedApplies(final JobFamily jobFamily,
                                                             final Long semesterId,
                                                             final Pageable pageable) {
        Page<Apply> applyPage = adminApplyQueryRepository.findAppliesByStatus(jobFamily, Status.SUBMITTED, semesterId, pageable);

        List<SubmittedApplyResponse> content = applyPage.getContent().stream()
                .map(this::toSubmittedApplyResponse)
                .toList();

        return PageResponse.from(content, pageable, applyPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public SubmittedApplyDetailResponse findSubmittedApplyDetail(final Long applyId) {
        return applyRepository.findByIdAndStatusWithMember(applyId, Status.SUBMITTED)
                .map(this::toSubmittedApplyDetailResponse)
                .orElseThrow(() -> new ApplyException(ApplyErrorCode.NOT_FOUND_APPLY));
    }

    @Transactional(readOnly = true)
    public SubmittedApplyCountResponse countSubmittedApply() {
        Long count = applyRepository.countByStatus(Status.SUBMITTED);
        return new SubmittedApplyCountResponse(count);
    }

    @Transactional
    public void updateSubmittedApply(final Long applyId,
                                     final SubmittedApplyEditRequest request) {
        Apply apply = applyRepository.findByIdAndStatusWithMember(applyId, Status.SUBMITTED)
                .orElseThrow(() -> new ApplyException(ApplyErrorCode.NOT_FOUND_APPLY));
        ensureSubmitted(apply);

        Member member = apply.getMember();
        MemberEditor memberEditor = member.toEditor()
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                .email(request.email())
                .jobFamily(request.jobFamily())
                .build();
        member.edit(memberEditor);

        Map<String, String> answers = request.answers();
        validateQuestions(answers, apply.getRecruit());
        String newContent = map2JsonSerializer.serializeAsString(answers);

        List<Portfolio> newPortfolios = request.portfolios()
                .stream()
                .map(ApplyPortfolioDto::toEntity)
                .toList();

        apply.getApplicationForm()
                .updateContentAndPortfolios(newContent, newPortfolios);
    }

    @Transactional
    public void deleteSubmittedApply(final Long applyId) {
        Apply apply = applyRepository.findByIdAndStatusWithMember(applyId, Status.SUBMITTED)
                .orElseThrow(() -> new ApplyException(ApplyErrorCode.NOT_FOUND_APPLY));

        deleteProfileAndApplicationForm(apply);
    }

    @Transactional
    public int deleteSubmittedApplies(final List<Long> applyIds) {
        final List<Long> distinctIds = applyIds.stream().distinct().toList();
        final List<Apply> applies = applyRepository.findAllByIdAndStatusWithMember(distinctIds, Status.SUBMITTED);

        if (applies.size() != distinctIds.size()) {
            throw new ApplyException(ApplyErrorCode.NOT_FOUND_APPLY);
        }

        applies.forEach(this::deleteProfileAndApplicationForm);
        return applies.size();
    }

    private SubmittedApplyDetailResponse toSubmittedApplyDetailResponse(final Apply apply) {
        ApplicationForm submittedApplicationForm = apply.getApplicationForm();
        Map<String, String> content = extractContent(submittedApplicationForm);
        List<ApplyPortfolioDto> portfolios = extractPortfolios(submittedApplicationForm);
        return SubmittedApplyDetailResponse.from(apply, content, portfolios);
    }

    private SubmittedApplyResponse toSubmittedApplyResponse(final Apply apply) {
        ApplicationForm applicationForm = apply.getApplicationForm();
        Map<String, String> content = extractContent(applicationForm);
        List<ApplyPortfolioDto> portfolios = extractPortfolios(applicationForm);
        return SubmittedApplyResponse.from(apply, content, portfolios);
    }

    private Map<String, String> extractContent(final ApplicationForm applicationForm) {
        return Optional.ofNullable(applicationForm)
                .map(ApplicationForm::getContent)
                .map(string2MapSerializer::serializeAsMap)
                .orElse(Map.of());
    }

    private List<ApplyPortfolioDto> extractPortfolios(final ApplicationForm applicationForm) {
        return Optional.ofNullable(applicationForm)
                .map(ApplicationForm::getPortfolios)
                .orElse(List.of())
                .stream()
                .map(ApplyPortfolioDto::from)
                .toList();
    }

    private void ensureSubmitted(final Apply apply) {
        if (apply.isNotSubmitted()) {
            throw new ApplyException(ApplyErrorCode.NOT_FOUND_SUBMITTED_APPLICATION_FORM);
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

    private void deleteProfileAndApplicationForm(final Apply apply) {
        // 제출된 지원서인지 검증
        ensureSubmitted(apply);

        //  제출된 지원서 제거
        apply.deleteApplicationForm();

        // 프로필 제거
        Member applicant = apply.getMember();
        applicant.deleteProfile();
    }
}
