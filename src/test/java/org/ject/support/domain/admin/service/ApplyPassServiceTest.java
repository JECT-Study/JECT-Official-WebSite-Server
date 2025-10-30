package org.ject.support.domain.admin.service;

import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.recruit.domain.Question;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class ApplyPassServiceTest extends UnitTestSupport {

    @InjectMocks
    ApplyPassService applyPassService;

    @Mock
    ApplyRepository applyRepository;

    @Test
    void 지원자_합격_처리() {
        // given
        Recruit recruit = getActiveRecruit(1L, JobFamily.BE, List.of());

        Member applicant1 = getApplicant(1L, "applicant1@test.com");
        Member applicant2 = getApplicant(2L, "applicant2@test.com");
        Member applicant3 = getApplicant(3L, "applicant3@test.com");

        Apply apply1 = getApply(1L, recruit, applicant1, getApplicationForm("content"), Apply.Status.SUBMITTED);
        Apply apply2 = getApply(2L, recruit, applicant2, getApplicationForm("content"), Apply.Status.SUBMITTED);
        Apply apply3 = getApply(3L, recruit, applicant3, getApplicationForm("content"), Apply.Status.SUBMITTED);

        when(applyRepository.findById(1L)).thenReturn(Optional.ofNullable(apply1));
        when(applyRepository.findById(2L)).thenReturn(Optional.ofNullable(apply2));
        when(applyRepository.findById(3L)).thenReturn(Optional.ofNullable(apply3));

        // when
        int result = applyPassService.passApply(List.of(1L, 2L, 3L));

        // then
        assertThat(result).isEqualTo(3);
        assertThat(applicant1.getRole()).isEqualTo(Role.SEMESTER);
        assertThat(applicant2.getRole()).isEqualTo(Role.SEMESTER);
        assertThat(applicant3.getRole()).isEqualTo(Role.SEMESTER);
    }

    @Test
    void 존재하지_않는_지원서_승인_실패() {
        Recruit recruit = getActiveRecruit(1L, JobFamily.BE, List.of());

        Member applicant1 = getApplicant(1L, "applicant1@test.com");
        Member applicant2 = getApplicant(2L, "applicant2@test.com");

        Apply apply1 = getApply(1L, recruit, applicant1, getApplicationForm("content"), Apply.Status.SUBMITTED);
        Apply apply2 = getApply(2L, recruit, applicant2, getApplicationForm("content"), Apply.Status.SUBMITTED);

        when(applyRepository.findById(1L)).thenReturn(Optional.ofNullable(apply1));
        when(applyRepository.findById(2L)).thenReturn(Optional.ofNullable(apply2));

        // when, then
        assertThatThrownBy(() -> applyPassService.passApply(List.of(1L, 2L, 3L)))
                .isInstanceOf(ApplyException.class);
    }

    @Test
    void 제출되지_않은_지원서_승인_실패() {
        Recruit recruit = getActiveRecruit(1L, JobFamily.BE, List.of());

        Member applicant1 = getApplicant(1L, "applicant1@test.com");
        Member applicant2 = getApplicant(2L, "applicant2@test.com");
        Member applicant3 = getApplicant(3L, "applicant3@test.com");

        Apply apply1 = getApply(1L, recruit, applicant1, getApplicationForm("content"), Apply.Status.SUBMITTED);
        Apply apply2 = getApply(2L, recruit, applicant2, getApplicationForm("content"), Apply.Status.TEMP_SAVED);
        Apply apply3 = getApply(3L, recruit, applicant3, getApplicationForm("content"), Apply.Status.SUBMITTED);

        when(applyRepository.findById(1L)).thenReturn(Optional.ofNullable(apply1));
        when(applyRepository.findById(2L)).thenReturn(Optional.ofNullable(apply2));
        when(applyRepository.findById(3L)).thenReturn(Optional.ofNullable(apply3));

        // when, then
        assertThatThrownBy(() -> applyPassService.passApply(List.of(1L, 2L, 3L)))
                .isInstanceOf(ApplyException.class);
    }

    private Recruit getActiveRecruit(Long id, JobFamily jobFamily, List<Question> questions) {
        return Recruit.builder()
                .id(id)
                .semester(Semester.builder().id(1L).name("1기").build())
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .jobFamily(jobFamily)
                .questions(questions)
                .build();
    }

    private Member getApplicant(Long id, String email) {
        return Member.builder()
                .id(id)
                .email(email)
                .pin("111111")
                .role(Role.APPLY)
                .status(MemberStatus.ACTIVE)
                .build();
    }

    private Apply getApply(Long id, Recruit recruit, Member applicant, ApplicationForm applicationForm, Apply.Status status) {
        return Apply.builder()
                .id(id)
                .recruit(recruit)
                .member(applicant)
                .applicationForm(applicationForm)
                .status(status)
                .build();
    }

    private ApplicationForm getApplicationForm(String content) {
        return ApplicationForm.builder()
                .id(1L)
                .content(content)
                .build();
    }
}
