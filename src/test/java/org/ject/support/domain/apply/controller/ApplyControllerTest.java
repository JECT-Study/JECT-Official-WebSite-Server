package org.ject.support.domain.apply.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.response.ResponseWrapper;
import org.ject.support.common.security.AuthenticatedApplicantIdResolver;
import org.ject.support.common.security.CustomUserDetails;
import org.ject.support.domain.apply.dto.ApplyProfileRequest;
import org.ject.support.domain.apply.dto.ApplyStatusResponse;
import org.ject.support.domain.apply.dto.ApplyTemporaryRequest;
import org.ject.support.domain.apply.dto.SubmitApplicationRequest;
import org.ject.support.domain.apply.dto.TempApplicationFormResponse;
import org.ject.support.domain.apply.service.ApplyUsecase;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.InterestedDomain;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.member.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.ject.support.domain.apply.domain.ApplyStatus.SUBMITTED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApplyControllerTest extends UnitTestSupport {

    @InjectMocks
    ApplyController applyController;

    @Mock
    ApplyUsecase applyUsecase;

    MockMvc mockMvc;
    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(applyController)
                .setControllerAdvice(new ResponseWrapper())
                .setCustomArgumentResolvers(new AuthenticatedApplicantIdResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 임시저장_지원서를_모집_공고_기준으로_조회한다() throws Exception {
        // given
        long memberId = 1L;
        long recruitId = 10L;
        setAuthentication(memberId);
        given(applyUsecase.findTempApplicationForm(memberId, recruitId))
                .willReturn(new TempApplicationFormResponse(JobFamily.FE, Map.of("1", "답변"), List.of()));

        // when, then
        mockMvc.perform(get("/apply/temp")
                        .param("recruitId", String.valueOf(recruitId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(applyUsecase).findTempApplicationForm(memberId, recruitId);
    }

    @Test
    void 지원서를_모집_공고_기준으로_임시저장한다() throws Exception {
        // given
        long memberId = 1L;
        long recruitId = 10L;
        setAuthentication(memberId);
        ApplyTemporaryRequest request = new ApplyTemporaryRequest(Map.of("1", "답변"), List.of());

        // when, then
        mockMvc.perform(post("/apply/temp")
                        .param("recruitId", String.valueOf(recruitId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(applyUsecase).saveApplicationTemporarily(
                eq(memberId), eq(recruitId), eq(request.answers()), eq(request.portfolios()));
    }

    @Test
    void 모집_공고_기준으로_프로필과_임시저장_지원서를_제거한다() throws Exception {
        // given
        long memberId = 1L;
        long recruitId = 10L;
        setAuthentication(memberId);

        // when, then
        mockMvc.perform(delete("/apply/temp")
                        .param("recruitId", String.valueOf(recruitId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(applyUsecase).deleteProfileAndTempApplicationForm(memberId, recruitId);
    }

    @Test
    void 지원서를_모집_공고_기준으로_제출한다() throws Exception {
        // given
        long memberId = 1L;
        long recruitId = 10L;
        setAuthentication(memberId);
        SubmitApplicationRequest request = new SubmitApplicationRequest(Map.of("1", "답변"), List.of());

        // when, then
        mockMvc.perform(post("/apply/submit")
                        .param("recruitId", String.valueOf(recruitId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(applyUsecase).submitApplication(
                eq(memberId), eq(recruitId), eq(request.answers()), eq(request.portfolios()));
    }

    @Test
    void 모집_공고_식별자가_없으면_지원서_제출에_실패한다() throws Exception {
        // given
        long memberId = 1L;
        setAuthentication(memberId);
        SubmitApplicationRequest request = new SubmitApplicationRequest(Map.of("1", "답변"), List.of());

        // when, then
        mockMvc.perform(post("/apply/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(applyUsecase);
    }

    @Test
    void 지원상태를_모집_공고_기준으로_조회한다() throws Exception {
        // given
        long memberId = 1L;
        long recruitId = 10L;
        setAuthentication(memberId);
        given(applyUsecase.checkApplyStatus(memberId, recruitId))
                .willReturn(new ApplyStatusResponse(SUBMITTED));

        // when, then
        mockMvc.perform(get("/apply/status")
                        .param("recruitId", String.valueOf(recruitId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"));

        verify(applyUsecase).checkApplyStatus(memberId, recruitId);
    }

    @Test
    void 모집_공고_식별자가_없으면_지원상태_조회에_실패한다() throws Exception {
        // given
        long memberId = 1L;
        setAuthentication(memberId);

        // when, then
        mockMvc.perform(get("/apply/status"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(applyUsecase);
    }

    @Test
    void 프로필을_모집_공고_기준으로_저장한다() throws Exception {
        // given
        long memberId = 1L;
        long recruitId = 10L;
        setAuthentication(memberId);
        ApplyProfileRequest request = new ApplyProfileRequest(
                "김젝트",
                "010-1234-5678",
                JobFamily.FE,
                Region.SEOUL,
                CareerDetails.STUDENT,
                ExperiencePeriod.NONE,
                List.of(InterestedDomain.GAME.getDescription())
        );

        // when, then
        mockMvc.perform(post("/apply/profile")
                        .param("recruitId", String.valueOf(recruitId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(applyUsecase).saveProfile(eq(memberId), eq(recruitId), any(ApplyProfileRequest.class));
    }

    @Test
    void 모집_공고_식별자가_없으면_프로필_저장에_실패한다() throws Exception {
        // given
        long memberId = 1L;
        setAuthentication(memberId);
        ApplyProfileRequest request = new ApplyProfileRequest(
                "김젝트",
                "010-1234-5678",
                JobFamily.FE,
                Region.SEOUL,
                CareerDetails.STUDENT,
                ExperiencePeriod.NONE,
                List.of(InterestedDomain.GAME.getDescription())
        );

        // when, then
        mockMvc.perform(post("/apply/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(applyUsecase);
    }

    private void setAuthentication(final Long memberId) {
        CustomUserDetails userDetails = new CustomUserDetails("apply@ject.kr", memberId, Role.APPLY);
        var authentication = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
