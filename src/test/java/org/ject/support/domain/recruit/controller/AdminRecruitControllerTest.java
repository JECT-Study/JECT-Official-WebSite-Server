package org.ject.support.domain.recruit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsString;
import static org.ject.support.domain.member.JobFamily.BE;
import static org.ject.support.domain.member.JobFamily.FE;
import static org.ject.support.domain.recruit.exception.RecruitErrorCode.DUPLICATED_JOB_FAMILY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    @DisplayName("지원서 관리용 모집공고 목록과 유형별 공고 수를 조회한다")
    void 지원서_관리용_모집공고_목록과_유형별_공고_수를_조회한다() throws Exception {
        // given
        Semester semester = semesterRepository.findRecruitingSemester().orElseThrow();
        saveRecruit(semester, FE, RecruitType.SEMESTER);
        Recruit beRecruit = saveRecruit(semester, BE, RecruitType.SEMESTER);
        saveRecruit(semester, FE, RecruitType.MAKERS);

        // when, then
        mockMvc.perform(get("/admin/recruits/apply-management")
                        .param("status", "SUBMITTED")
                        .param("recruitType", "SEMESTER")
                        .param("jobFamilies", "FE", "BE")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recruitTypeCounts", hasSize(3)))
                .andExpect(jsonPath("$.data.recruitTypeCounts[0].recruitType").value("SEMESTER"))
                .andExpect(jsonPath("$.data.recruitTypeCounts[0].count").value(2))
                .andExpect(jsonPath("$.data.recruitTypeCounts[1].recruitType").value("MAKERS"))
                .andExpect(jsonPath("$.data.recruitTypeCounts[1].count").value(1))
                .andExpect(jsonPath("$.data.recruitTypeCounts[2].recruitType").value("SUPPORTERS"))
                .andExpect(jsonPath("$.data.recruitTypeCounts[2].count").value(0))
                .andExpect(jsonPath("$.data.recruits.content", hasSize(1)))
                .andExpect(jsonPath("$.data.recruits.content[0].recruitId").value(beRecruit.getId()))
                .andExpect(jsonPath("$.data.recruits.content[0].title").value("BE 모집"))
                .andExpect(jsonPath("$.data.recruits.content[0].semesterId").value(semester.getId()))
                .andExpect(jsonPath("$.data.recruits.content[0].jobFamily").value("BE"))
                .andExpect(jsonPath("$.data.recruits.content[0].submittedApplyCount").value(0))
                .andExpect(jsonPath("$.data.recruits.size").value(1))
                .andExpect(jsonPath("$.data.recruits.hasNext").value(true))
                .andExpect(jsonPath("$.data.recruits.nextCursor").value(beRecruit.getId()))
                .andExpect(jsonPath("$.data.recruits.totalCount").value(2));

    }

    @Test
    @DisplayName("필수 조회 조건이 없으면 지원서 관리용 모집공고를 조회하지 않는다")
    void 필수_조회_조건이_없으면_지원서_관리용_모집공고를_조회하지_않는다() throws Exception {
        mockMvc.perform(get("/admin/recruits/apply-management"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 모집_정보를_등록한다() throws Exception {
        // given
        List<RecruitRegisterRequest> requests = List.of(
                recruitRegisterRequest("백엔드 개발자 모집", BE),
                recruitRegisterRequest("프론트엔드 개발자 모집", FE)
        );

        String reqJson = objectMapper.writeValueAsString(requests);

        // when
        mockMvc.perform(post("/admin/recruits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJson))
                .andExpect(content().string(containsString("SUCCESS")));

        // then
        assertThat(recruitRepository.findActiveRecruits(LocalDateTime.now()))
                .extracting(Recruit::getTitle)
                .containsExactlyInAnyOrder("백엔드 개발자 모집", "프론트엔드 개발자 모집");
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
                recruitRegisterRequest("백엔드 개발자 모집", BE),
                recruitRegisterRequest("프론트엔드 개발자 모집", FE)
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
                new RecruitUpdateRequest(
                        "수정된 프론트엔드 개발자 모집",
                        FE,
                        LocalDateTime.now().minusDays(1),
                        LocalDateTime.now().plusDays(1),
                        null,
                        null,
                        null,
                        List.of()));

        // when
        mockMvc.perform(put("/admin/recruits/{recruitId}", savedRecruit.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJson))
                .andExpect(content().string(containsString("SUCCESS")));

        // then
        Recruit updatedRecruit = recruitRepository.findById(savedRecruit.getId()).orElseThrow();
        assertThat(updatedRecruit.getTitle()).isEqualTo("수정된 프론트엔드 개발자 모집");
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

    private Recruit saveRecruit(Semester semester, JobFamily jobFamily, RecruitType recruitType) {
        return recruitRepository.save(Recruit.builder()
                .semester(semester)
                .title(jobFamily + " 모집")
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .jobFamily(jobFamily)
                .recruitType(recruitType)
                .recruitTypeDetail(recruitType == RecruitType.SEMESTER
                        ? RecruitTypeDetail.REGULAR
                        : RecruitTypeDetail.NEW)
                .build());
    }

    private RecruitRegisterRequest recruitRegisterRequest(String title, JobFamily jobFamily) {
        return new RecruitRegisterRequest(
                title,
                jobFamily,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                null,
                null,
                null,
                List.of());
    }
}
