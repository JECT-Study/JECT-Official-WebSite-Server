package org.ject.support.domain.file.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.time.LocalDate;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.dto.Constants;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.testconfig.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
class FileControllerTest{
    @Autowired
    private RecruitRepository recruitRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("test period access endpoint")
    void test_access_period() throws Exception {
        recruitRepository.save(Recruit.builder()
                .jobFamily(JobFamily.BE)
                .semester("2021-1")
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(1))
                .build());
        redisTemplate.opsForValue().set(Constants.PERIOD_FLAG, Boolean.toString(true));//모집 기간으로 지정

        mockMvc.perform(post("/upload/presigned-url")
                        .contentType("application/json")
                        .content("{\"fileName\":\"test.txt\"}"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andDo(print());
    }

    @Test
    @DisplayName("not in period")
    void not_in_period() throws Exception {
        // given
        recruitRepository.save(Recruit.builder()
                .jobFamily(JobFamily.BE)
                .semester("2021-1")
                .startDate(LocalDate.now().plusDays(3))
                .endDate(LocalDate.now().plusDays(5))
                .build());

        redisTemplate.opsForValue().set(Constants.PERIOD_FLAG, Boolean.toString(false));//모집 기간 종료
        // then
        mockMvc.perform(post("/upload/presigned-url")
                        .contentType("application/json")
                        .content("{\"fileName\":\"test.txt\"}"))
                .andExpect(jsonPath("$.status").value("G-06"))
                .andDo(print());
    }
}
