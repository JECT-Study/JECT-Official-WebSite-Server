package org.ject.support.domain.recruit.service;

import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.testconfig.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@TestPropertySource(properties = {"spring.data.redis.repositories.enabled=false"})
class RecruitFlagServiceTest {

    @Autowired
    RecruitFlagService recruitFlagService;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Test
    @DisplayName("recruit flag 세팅")
    void set_recruit_flag() {
        // given
        Recruit recruit = Recruit.builder()
                .semesterId(1L)
                .jobFamily(JobFamily.BE)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .build();

        // when
        recruitFlagService.setRecruitFlag(recruit);

        // then
        assertThat(redisTemplate.opsForValue().get("RECRUIT_FLAG:BE")).isEqualTo("true");
    }

    @Test
    @DisplayName("recruit flag 제거")
    void delete_recruit_flag() {
        // when
        recruitFlagService.deleteRecruitFlag("RECRUIT_FLAG:BE");

        // then
        assertThat(redisTemplate.opsForValue().get("RECRUIT_FLAG:BE")).isEqualTo(null);
    }
}