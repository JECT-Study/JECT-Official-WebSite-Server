package org.ject.support.domain.recruit.repository;

import org.assertj.core.api.Assertions;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.domain.recruit.domain.ApplicationForm;
import org.ject.support.domain.recruit.domain.Recruit;
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
    MemberRepository memberRepository;

    @Autowired
    ApplicationFormRepository applicationFormRepository;

    @Test
    @DisplayName("지원서 제출 여부 확인")
    void check_apply_submit() {
        // given
        Recruit recruit = recruitRepository.save(Recruit.builder()
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .semesterId(1L)
                .jobFamily(JobFamily.BE)
                .build());

        Member member = memberRepository.save(Member.builder()
                .email("test32@gmail.com")
                .jobFamily(JobFamily.BE)
                .name("김젝트")
                .role(Role.CORE)
                .phoneNumber("01012345678")
                .semesterId(1L)
                .pin("123456") // PIN 필드 추가
                .status(MemberStatus.ACTIVE)
                .build());

        applicationFormRepository.save(ApplicationForm.builder().
                content("content")
                .member(member)
                .recruit(recruit)
                .portfolios(List.of())
                .build());

        // when
        boolean result = applicationFormRepository.existsByMemberId(member.getId(), LocalDateTime.now());

        // then
        Assertions.assertThat(result).isTrue();
    }

}