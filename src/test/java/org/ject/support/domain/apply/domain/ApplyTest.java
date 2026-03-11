package org.ject.support.domain.apply.domain;

import org.ject.support.base.TestSupport;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.entity.Member;
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
        Apply apply = Apply.createApply(Member.builder().build(), recruit);
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
        Apply apply = Apply.createApply(Member.builder().build(), recruit);
        
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
        Apply apply = Apply.createApply(Member.builder().build(), recruit);
        ApplicationForm form = ApplicationForm.builder().build(); // 빈 포트폴리오

        // when & then
        assertThatThrownBy(() -> apply.submit(form))
                .isInstanceOf(ApplyException.class)
                .extracting("errorCode")
                .isEqualTo(ApplyErrorCode.PORTFOLIO_REQUIRED);
    }

    @Test
    void 이미_제출된_지원서는_다시_제출할_수_없다() {
        // given
        Recruit recruit = Recruit.builder()
                .jobFamily(JobFamily.BE)
                .build();
        Apply apply = Apply.createApply(Member.builder().build(), recruit);
        apply.updateStatus(SUBMITTED); // 이미 제출된 상태

        ApplicationForm form = ApplicationForm.builder().build();

        // when & then
        assertThatThrownBy(() -> apply.submit(form))
                .isInstanceOf(ApplyException.class)
                .extracting("errorCode")
                .isEqualTo(ApplyErrorCode.ALREADY_SUBMITTED);
    }
}
