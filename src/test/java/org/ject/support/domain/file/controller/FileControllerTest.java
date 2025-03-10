package org.ject.support.domain.file.controller;

import java.time.LocalDate;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.dto.Constants;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.testconfig.ApplicationPeriodTest;
import org.ject.support.testconfig.AuthenticatedUser;
import org.ject.support.testconfig.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.ject.support.domain.member.Role.USER;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@IntegrationTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.data.redis.repositories.enabled=false"})
class FileControllerTest extends ApplicationPeriodTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    Member member;

    @Autowired
    private RecruitRepository recruitRepository;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .email("test32@gmail.com")
                .jobFamily(JobFamily.BE)
                .name("홍길동") // 한글 1~5글자로 수정
                .role(USER)
                .phoneNumber("01012345678") // 010으로 시작하는 11자리 수정
                .semester("2025-1")
                .pin("123456") // PIN 추가
                .build();
        memberRepository.save(member);
    }

    @Test
    @DisplayName("test period access endpoint")
    @AuthenticatedUser
    @Transactional
    void test_access_period() throws Exception {
        recruitRepository.save(Recruit.builder()
                .jobFamily(JobFamily.BE)
                .semester("2021-1")
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(1))
                .build());

        mockMvc.perform(post("/upload/portfolios")
                        .contentType("application/json")
                        .param("memberId", member.getId().toString())
                        .content(getContent()))
                .andExpect(content().string(containsString("SUCCESS")))
                .andDo(print());
    }

    @Test
    @DisplayName("not in period")
    @AuthenticatedUser
    @Transactional
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
        mockMvc.perform(post("/upload/portfolios")
                        .contentType("application/json")
                        .param("memberId", member.getId().toString())
                        .content(getContent()))
                .andExpect(content().string(containsString("G-06")))
                .andDo(print());
    }

    @Test
    @DisplayName("invalid portfolio content type")
    @AuthenticatedUser
    @Transactional
    void invalid_portfolio_content_type() throws Exception {
        // given
        recruitRepository.save(Recruit.builder()
                .jobFamily(JobFamily.BE)
                .semester("2021-1")
                .startDate(LocalDate.now().plusDays(3))
                .endDate(LocalDate.now().plusDays(5))
                .build());

        when(redisTemplate.opsForValue().get(Constants.PERIOD_FLAG)).thenReturn(Boolean.toString(false));

        // when, then
        mockMvc.perform(post("/upload/portfolios")
                        .contentType("application/json")
                        .param("memberId", member.getId().toString())
                        .content("""
                                [
                                    {
                                        "name": "test1.png",
                                        "contentType": "image/png",
                                        "contentLength": 126203
                                    }
                                ]
                                """))
                .andExpect(content().string(containsString("G-06")))
                .andDo(print());
    }

    private String getContent() {
        return """
                [
                    {
                        "name": "test1.pdf",
                        "contentType": "application/pdf",
                        "contentLength": 126203
                    },
                    {
                        "name": "test2.pdf",
                        "contentType": "application/pdf",
                        "contentLength": 126203
                    }
                ]
                """;
    }
}
