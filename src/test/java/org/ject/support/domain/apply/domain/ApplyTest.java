package org.ject.support.domain.apply.domain;

import org.ject.support.base.TestSupport;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.ject.support.domain.apply.domain.ApplyStatus.SUBMITTED;

class ApplyTest extends TestSupport {

    @Test
    void 포트폴리오가_필수가_아닌_직군은_포트폴리오_없이_제출할_수_있다() {
        // given
        Recruit recruit = Recruit.builder()
                .jobFamily(JobFamily.BE)
                .build();
        Apply apply = Apply.createApply(Applicant.builder().build(), recruit);
        ApplicationForm form = ApplicationForm.builder().build();

        // when
        apply.submit(form);

        // then
        assertThat(apply.getStatus()).isEqualTo(SUBMITTED);
        assertThat(apply.getApplicationForm()).isEqualTo(form);
    }

    @Test
    void 포트폴리오가_필수인_직군은_포트폴리오가_있으면_제출할_수_있다() {
        // given
        Recruit recruit = Recruit.builder()
                .jobFamily(JobFamily.PD)
                .build();
        Apply apply = Apply.createApply(Applicant.builder().build(), recruit);
        
        Portfolio portfolio = Portfolio.builder().build();
        ApplicationForm form = ApplicationForm.builder()
                .portfolios(List.of(portfolio))
                .build();

        // when
        apply.submit(form);

        // then
        assertThat(apply.getStatus()).isEqualTo(SUBMITTED);
        assertThat(apply.getApplicationForm()).isEqualTo(form);
    }

    @Test
    void 포트폴리오가_필수인_직군이_포트폴리오_없이_제출하면_예외가_발생한다() {
        // given
        Recruit recruit = Recruit.builder()
                .jobFamily(JobFamily.PD)
                .build();
        Apply apply = Apply.createApply(Applicant.builder().build(), recruit);
        ApplicationForm form = ApplicationForm.builder().build(); // 빈 포트폴리오

        // when & then
        assertThatThrownBy(() -> apply.submit(form))
                .isInstanceOf(ApplyException.class)
                .extracting("errorCode")
                .isEqualTo(ApplyErrorCode.PORTFOLIO_REQUIRED);
    }

    @Test
    void 지원은_기본적으로_선정_결과가_미정이다() {
        // given
        Recruit recruit = Recruit.builder()
                .jobFamily(JobFamily.BE)
                .build();

        // when
        Apply apply = Apply.createApply(Applicant.builder().build(), recruit);

        // then
        assertThat(apply.getSelectionResult()).isEqualTo(SelectionResult.UNDECIDED);
        assertThat(apply.getWaitlistNumber()).isNull();
    }

    @Test
    void 제출된_지원의_선정_결과를_합격으로_정할_수_있다() {
        // given
        Apply apply = submittedApply();

        // when
        apply.decideSelectionResult(SelectionResult.PASSED, null);

        // then
        assertThat(apply.getSelectionResult()).isEqualTo(SelectionResult.PASSED);
        assertThat(apply.getWaitlistNumber()).isNull();
    }

    @Test
    void 선정_결과를_불합격으로_정해도_지원_상태와_지원서는_그대로_유지된다() {
        // given
        Apply apply = submittedApply();
        ApplicationForm form = apply.getApplicationForm();

        // when
        apply.decideSelectionResult(SelectionResult.FAILED, null);

        // then
        assertThat(apply.getSelectionResult()).isEqualTo(SelectionResult.FAILED);
        assertThat(apply.getStatus()).isEqualTo(SUBMITTED);
        assertThat(apply.getApplicationForm()).isEqualTo(form);
    }

    @Test
    void 예비_합격으로_정하면_예비_번호를_함께_가진다() {
        // given
        Apply apply = submittedApply();

        // when
        apply.decideSelectionResult(SelectionResult.WAITLISTED, 3);

        // then
        assertThat(apply.getSelectionResult()).isEqualTo(SelectionResult.WAITLISTED);
        assertThat(apply.getWaitlistNumber()).isEqualTo(3);
    }

    @Test
    void 예비_합격이_아닌_선정_결과로_바꾸면_예비_번호가_사라진다() {
        // given
        Apply apply = submittedApply();
        apply.decideSelectionResult(SelectionResult.WAITLISTED, 3);

        // when
        apply.decideSelectionResult(SelectionResult.PASSED, null);

        // then
        assertThat(apply.getWaitlistNumber()).isNull();
    }

    @Test
    void 예비_번호_없이_예비_합격으로_정하면_예외가_발생한다() {
        // given
        Apply apply = submittedApply();

        // when & then
        assertThatThrownBy(() -> apply.decideSelectionResult(SelectionResult.WAITLISTED, null))
                .isInstanceOf(ApplyException.class)
                .extracting("errorCode")
                .isEqualTo(ApplyErrorCode.WAITLIST_NUMBER_REQUIRED);
    }

    @Test
    void 예비_합격이_아닌데_예비_번호를_지정하면_예외가_발생한다() {
        // given
        Apply apply = submittedApply();

        // when & then
        assertThatThrownBy(() -> apply.decideSelectionResult(SelectionResult.PASSED, 1))
                .isInstanceOf(ApplyException.class)
                .extracting("errorCode")
                .isEqualTo(ApplyErrorCode.WAITLIST_NUMBER_NOT_ALLOWED);
    }

    @Test
    void 예비_번호는_양수여야_한다() {
        // given
        Apply apply = submittedApply();

        // when & then
        assertThatThrownBy(() -> apply.decideSelectionResult(SelectionResult.WAITLISTED, 0))
                .isInstanceOf(ApplyException.class)
                .extracting("errorCode")
                .isEqualTo(ApplyErrorCode.INVALID_WAITLIST_NUMBER);
    }

    @Test
    void 반려된_지원은_선정_결과를_정할_수_없다() {
        // given
        Apply apply = submittedApply();
        apply.reject();

        // when & then
        assertThatThrownBy(() -> apply.decideSelectionResult(SelectionResult.PASSED, null))
                .isInstanceOf(ApplyException.class)
                .extracting("errorCode")
                .isEqualTo(ApplyErrorCode.NOT_SUBMITTED);
    }

    @Test
    void 지원을_반려하면_선정_결과와_예비_번호를_초기화한다() {
        // given
        Apply apply = submittedApply();
        apply.decideSelectionResult(SelectionResult.WAITLISTED, 3);

        // when
        apply.reject();

        // then
        assertThat(apply.getSelectionResult()).isEqualTo(SelectionResult.UNDECIDED);
        assertThat(apply.getWaitlistNumber()).isNull();
    }

    @Test
    void 제출되지_않은_지원은_선정_결과를_정할_수_없다() {
        // given
        Recruit recruit = Recruit.builder()
                .jobFamily(JobFamily.BE)
                .build();
        Apply apply = Apply.createApply(Applicant.builder().build(), recruit);

        // when & then
        assertThatThrownBy(() -> apply.decideSelectionResult(SelectionResult.PASSED, null))
                .isInstanceOf(ApplyException.class)
                .extracting("errorCode")
                .isEqualTo(ApplyErrorCode.NOT_SUBMITTED);
    }

    private Apply submittedApply() {
        Recruit recruit = Recruit.builder()
                .jobFamily(JobFamily.BE)
                .build();
        Apply apply = Apply.createApply(Applicant.builder().build(), recruit);
        apply.submit(ApplicationForm.builder().build());
        return apply;
    }

    @Test
    void 이미_제출된_지원서는_다시_제출할_수_없다() {
        // given
        Recruit recruit = Recruit.builder()
                .jobFamily(JobFamily.BE)
                .build();
        Apply apply = Apply.createApply(Applicant.builder().build(), recruit);
        apply.updateStatus(SUBMITTED); // 이미 제출된 상태

        ApplicationForm form = ApplicationForm.builder().build();

        // when & then
        assertThatThrownBy(() -> apply.submit(form))
                .isInstanceOf(ApplyException.class)
                .extracting("errorCode")
                .isEqualTo(ApplyErrorCode.ALREADY_SUBMITTED);
    }
}
