package org.ject.support.admin.apply.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.List;
import org.ject.support.admin.apply.dto.EditWaitlistOrderRequest;
import org.ject.support.admin.apply.dto.GetAppliesRequest;
import org.ject.support.admin.apply.dto.UpdateSelectionResultsRequest;
import org.ject.support.admin.apply.dto.WaitlistOrderResponse;
import org.ject.support.admin.apply.dto.WaitlistOrderSearchCondition;
import org.ject.support.admin.apply.repository.AdminApplyRepository;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.recruit.domain.Recruit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class AdminApplySelectionServiceTest extends UnitTestSupport {

    @InjectMocks
    private AdminApplyService service;
    @Mock
    private AdminApplyRepository repository;

    @ParameterizedTest
    @EnumSource(value = SelectionResult.class, names = {"PASSED", "FAILED"})
    void 선택한_지원서에_동일한_선정결과를_지정한다(SelectionResult result) {
        // given
        Apply first = apply(1L);
        Apply second = apply(2L);
        given(repository.findAppliesInRecruitWithApplicant(1L, List.of(1L, 2L)))
                .willReturn(List.of(first, second));

        // when
        service.updateSelectionResults(new UpdateSelectionResultsRequest(1L, List.of(1L, 2L), result));

        // then
        assertThat(List.of(first, second)).extracting(Apply::getSelectionResult).containsOnly(result);
        assertThat(first.getWaitlistNumber()).isNull();
        assertThat(second.getWaitlistNumber()).isNull();
    }

    @Test
    void 새_예비합격자는_요청_순서대로_기존_예비번호_뒤에_추가한다() {
        // given
        Apply existing = apply(1L);
        existing.decideSelectionResult(SelectionResult.WAITLISTED, 1);
        Apply passed = apply(2L);
        passed.decideSelectionResult(SelectionResult.PASSED, null);
        Apply failed = apply(3L);
        failed.decideSelectionResult(SelectionResult.FAILED, null);
        given(repository.findAppliesInRecruitWithApplicant(1L, List.of(3L, 2L)))
                .willReturn(List.of(passed, failed));
        given(repository.findWaitlistedApplies(1L)).willReturn(List.of(existing));

        // when
        service.updateSelectionResults(new UpdateSelectionResultsRequest(1L, List.of(3L, 2L), SelectionResult.WAITLISTED));

        // then
        assertThat(List.of(existing, failed, passed)).extracting(Apply::getWaitlistNumber).containsExactly(1, 2, 3);
        assertThat(List.of(failed, passed)).extracting(Apply::getSelectionResult)
                .containsOnly(SelectionResult.WAITLISTED);
    }

    @Test
    void 기존_예비합격자를_다시_지정하면_순서를_유지한다() {
        // given
        Apply first = apply(1L);
        Apply second = apply(2L);
        first.decideSelectionResult(SelectionResult.WAITLISTED, 1);
        second.decideSelectionResult(SelectionResult.WAITLISTED, 2);
        given(repository.findAppliesInRecruitWithApplicant(1L, List.of(2L, 1L))).willReturn(List.of(first, second));
        given(repository.findWaitlistedApplies(1L)).willReturn(List.of(first, second));

        // when
        service.updateSelectionResults(new UpdateSelectionResultsRequest(1L, List.of(2L, 1L), SelectionResult.WAITLISTED));

        // then
        assertThat(List.of(first, second)).extracting(Apply::getWaitlistNumber).containsExactly(1, 2);
    }

    @ParameterizedTest
    @EnumSource(value = SelectionResult.class, names = {"PASSED", "FAILED"})
    void 예비합격을_해제하면_번호를_비우고_남은_순번을_당긴다(SelectionResult result) {
        // given
        Apply first = apply(1L);
        Apply second = apply(2L);
        Apply third = apply(3L);
        first.decideSelectionResult(SelectionResult.WAITLISTED, 1);
        second.decideSelectionResult(SelectionResult.WAITLISTED, 2);
        third.decideSelectionResult(SelectionResult.WAITLISTED, 3);
        given(repository.findAppliesInRecruitWithApplicant(1L, List.of(2L))).willReturn(List.of(second));
        given(repository.findWaitlistedApplies(1L)).willReturn(List.of(first, second, third));

        // when
        service.updateSelectionResults(new UpdateSelectionResultsRequest(1L, List.of(2L), result));

        // then
        assertThat(second.getSelectionResult()).isEqualTo(result);
        assertThat(second.getWaitlistNumber()).isNull();
        assertThat(List.of(first, third)).extracting(Apply::getWaitlistNumber).containsExactly(1, 2);
    }

    @Test
    void 공고에_없는_지원서가_포함되면_선정결과를_변경하지_않는다() {
        // given
        Apply first = apply(1L);
        given(repository.findAppliesInRecruitWithApplicant(1L, List.of(1L, 99L))).willReturn(List.of(first));

        // when, then
        assertThatThrownBy(() -> service.updateSelectionResults(
                new UpdateSelectionResultsRequest(1L, List.of(1L, 99L), SelectionResult.PASSED)))
                .isInstanceOf(ApplyException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApplyErrorCode.NOT_FOUND_APPLY);
        assertThat(first.getSelectionResult()).isEqualTo(SelectionResult.UNDECIDED);
    }

    @Test
    void 중복된_지원서는_선정결과를_지정할_수_없다() {
        // when, then
        assertThatThrownBy(() -> service.updateSelectionResults(
                new UpdateSelectionResultsRequest(1L, List.of(1L, 1L), SelectionResult.PASSED)))
                .isInstanceOf(ApplyException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApplyErrorCode.DUPLICATE_APPLY_ID);
    }

    @Test
    void 임시저장_지원서가_포함되면_선정결과를_변경하지_않는다() {
        // given
        Apply submitted = apply(1L);
        Apply tempSaved = apply(2L);
        tempSaved.saveTemporarily();
        given(repository.findAppliesInRecruitWithApplicant(1L, List.of(1L, 2L)))
                .willReturn(List.of(submitted, tempSaved));

        // when, then
        assertThatThrownBy(() -> service.updateSelectionResults(
                new UpdateSelectionResultsRequest(1L, List.of(1L, 2L), SelectionResult.PASSED)))
                .isInstanceOf(ApplyException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApplyErrorCode.NOT_SUBMITTED);
        assertThat(submitted.getSelectionResult()).isEqualTo(SelectionResult.UNDECIDED);
    }

    @Test
    void 예비합격자_전체를_요청한_순서대로_재배치한다() {
        // given
        Apply first = apply(1L);
        Apply second = apply(2L);
        first.decideSelectionResult(SelectionResult.WAITLISTED, 1);
        second.decideSelectionResult(SelectionResult.WAITLISTED, 2);
        given(repository.findWaitlistedApplies(1L)).willReturn(List.of(first, second));

        // when
        service.editWaitlistOrder(new EditWaitlistOrderRequest(1L, List.of(2L, 1L)));

        // then
        assertThat(List.of(second, first)).extracting(Apply::getWaitlistNumber).containsExactly(1, 2);
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 99L})
    void 예비합격자가_누락되거나_다른_대상이_포함되면_순서를_유지한다(Long id) {
        // given
        Apply first = apply(1L);
        Apply second = apply(2L);
        first.decideSelectionResult(SelectionResult.WAITLISTED, 1);
        second.decideSelectionResult(SelectionResult.WAITLISTED, 2);
        given(repository.findWaitlistedApplies(1L)).willReturn(List.of(first, second));

        // when, then
        assertThatThrownBy(() -> service.editWaitlistOrder(new EditWaitlistOrderRequest(1L, List.of(id))))
                .isInstanceOf(ApplyException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApplyErrorCode.INVALID_WAITLIST_ORDER);
        assertThat(List.of(first, second)).extracting(Apply::getWaitlistNumber).containsExactly(1, 2);
    }

    @Test
    void 예비번호_조정에_중복된_지원서를_넣을_수_없다() {
        // when, then
        assertThatThrownBy(() -> service.editWaitlistOrder(new EditWaitlistOrderRequest(1L, List.of(1L, 1L))))
                .isInstanceOf(ApplyException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApplyErrorCode.DUPLICATE_APPLY_ID);
    }

    @Test
    void 예비목록의_다음_커서는_마지막_예비번호이고_전체_인원은_유지한다() {
        // given
        var condition = new WaitlistOrderSearchCondition(1L, null, 2);
        given(repository.searchWaitlistOrders(condition, 3)).willReturn(List.of(
                response(30L, 1), response(10L, 2), response(20L, 3)));
        given(repository.countWaitlistOrders(1L)).willReturn(3L);

        // when
        var result = service.searchWaitlistOrders(condition);

        // then
        assertThat(result.content()).extracting(r -> r.applyId()).containsExactly(30L, 10L);
        assertThat(result.nextCursor()).isEqualTo(2L);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.totalCount()).isEqualTo(3L);
    }

    @Test
    void 예비목록의_마지막_페이지는_다음_커서가_없다() {
        // given
        var condition = new WaitlistOrderSearchCondition(1L, 2L, 2);
        given(repository.searchWaitlistOrders(condition, 3)).willReturn(List.of(response(20L, 3)));
        given(repository.countWaitlistOrders(1L)).willReturn(3L);

        // when
        var result = service.searchWaitlistOrders(condition);

        // then
        assertThat(result.content()).extracting(r -> r.applyId()).containsExactly(20L);
        assertThat(result.nextCursor()).isNull();
        assertThat(result.hasNext()).isFalse();
        assertThat(result.totalCount()).isEqualTo(3L);
    }

    @Test
    void 기존_합격_요청에서도_다른_공고를_함께_선택할_수_없다() {
        // given
        given(repository.findRecruitIdsByApplyIds(List.of(1L, 2L))).willReturn(List.of(1L, 2L));

        // when, then
        assertThatThrownBy(() -> service.getAppliesForPass(new GetAppliesRequest(List.of(1L, 2L))))
                .isInstanceOf(ApplyException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApplyErrorCode.DIFFERENT_RECRUIT_NOT_ALLOWED);
    }

    private Apply apply(Long id) {
        return Apply.builder().id(id).status(ApplyStatus.SUBMITTED)
                .applicationForm(ApplicationForm.builder().build())
                .recruit(Recruit.builder().id(1L).build()).applicant(Applicant.builder().id(id).build()).build();
    }

    private WaitlistOrderResponse response(Long id, int number) {
        return new WaitlistOrderResponse(id, "지원자", "01012345678", CareerDetails.STUDENT, number);
    }
}
