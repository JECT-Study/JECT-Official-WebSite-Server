package org.ject.support.domain.recruit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.dto.RecruitRegisterRequest;
import org.ject.support.domain.recruit.dto.RecruitUpdateRequest;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.ject.support.testconfig.AuthenticatedUser;
import org.ject.support.testconfig.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.ject.support.domain.member.JobFamily.BE;
import static org.ject.support.domain.member.JobFamily.FE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@IntegrationTest
@AutoConfigureMockMvc
@Transactional
@AuthenticatedUser(isAdmin = true)
class RecruitControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RecruitRepository recruitRepository;

    @Autowired
    SemesterRepository semesterRepository;

    @BeforeEach
    void setUp() {
        semesterRepository.deleteAll();
        recruitRepository.deleteAll();
        semesterRepository.save(Semester.builder().name("3기").isRecruiting(true).build());
    }

    @Test
    @DisplayName("모집 정보 등록")
    void register_recruits() throws Exception {
        // given
        List<RecruitRegisterRequest> requests = List.of(
                new RecruitRegisterRequest(
                        BE,
                        LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(1)),
                new RecruitRegisterRequest(
                        FE,
                        LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(1))
        );

        String reqJson = objectMapper.writeValueAsString(requests);

        // when
        mockMvc.perform(post("/admin/recruits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJson))
                .andExpect(content().string(containsString("SUCCESS")));

        // then
        assertThat(recruitRepository.findActiveRecruits(LocalDateTime.now())).hasSize(2);
    }

    @Test
    @DisplayName("이미 모집중인 직군에 대한 모집 등록 시 실패")
    void register_recruit_fail_by_duplicated_job_family() throws Exception {
        // given
        Long semesterId = semesterRepository.findOngoingSemesterId().orElse(1L);
        recruitRepository.save(Recruit.builder()
                .semesterId(semesterId)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .jobFamily(BE)
                .build());

        List<RecruitRegisterRequest> requests = List.of(
                new RecruitRegisterRequest(BE, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1)),
                new RecruitRegisterRequest(FE, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1))
        );

        String reqJson = objectMapper.writeValueAsString(requests);

        // when, then
        mockMvc.perform(post("/admin/recruits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJson))
                .andExpect(content().string(containsString("DUPLICATED_JOB_FAMILY")));
    }

    @Test
    @DisplayName("모집 정보 수정")
    void update_recruit() throws Exception {
        // given
        Long semesterId = semesterRepository.findOngoingSemesterId().orElse(1L);
        Recruit savedRecruit = recruitRepository.save(Recruit.builder()
                .semesterId(semesterId)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .jobFamily(BE)
                .build());

        String reqJson = objectMapper.writeValueAsString(
                new RecruitUpdateRequest(FE, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1)));

        // when
        mockMvc.perform(put("/admin/recruits/{recruitId}", savedRecruit.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJson))
                .andExpect(content().string(containsString("SUCCESS")));

        // then
        Recruit updatedRecruit = recruitRepository.findById(savedRecruit.getId()).orElseThrow();
        assertThat(updatedRecruit.getJobFamily()).isEqualTo(FE);
    }

    @Test
    @DisplayName("모집 취소")
    void cancel_recruit() throws Exception {
        // given
        Long semesterId = semesterRepository.findOngoingSemesterId().orElse(1L);
        Recruit savedRecruit = recruitRepository.save(Recruit.builder()
                .semesterId(semesterId)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .jobFamily(BE)
                .build());

        // when
        mockMvc.perform(delete("/admin/recruits/{recruitId}", savedRecruit.getId()))
                .andExpect(content().string(containsString("SUCCESS")));

        // then
        List<Recruit> recruits = recruitRepository.findActiveRecruits(LocalDateTime.now());
        assertThat(recruits).isEmpty();
    }
}