package org.ject.support.domain.apply.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.response.ResponseWrapper;
import org.ject.support.common.security.AuthenticatedMemberIdResolver;
import org.ject.support.common.security.CustomUserDetails;
import org.ject.support.domain.apply.dto.ApplyProfileRequest;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
                .setCustomArgumentResolvers(new AuthenticatedMemberIdResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
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
