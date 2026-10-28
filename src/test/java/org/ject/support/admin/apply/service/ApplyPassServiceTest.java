package org.ject.support.admin.apply.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.List;
import org.ject.support.admin.apply.dto.ApplyPassRequest;
import org.ject.support.admin.apply.repository.AdminApplyRepository;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.recruit.domain.Recruit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class ApplyPassServiceTest extends UnitTestSupport {

    @Mock
    private AdminApplyRepository repository;
    private ApplyPassService service;

    @BeforeEach
    void setUp() {
        service = new ApplyPassService(new AdminApplyService(repository, null, null, null));
    }

    @Test
    void 합격해도_지원자_권한과_구성원_유형은_유지한다() {
        // given
        Applicant applicant = Applicant.builder().id(1L).role(Role.APPLY).memberType(MemberType.SUPPORTERS).build();
        Apply apply = Apply.builder().id(1L).recruit(Recruit.builder().id(1L).build())
                .applicant(applicant).status(ApplyStatus.SUBMITTED)
                .applicationForm(ApplicationForm.builder().build()).build();
        apply.decideSelectionResult(SelectionResult.WAITLISTED, 1);
        Apply remaining = Apply.builder().id(2L).recruit(apply.getRecruit()).status(ApplyStatus.SUBMITTED)
                .applicationForm(ApplicationForm.builder().build()).build();
        remaining.decideSelectionResult(SelectionResult.WAITLISTED, 2);
        given(repository.findRecruitIdsByApplyIds(List.of(1L))).willReturn(List.of(1L));
        given(repository.findAppliesInRecruitWithApplicant(1L, List.of(1L))).willReturn(List.of(apply));
        given(repository.findWaitlistedApplies(1L)).willReturn(List.of(apply, remaining));

        // when
        service.passApply(new ApplyPassRequest(List.of(1L)));

        // then
        assertThat(apply.getSelectionResult()).isEqualTo(SelectionResult.PASSED);
        assertThat(apply.getWaitlistNumber()).isNull();
        assertThat(remaining.getWaitlistNumber()).isEqualTo(1);
        assertThat(applicant.getRole()).isEqualTo(Role.APPLY);
        assertThat(applicant.getMemberType()).isEqualTo(MemberType.SUPPORTERS);
    }

    @Test
    void 임시저장_지원서가_섞이면_합격_처리하지_않는다() {
        // given
        Apply submitted = Apply.builder().id(1L).status(ApplyStatus.SUBMITTED)
                .applicationForm(ApplicationForm.builder().build()).build();
        Apply tempSaved = Apply.builder().id(2L).status(ApplyStatus.TEMP_SAVED)
                .applicationForm(ApplicationForm.builder().build()).build();
        given(repository.findRecruitIdsByApplyIds(List.of(1L, 2L))).willReturn(List.of(1L));
        given(repository.findAppliesInRecruitWithApplicant(1L, List.of(1L, 2L)))
                .willReturn(List.of(submitted, tempSaved));

        // when, then
        assertThatThrownBy(() -> service.passApply(new ApplyPassRequest(List.of(1L, 2L))))
                .isInstanceOf(ApplyException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApplyErrorCode.NOT_SUBMITTED);
        assertThat(submitted.getSelectionResult()).isEqualTo(SelectionResult.UNDECIDED);
    }
}
