package org.ject.support.admin.member.controller;

import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.ject.support.admin.member.dto.request.CreateMemberSemesterRequest;
import org.ject.support.admin.member.service.AdminMemberUseCase;
import org.ject.support.common.exception.GlobalExceptionHandler;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AdminMemberSemesterControllerTest {

    @Mock
    private AdminMemberUseCase adminMemberUseCase;

    @InjectMocks
    private AdminMemberSemesterController adminMemberSemesterController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(adminMemberSemesterController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    @DisplayName("유효한 요청이면 일반 구성원을 추가한다")
    void 유효한_요청이면_일반_구성원을_추가한다() throws Exception {
        // given
        CreateMemberSemesterRequest request = createMemberSemesterRequest("jectkim@ject.kr");

        // when
        mockMvc.perform(post("/admin/members/semester")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        // then
        verify(adminMemberUseCase).createMemberSemester(any(CreateMemberSemesterRequest.class));
    }

    @Test
    @DisplayName("필수값이 없으면 일반 구성원을 추가하지 않는다")
    void 필수값이_없으면_일반_구성원을_추가하지_않는다() throws Exception {
        // given
        CreateMemberSemesterRequest request = new CreateMemberSemesterRequest(
            null,
            "jectkim@ject.kr",
            "01012345678",
            JobFamily.BE,
            RecruitTypeDetail.REGULAR,
            CareerDetails.EMPLOYEE,
            1L,
            ExperiencePeriod.ONE_TO_TWO,
            "memo",
            List.of("HEALTHCARE"),
            Region.SEOUL
        );

        // when
        mockMvc.perform(post("/admin/members/semester")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        // then
        verify(adminMemberUseCase, never()).createMemberSemester(any(CreateMemberSemesterRequest.class));
    }

    @Test
    @DisplayName("이메일 형식이 올바르지 않으면 일반 구성원을 추가하지 않는다")
    void 이메일_형식이_올바르지_않으면_일반_구성원을_추가하지_않는다() throws Exception {
        // given
        CreateMemberSemesterRequest request = createMemberSemesterRequest("invalid-email");

        // when
        mockMvc.perform(post("/admin/members/semester")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());

        // then
        verify(adminMemberUseCase, never()).createMemberSemester(any(CreateMemberSemesterRequest.class));
    }

    private CreateMemberSemesterRequest createMemberSemesterRequest(String email) {
        return new CreateMemberSemesterRequest(
            "김젝트",
            email,
            "01012345678",
            JobFamily.BE,
            RecruitTypeDetail.REGULAR,
            CareerDetails.EMPLOYEE,
            1L,
            ExperiencePeriod.ONE_TO_TWO,
            "memo",
            List.of("HEALTHCARE", "FINTECH", "AI"),
            Region.SEOUL
        );
    }
}
