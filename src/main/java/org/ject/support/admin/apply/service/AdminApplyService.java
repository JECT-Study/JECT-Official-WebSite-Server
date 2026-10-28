package org.ject.support.admin.apply.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.apply.dto.AdminApplyDetailResponse;
import org.ject.support.admin.apply.dto.EditApplyRequest;
import org.ject.support.admin.apply.dto.EditWaitlistOrderRequest;
import org.ject.support.admin.apply.dto.GetAppliesRequest;
import org.ject.support.admin.apply.dto.SubmittedApplyBulkDeleteRequest;
import org.ject.support.admin.apply.dto.SubmittedApplyProjection;
import org.ject.support.admin.apply.dto.SubmittedApplyResponse;
import org.ject.support.admin.apply.dto.SubmittedApplySearchCondition;
import org.ject.support.admin.apply.dto.TempSavedApplyProjection;
import org.ject.support.admin.apply.dto.TempSavedApplyResponse;
import org.ject.support.admin.apply.dto.TempSavedApplySearchCondition;
import org.ject.support.admin.apply.dto.UpdateSelectionResultsRequest;
import org.ject.support.admin.apply.dto.WaitlistOrderResponse;
import org.ject.support.admin.apply.dto.WaitlistOrdersResponse;
import org.ject.support.admin.apply.repository.AdminApplyRepository;
import org.ject.support.common.response.CursorPageResponse;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.common.util.String2MapSerializer;
import org.ject.support.domain.applicant.dto.DeleteApplicantsRequest;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.applicant.entity.ApplicantEditor;
import org.ject.support.domain.applicant.service.ApplicantCommandService;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.exception.QuestionErrorCode;
import org.ject.support.domain.recruit.exception.QuestionException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminApplyService {

    private final AdminApplyRepository adminApplyRepository;
    private final ApplicantCommandService applicantCommandService;
    private final Map2JsonSerializer map2JsonSerializer;
    private final String2MapSerializer string2MapSerializer;

    // 공고별 제출완료 지원서를 선정 결과와 지원자 신분으로 조회
    @Transactional(readOnly = true)
    public CursorPageResponse<SubmittedApplyResponse> searchSubmittedApplies(
            SubmittedApplySearchCondition condition
    ) {
        int size = condition.getSizeOrDefault();
        List<SubmittedApplyProjection> queriedApplies =
                adminApplyRepository.searchSubmittedApplies(condition, size + 1);
        boolean hasNext = queriedApplies.size() > size;
        List<SubmittedApplyProjection> pageContent = hasNext
                ? queriedApplies.subList(0, size)
                : queriedApplies;
        Long nextCursor = hasNext && !pageContent.isEmpty()
                ? pageContent.get(pageContent.size() - 1).applyId()
                : null;
        List<SubmittedApplyResponse> content = pageContent.stream()
                .map(SubmittedApplyResponse::from)
                .toList();
        long totalCount = adminApplyRepository.countSubmittedApplies(condition);

        return CursorPageResponse.of(content, size, hasNext, nextCursor, totalCount);
    }

    // 공고별 임시저장 지원서를 지원자 신분으로 조회
    @Transactional(readOnly = true)
    public CursorPageResponse<TempSavedApplyResponse> searchTempSavedApplies(
            TempSavedApplySearchCondition condition
    ) {
        int size = condition.getSizeOrDefault();
        List<TempSavedApplyProjection> queriedApplies =
                adminApplyRepository.searchTempSavedApplies(condition, size + 1);
        boolean hasNext = queriedApplies.size() > size;
        List<TempSavedApplyProjection> pageContent = hasNext
                ? queriedApplies.subList(0, size)
                : queriedApplies;
        Long nextCursor = hasNext && !pageContent.isEmpty()
                ? pageContent.get(pageContent.size() - 1).applyId()
                : null;
        List<TempSavedApplyResponse> content = pageContent.stream()
                .map(TempSavedApplyResponse::from)
                .toList();
        long totalCount = adminApplyRepository.countTempSavedApplies(condition);

        return CursorPageResponse.of(content, size, hasNext, nextCursor, totalCount);
    }

    @Transactional(readOnly = true)
    public AdminApplyDetailResponse findApply(final Long applyId,
                                              final ApplyStatus applyStatus) {
        return adminApplyRepository.findApplyByIdByStatus(applyId, applyStatus)
                .map(apply -> AdminApplyDetailResponse.from(apply, getAnswers(apply)))
                .orElseThrow(() -> new ApplyException(ApplyErrorCode.NOT_FOUND_APPLY));
    }

    // 지원 상태와 공고 정보를 유지하며 개인정보·선정 결과·지원서 내용을 함께 편집
    @Transactional
    public void editApply(Long applyId, EditApplyRequest request) {
        Apply apply = getApplyForEdit(applyId);
        apply.validateEditable();
        if (apply.isSubmitted() || request.selectionResult() != null) {
            editSelectionResult(apply, request.selectionResult());
        }

        Applicant applicant = apply.getApplicant();
        ApplicantEditor applicantEditor = applicant.toEditor()
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                .email(request.email())
                .careerDetails(request.careerDetails())
                .region(request.region())
                .experiencePeriod(request.experiencePeriod())
                .interestedDomains(request.interestedDomains())
                .build();
        applicant.edit(applicantEditor);
        apply.editNote(request.note());

        validateQuestions(request.answers(), apply.getRecruit());
        String newContent = map2JsonSerializer.serializeAsString(request.answers());
        ApplicationForm applicationForm = apply.getApplicationForm();
        applicationForm.editContent(newContent);
        if (request.deletePortfolioIds() != null && !request.deletePortfolioIds().isEmpty()) {
            applicationForm.deletePortfolios(request.deletePortfolioIds());
        }
    }

    // 지원서 삭제 후 연결된 지원자를 같은 트랜잭션에서 소프트 삭제
    @Transactional
    public void deleteApply(final Long applyId) {
        Apply apply = getApplyForEdit(applyId);
        Long recruitId = apply.getRecruit().getId();
        adminApplyRepository.delete(apply);
        applicantCommandService.deleteApplicant(apply.getApplicant().getId());
        adminApplyRepository.flush();
        reorderWaitlist(recruitId);
    }

    // 선택한 지원서와 연결된 지원자를 같은 트랜잭션에서 소프트 삭제
    @Transactional
    public int deleteApplies(final SubmittedApplyBulkDeleteRequest request) {
        List<Long> applyIds = request.applyIds();
        if (applyIds.stream().distinct().count() != applyIds.size()) {
            throw new ApplyException(ApplyErrorCode.DUPLICATE_APPLY_ID);
        }
        List<Apply> applies = getAppliesForDeletion(new GetAppliesRequest(request.applyIds()));
        if (applies.isEmpty()) {
            return 0;
        }
        Set<Long> applicantIds = applies.stream()
                .map(apply -> apply.getApplicant().getId())
                .collect(Collectors.toSet());
        adminApplyRepository.deleteAll(applies);
        applicantCommandService.deleteApplicants(new DeleteApplicantsRequest(applicantIds));
        adminApplyRepository.flush();
        applies.stream().map(a -> a.getRecruit().getId()).distinct().sorted()
                .forEach(this::reorderWaitlist);
        return applies.size();
    }

    // 공고별 예비 합격자 전체와 조회된 인원수 반환
    @Transactional(readOnly = true)
    public WaitlistOrdersResponse getWaitlistOrders(Long recruitId) {
        List<WaitlistOrderResponse> content = adminApplyRepository.getWaitlistOrders(recruitId);
        return WaitlistOrdersResponse.from(content);
    }

    // 삭제되지 않은 지원서와 지원자를 함께 조회
    private Apply getApplyForEdit(Long applyId) {
        return adminApplyRepository.findAppliesWithApplicant(List.of(applyId)).stream().findFirst()
                .orElseThrow(() -> new ApplyException(ApplyErrorCode.NOT_FOUND_APPLY));
    }

    // 삭제되지 않은 일괄 삭제 대상 조회
    private List<Apply> getAppliesForDeletion(GetAppliesRequest request) {
        return adminApplyRepository.findAppliesWithApplicant(request.applyIds());
    }

    // 기존 합격 처리 요청의 지원서가 같은 공고에 속하는지 검증
    @Transactional
    public List<Apply> getAppliesForPass(GetAppliesRequest request) {
        if (new HashSet<>(request.applyIds()).size() != request.applyIds().size()) {
            throw new ApplyException(ApplyErrorCode.DUPLICATE_APPLY_ID);
        }
        List<Long> recruitIds = adminApplyRepository.findRecruitIdsByApplyIds(request.applyIds());
        if (recruitIds.isEmpty()) {
            throw new ApplyException(ApplyErrorCode.NOT_FOUND_APPLY);
        }
        if (recruitIds.size() != 1) {
            throw new ApplyException(ApplyErrorCode.DIFFERENT_RECRUIT_NOT_ALLOWED);
        }
        Long recruitId = recruitIds.getFirst();
        List<Apply> applies = adminApplyRepository.findAppliesInRecruitWithApplicant(recruitId, request.applyIds());
        if (applies.size() != request.applyIds().size()) {
            throw new ApplyException(ApplyErrorCode.NOT_FOUND_APPLY);
        }
        return applies;
    }

    // 선택한 제출완료 지원자의 결과를 공통 적용하고 예비 합격 순서를 연속 번호로 유지
    @Transactional
    public void updateSelectionResults(UpdateSelectionResultsRequest request) {
        if (new HashSet<>(request.applyIds()).size() != request.applyIds().size()) {
            throw new ApplyException(ApplyErrorCode.DUPLICATE_APPLY_ID);
        }
        List<Apply> targets = adminApplyRepository.findAppliesInRecruitWithApplicant(
                request.recruitId(), request.applyIds());
        if (targets.size() != request.applyIds().size()) {
            throw new ApplyException(ApplyErrorCode.NOT_FOUND_APPLY);
        }
        replaceSelectionResults(targets, request);
    }

    // 상세 편집의 미선정 초기화를 포함하여 동일한 예비 번호 정책 적용
    private void editSelectionResult(Apply apply, SelectionResult result) {
        if (!apply.isSubmitted() || result == null) {
            apply.editSelectionResult(result);
            return;
        }
        replaceSelectionResults(List.of(apply), new UpdateSelectionResultsRequest(
                apply.getRecruit().getId(), List.of(apply.getId()), result));
    }

    // 예비 합격자 전체 목록을 요청 순서로 저장하여 누락과 중복 차단
    @Transactional
    public int editWaitlistOrder(EditWaitlistOrderRequest request) {
        List<Apply> waitlisted = adminApplyRepository.findWaitlistedApplies(request.recruitId());
        Set<Long> requestedIds = new HashSet<>(request.applyIds());
        Set<Long> existingIds = waitlisted.stream().map(Apply::getId).collect(Collectors.toSet());
        if (requestedIds.size() != request.applyIds().size()) {
            throw new ApplyException(ApplyErrorCode.DUPLICATE_APPLY_ID);
        }
        if (!existingIds.equals(requestedIds)) {
            throw new ApplyException(ApplyErrorCode.INVALID_WAITLIST_ORDER);
        }
        Map<Long, Apply> byId = waitlisted.stream().collect(Collectors.toMap(Apply::getId, Function.identity()));
        saveWaitlistOrder(request.applyIds().stream().map(byId::get).toList());
        return waitlisted.size();
    }

    // 삭제와 합격 처리 이후 남은 예비 합격자의 빈 번호를 순서대로 정리
    private void reorderWaitlist(Long recruitId) {
        saveWaitlistOrder(adminApplyRepository.findWaitlistedApplies(recruitId));
    }

    // 선정 결과 변경에 맞춰 예비 합격자 추가와 제외를 반영하고 연속 번호 부여
    private void replaceSelectionResults(List<Apply> targets, UpdateSelectionResultsRequest request) {
        List<Apply> numbered = adminApplyRepository.findWaitlistedApplies(request.recruitId());
        List<Apply> ordered = new ArrayList<>(numbered);
        Map<Long, Apply> byId = targets.stream().collect(Collectors.toMap(Apply::getId, Function.identity()));
        for (Apply target : targets) {
            if (target.isNotSubmitted()) {
                throw new ApplyException(ApplyErrorCode.NOT_SUBMITTED);
            }
        }
        if (request.selectionResult() == SelectionResult.WAITLISTED) {
            for (Long id : request.applyIds()) {
                Apply target = byId.get(id);
                if (target.getSelectionResult() != SelectionResult.WAITLISTED) {
                    ordered.add(target);
                }
            }
        } else {
            Set<Long> targetIds = byId.keySet();
            ordered.removeIf(a -> targetIds.contains(a.getId()));
        }
        // 기존 번호를 먼저 해제해 번호 당김과 순서 교환 시 유니크 충돌 방지
        numbered.forEach(Apply::clearWaitlistNumber);
        adminApplyRepository.flush();
        if (request.selectionResult() != SelectionResult.WAITLISTED) {
            targets.forEach(a -> a.decideSelectionResult(request.selectionResult(), null));
        }
        for (int i = 0; i < ordered.size(); i++) {
            ordered.get(i).decideSelectionResult(SelectionResult.WAITLISTED, i + 1);
        }
        adminApplyRepository.flush();
    }

    // 기존 번호 해제 후 요청 순서대로 번호를 부여하여 유니크 충돌 방지
    private void saveWaitlistOrder(List<Apply> ordered) {
        ordered.forEach(Apply::clearWaitlistNumber);
        adminApplyRepository.flush();
        for (int i = 0; i < ordered.size(); i++) {
            ordered.get(i).decideSelectionResult(SelectionResult.WAITLISTED, i + 1);
        }
        adminApplyRepository.flush();
    }

    private void validateQuestions(final Map<String, String> answers, final Recruit recruit) {
        answers.keySet().stream()
                .map(Long::parseLong)
                .filter(recruit::isInvalidQuestionId)
                .findAny()
                .ifPresent(key -> {
                    throw new QuestionException(QuestionErrorCode.NOT_FOUND_QUESTION);
                });
    }

    private Map<String, String> getAnswers(final Apply apply) {
        ApplicationForm applicationForm = apply.getApplicationForm();
        if (applicationForm == null) {
            return Map.of();
        }
        return Optional.ofNullable(string2MapSerializer.serializeAsMap(applicationForm.getContent()))
                .orElse(Map.of());
    }
}
