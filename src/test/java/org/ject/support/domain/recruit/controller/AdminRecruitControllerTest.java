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
import static org.ject.support.domain.recruit.exception.RecruitErrorCode.DUPLICATED_JOB_FAMILY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@IntegrationTest
@AutoConfigureMockMvc
@Transactional
@AuthenticatedUser(isAdmin = true)
class AdminRecruitControllerTest {

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
    void 모집_정보를_등록한다() throws Exception {
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
    void 이미_모집중인_직군에_대한_모집_등록은_실패한다() throws Exception {
        // given
        Semester recruitingSemester = semesterRepository.findRecruitingSemester()
                .orElse(Semester.builder().id(1L).name("1기").isRecruiting(true).build());
        recruitRepository.save(Recruit.builder()
                .semester(recruitingSemester)
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
                .andExpect(content().string(containsString(DUPLICATED_JOB_FAMILY.getCode())));
    }

    @Test
    void 모집_정보를_수정한다() throws Exception {
        // given
        Semester recruitingSemester = semesterRepository.findRecruitingSemester()
                .orElse(Semester.builder().id(1L).name("1기").isRecruiting(true).build());
        Recruit savedRecruit = recruitRepository.save(Recruit.builder()
                .semester(recruitingSemester)
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
    void 모집을_취소한다() throws Exception {
        // given
        Semester recruitingSemester = semesterRepository.findRecruitingSemester()
                .orElse(Semester.builder().id(1L).name("1기").isRecruiting(true).build());
        Recruit savedRecruit = recruitRepository.save(Recruit.builder()
                .semester(recruitingSemester)
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
