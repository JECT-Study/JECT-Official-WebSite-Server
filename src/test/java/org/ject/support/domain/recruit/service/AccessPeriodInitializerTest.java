package org.ject.support.domain.recruit.service;

import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.dto.Constants;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.ject.support.testconfig.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ject.support.domain.member.JobFamily.BE;
import static org.ject.support.domain.member.JobFamily.FE;
import static org.ject.support.domain.member.JobFamily.PD;
import static org.ject.support.domain.member.JobFamily.PM;

@IntegrationTest
@Transactional
@TestPropertySource(properties = {"spring.data.redis.repositories.enabled=false"})
class AccessPeriodInitializerTest {

    @Autowired
    AccessPeriodInitializer accessPeriodInitializer;

    @Autowired
    RecruitRepository recruitRepository;

    @Autowired
    SemesterRepository semesterRepository;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Test
    @DisplayName("애플리케이션 구동 시 RECRUIT_FLAG 세팅")
    void set_recruit_flag_by_run_application() {
        // given
        redisTemplate.delete(List.of("RECRUIT_FLAG:PM", "RECRUIT_FLAG:PD", "RECRUIT_FLAG:FE", "RECRUIT_FLAG:BE"));

        Semester savedSemester = semesterRepository.save(Semester.builder()
                .name("1기")
                .isRecruiting(true)
                .build());

        recruitRepository.saveAll(List.of(
                Recruit.builder()
                        .semester(savedSemester)
                        .startDate(LocalDateTime.now().minusDays(1))
                        .endDate(LocalDateTime.now().plusDays(1))
                        .jobFamily(PM)
                        .build(),
                Recruit.builder()
                        .semester(savedSemester)
                        .startDate(LocalDateTime.now().minusDays(1))
                        .endDate(LocalDateTime.now().plusDays(1))
                        .jobFamily(PD)
                        .build(),
                Recruit.builder()
                        .semester(savedSemester)
                        .startDate(LocalDateTime.now().plusDays(3))
                        .endDate(LocalDateTime.now().plusDays(5))
                        .jobFamily(FE)
                        .build()
        ));

        // when
        accessPeriodInitializer.run(null);

        // then
        assertThat(Boolean.parseBoolean(getRecruitFlag(PM))).isTrue();
        assertThat(Boolean.parseBoolean(getRecruitFlag(PD))).isTrue();
        assertThat(Boolean.parseBoolean(getRecruitFlag(FE))).isFalse();
        assertThat(Boolean.parseBoolean(getRecruitFlag(BE))).isFalse();
    }

    private String getRecruitFlag(JobFamily jobFamily) {
        return redisTemplate.opsForValue().get(String.format("%s%s", Constants.RECRUIT_FLAG_PREFIX, jobFamily));
    }
}