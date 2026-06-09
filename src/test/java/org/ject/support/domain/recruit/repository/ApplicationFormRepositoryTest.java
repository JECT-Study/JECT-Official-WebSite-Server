package org.ject.support.domain.recruit.repository;

import org.assertj.core.api.Assertions;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.repository.ApplicationFormRepository;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.applicant.repository.ApplicantRepository;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

@Import(QueryDslTestConfig.class)
@DataJpaTest
class ApplicationFormRepositoryTest {

    @Autowired
    RecruitRepository recruitRepository;

    @Autowired
    ApplicantRepository applicantRepository;

    @Autowired
    ApplyRepository applyRepository;

    @Autowired
    ApplicationFormRepository applicationFormRepository;

    @Autowired
    SemesterRepository semesterRepository;

    @Test
    @DisplayName("지원서 제출 여부 확인")
    void check_apply_submit() {
        // given
        Semester savedSemester = semesterRepository.save(Semester.builder()
                .name("1기")
                .isRecruiting(true)
                .build());

        Recruit recruit = recruitRepository.save(Recruit.builder()
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .semester(savedSemester)
                .jobFamily(JobFamily.BE)
                .build());

        Applicant applicant = applicantRepository.save(Applicant.builder()
                .email("test32@gmail.com")
                .semesterId(savedSemester.getId())
                .jobFamily(JobFamily.BE)
                .name("김젝트")
                .role(Role.SEMESTER)
                .phoneNumber("01012345678")
                .pin("123456") // PIN 필드 추가
                .status(MemberStatus.ACTIVE)
                .build());

        Apply apply = applyRepository.save(Apply.builder()
                .applicant(applicant)
                .recruit(recruit)
                .status(ApplyStatus.JOINED)
                .build());

        applicationFormRepository.save(ApplicationForm.builder().
                content("content")
                .apply(apply)
                .portfolios(List.of())
                .build());

        // when
        boolean result = applicationFormRepository.existsByApplicantId(applicant.getId(), LocalDateTime.now());

        // then
        Assertions.assertThat(result).isTrue();
    }

}
