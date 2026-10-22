package org.ject.support.domain.recruit.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.ject.support.domain.recruit.exception.RecruitErrorCode.DUPLICATED_JOB_FAMILY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.dto.RecruitRegisterRequest;
import org.ject.support.domain.recruit.dto.SemesterRegisterRequest;
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
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@AutoConfigureMockMvc
@Transactional
class SemesterControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    RecruitRepository recruitRepository;

    @Autowired
    ObjectMapper objectMapper;
    
    @Autowired
    private SemesterRepository semesterRepository;

    @BeforeEach
    void setUp() {
        recruitRepository.deleteAll();
        semesterRepository.deleteAll();
        TestTransaction.flagForCommit();
        TestTransaction.end();
    }

    @Test
    @AuthenticatedUser(isAdmin = true)
    @DisplayName("학기와 모집 포지션을 등록한다")
    void register_semester() throws Exception {
        // given
        SemesterRegisterRequest request = new SemesterRegisterRequest("2023-2학기",
                List.of(
                        recruitRegisterRequest("백엔드 개발자 모집", JobFamily.BE),
                        recruitRegisterRequest("프론트엔드 개발자 모집", JobFamily.FE)));

        String reqJson = objectMapper.writeValueAsString(request);
        // when
        mockMvc.perform(post("/admin/semesters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJson))
                .andExpect(status().isOk())
                .andExpect(
                        content().string(containsString("SUCCESS"))
                );

        // then
        assertThat(semesterRepository.findAll()).hasSize(1);
        assertThat(recruitRepository.findAll()).hasSize(2);
    }

    @Test
    @AuthenticatedUser(isAdmin = true)
    @DisplayName("중복된 JobFamily로 학기를 등록할 수 없다")
    void register_semester_with_duplicate_jobFamily() throws Exception {
        // given
        SemesterRegisterRequest request = new SemesterRegisterRequest("2023-2학기",
                List.of(
                        recruitRegisterRequest("백엔드 개발자 모집", JobFamily.BE),
                        recruitRegisterRequest("중복 백엔드 개발자 모집", JobFamily.BE)));

        String reqJson = objectMapper.writeValueAsString(request);
        // when
        mockMvc.perform(post("/admin/semesters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqJson))
                .andExpect(status().isConflict())
                .andDo(print())
                .andExpect(
                        content().string(containsString(DUPLICATED_JOB_FAMILY.getCode()))
                );

        // then
        assertThat(semesterRepository.findAll()).isEmpty();
        assertThat(recruitRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("모든 기수 목록을 조회한다")
    void getAllSemesters() throws Exception {
        //given
        Semester semester1 = Semester.builder()
                .name("2025-1학기")
                .build();
        Semester semester2 = Semester.builder()
                .name("2025-2학기")
                .build();
        semesterRepository.saveAll(List.of(semester1, semester2));
        // when & then
        mockMvc.perform(get("/semesters"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpectAll(
                        content().string(containsString("2025-1학기")),
                        content().string(containsString("2025-2학기"))
                );
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
