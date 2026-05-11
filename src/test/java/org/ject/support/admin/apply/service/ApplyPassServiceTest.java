package org.ject.support.admin.apply.service;

import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.recruit.domain.Question;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.ject.support.domain.recruit.domain.Semester;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.List;

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

        Apply apply1 = getApply(1L, recruit, applicant1, getApplicationForm("content"), ApplyStatus.SUBMITTED);
        Apply apply2 = getApply(2L, recruit, applicant2, getApplicationForm("content"), ApplyStatus.SUBMITTED);
        Apply apply3 = getApply(3L, recruit, applicant3, getApplicationForm("content"), ApplyStatus.SUBMITTED);

        when(applyRepository.findAllByIdWithMember(List.of(1L, 2L, 3L))).thenReturn(List.of(apply1, apply2, apply3));

        // when
        int result = applyPassService.passApply(List.of(1L, 2L, 3L));

        // then
        assertThat(result).isEqualTo(3);
        assertThat(applicant1.getRole()).isEqualTo(Role.SEMESTER);
        assertThat(applicant2.getRole()).isEqualTo(Role.SEMESTER);
        assertThat(applicant3.getRole()).isEqualTo(Role.SEMESTER);
        assertThat(applicant1.getMemberType()).isEqualTo(MemberType.SEMESTER);
        assertThat(applicant2.getMemberType()).isEqualTo(MemberType.SEMESTER);
        assertThat(applicant3.getMemberType()).isEqualTo(MemberType.SEMESTER);
    }

    @Test
    void 메이커스_지원자_합격_시_구성원_타입을_메이커스로_변경한다() {
        // given
        Recruit recruit = getActiveRecruit(1L, JobFamily.FE, RecruitType.MAKERS, List.of());
        Member applicant = getApplicant(1L, "applicant@test.com");
        Apply apply = getApply(1L, recruit, applicant, getApplicationForm("content"), ApplyStatus.SUBMITTED);

        when(applyRepository.findAllByIdWithMember(List.of(1L))).thenReturn(List.of(apply));

        // when
        int result = applyPassService.passApply(List.of(1L));

        // then
        assertThat(result).isEqualTo(1);
        assertThat(applicant.getMemberType()).isEqualTo(MemberType.MAKERS);
    }

    @Test
    void 운영_서포터즈_지원자_합격_시_구성원_타입을_운영_서포터즈로_변경한다() {
        // given
        Recruit recruit = getActiveRecruit(1L, JobFamily.SUPPORTER, RecruitType.SUPPORTERS, List.of());
        Member applicant = getApplicant(1L, "applicant@test.com");
        Apply apply = getApply(1L, recruit, applicant, getApplicationForm("content"), ApplyStatus.SUBMITTED);

        when(applyRepository.findAllByIdWithMember(List.of(1L))).thenReturn(List.of(apply));

        // when
        int result = applyPassService.passApply(List.of(1L));

        // then
        assertThat(result).isEqualTo(1);
        assertThat(applicant.getMemberType()).isEqualTo(MemberType.SUPPORTERS);
    }

    @Test
    void 제출되지_않은_지원서_승인_실패() {
        Recruit recruit = getActiveRecruit(1L, JobFamily.BE, List.of());

        Member applicant1 = getApplicant(1L, "applicant1@test.com");
        Member applicant2 = getApplicant(2L, "applicant2@test.com");
        Member applicant3 = getApplicant(3L, "applicant3@test.com");

        Apply apply1 = getApply(1L, recruit, applicant1, getApplicationForm("content"), ApplyStatus.SUBMITTED);
        Apply apply2 = getApply(2L, recruit, applicant2, getApplicationForm("content"), ApplyStatus.TEMP_SAVED);
        Apply apply3 = getApply(3L, recruit, applicant3, getApplicationForm("content"), ApplyStatus.SUBMITTED);

        when(applyRepository.findAllByIdWithMember(List.of(1L, 2L, 3L))).thenReturn(List.of(apply1, apply2, apply3));

        // when, then
        assertThatThrownBy(() -> applyPassService.passApply(List.of(1L, 2L, 3L)))
                .isInstanceOf(ApplyException.class);
    }

    private Recruit getActiveRecruit(Long id, JobFamily jobFamily, List<Question> questions) {
        return getActiveRecruit(id, jobFamily, RecruitType.SEMESTER, questions);
    }

    private Recruit getActiveRecruit(Long id, JobFamily jobFamily, RecruitType recruitType, List<Question> questions) {
        return Recruit.builder()
                .id(id)
                .semester(Semester.builder().id(1L).name("1기").build())
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .jobFamily(jobFamily)
                .recruitType(recruitType)
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

    private Apply getApply(Long id, Recruit recruit, Member applicant, ApplicationForm applicationForm, ApplyStatus status) {
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
