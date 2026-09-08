package org.ject.support.domain.recruit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.dto.RecruitUpdateRequest;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.ject.support.testconfig.AuthenticatedUser;
import org.ject.support.testconfig.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ject.support.domain.member.JobFamily.BE;
import static org.ject.support.domain.member.JobFamily.FE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
@AutoConfigureMockMvc
@AuthenticatedUser(isAdmin = true)
class RecruitCacheTest {

    private static final String RECRUIT_DETAIL_CACHE_KEY = "cache::recruit-detail::";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RecruitRepository recruitRepository;

    @Autowired
    SemesterRepository semesterRepository;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @Autowired
    TransactionTemplate transactionTemplate;

    Recruit recruit;

    @BeforeEach
    void setUp() {
        Semester semester = semesterRepository.save(Semester.builder()
                .name("캐시" + UUID.randomUUID().toString().substring(0, 8))
                .isRecruiting(true)
                .build());
        recruit = recruitRepository.save(Recruit.builder()
                .semester(semester)
                .jobFamily(BE)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .recruitInformation("<h2>기존 모집 정보</h2>")
                .faqs(List.of())
                .build());
    }

    @Test
    void 모집_공고_상세정보를_조회하면_캐시에_저장한다() throws Exception {
        // when
        mockMvc.perform(get("/recruits/{recruitId}", recruit.getId()))
                .andExpect(status().isOk());

        // then
        assertThat(redisTemplate.hasKey(cacheKey())).isTrue();
    }

    @Test
    void 캐시된_모집_공고_상세정보를_다시_조회한다() throws Exception {
        // given
        mockMvc.perform(get("/recruits/{recruitId}", recruit.getId()))
                .andExpect(status().isOk());
        transactionTemplate.executeWithoutResult(status -> {
            Recruit savedRecruit = recruitRepository.findById(recruit.getId()).orElseThrow();
            savedRecruit.update(
                    savedRecruit.getJobFamily(),
                    savedRecruit.getStartDate(),
                    savedRecruit.getEndDate(),
                    "<h2>변경된 모집 정보</h2>",
                    null,
                    null
            );
        });

        // when, then
        mockMvc.perform(get("/recruits/{recruitId}", recruit.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recruitInformation").value("<h2>기존 모집 정보</h2>"));
    }

    @Test
    void 모집_공고를_수정하면_상세정보_캐시를_제거한다() throws Exception {
        // given
        mockMvc.perform(get("/recruits/{recruitId}", recruit.getId()))
                .andExpect(status().isOk());
        RecruitUpdateRequest request = new RecruitUpdateRequest(
                FE,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(3),
                "<h2>변경된 모집 정보</h2>",
                null,
                List.of()
        );

        // when
        mockMvc.perform(put("/admin/recruits/{recruitId}", recruit.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // then
        assertThat(redisTemplate.hasKey(cacheKey())).isFalse();
    }

    @Test
    void 모집_공고를_삭제하면_상세정보_캐시를_제거한다() throws Exception {
        // given
        mockMvc.perform(get("/recruits/{recruitId}", recruit.getId()))
                .andExpect(status().isOk());

        // when
        mockMvc.perform(delete("/admin/recruits/{recruitId}", recruit.getId()))
                .andExpect(status().isOk());

        // then
        assertThat(redisTemplate.hasKey(cacheKey())).isFalse();
    }

    private String cacheKey() {
        return RECRUIT_DETAIL_CACHE_KEY + recruit.getId();
    }
}
