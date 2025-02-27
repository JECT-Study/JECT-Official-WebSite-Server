package org.ject.support.domain.file.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.time.LocalDate;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.dto.Constants;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.testconfig.ApplicationPeriodTest;
import org.ject.support.testconfig.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@IntegrationTest
@AutoConfigureMockMvc
class FileControllerTest extends ApplicationPeriodTest {
    @Autowired
    private RecruitRepository recruitRepository;

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

        mockMvc.perform(post("/upload/presigned-url")
                        .contentType("application/json")
                        .content("{\"fileName\":\"test.txt\"}"))
                .andExpect(content().string(containsString("SUCCESS")))
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

        when(redisTemplate.opsForValue().get(Constants.PERIOD_FLAG)).thenReturn(Boolean.toString(false));
        // then
        mockMvc.perform(post("/upload/presigned-url")
                        .contentType("application/json")
                        .content("{\"fileName\":\"test.txt\"}"))
                .andExpect(content().string(containsString("G-06")))
                .andDo(print());
    }
}
