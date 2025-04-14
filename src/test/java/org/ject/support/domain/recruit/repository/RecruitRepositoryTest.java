package org.ject.support.domain.recruit.repository;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.ject.support.domain.member.JobFamily.BE;
import static org.ject.support.domain.member.JobFamily.FE;
import static org.ject.support.domain.member.JobFamily.PM;

@Import(QueryDslTestConfig.class)
@DataJpaTest
class RecruitRepositoryTest {

    @Autowired
    private RecruitRepository recruitRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Test
    @DisplayName("특정 직군의 마감되지 않은 모집 정보가 존재하면 true 반환")
    void exists_by_job_family_and_is_not_closed() {
        // given
        semesterRepository.save(Semester.builder().name("3기").isRecruiting(true).build());

        recruitRepository.saveAll(
                List.of(
                        Recruit.builder()
                                .semesterId(1L)
                                .startDate(LocalDateTime.now().minusDays(1))
                                .endDate(LocalDateTime.now().plusDays(1))
                                .jobFamily(PM)
                                .build(),
                        Recruit.builder()
                                .semesterId(1L)
                                .startDate(LocalDateTime.now().minusDays(1))
                                .endDate(LocalDateTime.now().plusDays(1))
                                .jobFamily(BE)
                                .build()
                )
        );

        // when
        boolean result = recruitRepository.existsByJobFamilyAndIsNotClosed(1L, List.of(FE, BE));

        // then
        assertThat(result).isTrue();
    }

}