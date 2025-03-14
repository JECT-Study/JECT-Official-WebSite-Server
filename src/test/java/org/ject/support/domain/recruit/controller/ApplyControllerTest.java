package org.ject.support.domain.recruit.controller;

import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.domain.recruit.domain.Question;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.domain.tempapply.domain.TemporaryApplication;
import org.ject.support.domain.tempapply.repository.TemporaryApplicationRepository;
import org.ject.support.external.dynamodb.domain.CompositeKey;
import org.ject.support.testconfig.ApplicationPeriodTest;
import org.ject.support.testconfig.AuthenticatedUser;
import org.ject.support.testconfig.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.ject.support.domain.member.Role.USER;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Redis 관련 설정을 제외하고 필요한 설정만 포함합니다
@IntegrationTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = {"spring.data.redis.repositories.enabled=false"})
class ApplyControllerTest extends ApplicationPeriodTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    RecruitRepository recruitRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TemporaryApplicationRepository temporaryApplicationRepository;

    Member member;

    @BeforeEach
    void setUp() {
        List<Question> questions = List.of(
                Question.builder().title("title1").body("question1").build(),
                Question.builder().title("title2").body("question2").build(),
                Question.builder().title("title3").body("question3").build(),
                Question.builder().title("title4").body("question4").build(),
                Question.builder().title("title5").body("question5").build()
        );

        Recruit recruit = Recruit.builder()
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(1))
                .semester("2025-1")
                .jobFamily(JobFamily.BE)
                .build();

        for (Question question : questions) {
            recruit.addQuestion(question);
        }

        recruitRepository.save(recruit);

        member = Member.builder()
                .email("test32@gmail.com")
                .jobFamily(JobFamily.BE)
                .name("김젝트")
                .role(USER)
                .phoneNumber("01012345678")
                .semester("2025-1")
                .pin("123456") // PIN 필드 추가
                .build();
        memberRepository.save(member);
    }

    @AfterEach
    void tearDown() {
        temporaryApplicationRepository.deleteAll();
    }

    @Test
    @DisplayName("apply temporal test")
    @AuthenticatedUser
    void test_temp_apply() throws Exception {
        mockMvc.perform(post("/apply/temp")
                        .contentType("application/json")
                        .param("jobFamily", "BE")
                        .content("""
                                {
                                  "1": "answer1",
                                  "2": "answer2",
                                  "3": "answer3",
                                  "4": "answer4",
                                  "5": "answer5"
                                }""")
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
    }

    @Test
    @DisplayName("invalid question id")
    @AuthenticatedUser
    @Transactional
    void invalid_question_id() throws Exception {
        mockMvc.perform(post("/apply/temp")
                        .contentType("application/json")
                        .param("jobFamily", "BE")
                        .content("""
                                {
                                  "1": "answer1",
                                  "2": "answer2",
                                  "3": "answer3",
                                  "4": "answer4",
                                  "5": "answer5",
                                  "6": "answer6"
                                }""")
                )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("QUESTION_NOT_FOUND")))
                .andDo(print())
                .andReturn();
    }

    @Test
    @AuthenticatedUser
    void inquire_temporal_application() throws Exception {
        // given: 테스트 데이터 저장
        temporaryApplicationRepository.save(new TemporaryApplication("1", Map.of("1", "답변1", "2", "답변2"), "PM"));
        temporaryApplicationRepository.save(
                new TemporaryApplication("1", Map.of("3", "답변3", "4", "답변4", "5", "답변5"), "BE"));

        // when & then
        ResultActions resultActions = mockMvc.perform(get("/apply/temp"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SUCCESS")))
                .andExpectAll(
                        content().string(containsString("답변3")),
                        content().string(containsString("답변4")),
                        content().string(containsString("답변5"))
                );

        resultActions.andDo(print());
    }

    @Test
    @DisplayName("change job family")
    @AuthenticatedUser
    void change_job_family() throws Exception {
        // given
        temporaryApplicationRepository.save(new TemporaryApplication("1", Map.of(
                "8", "answer 1-1 for 8",
                "9", "answer 1-1 for 9",
                "10", "answer 1-1 for 10"), "BE"));
        temporaryApplicationRepository.save(new TemporaryApplication("1", Map.of(
                "8", "answer 1-2 for 8",
                "9", "answer 1-2 for 9",
                "10", "answer 1-2 for 10"), "BE"));
        temporaryApplicationRepository.save(new TemporaryApplication("1", Map.of(
                "8", "answer 1-3 for 8",
                "9", "answer 1-3 for 9",
                "10", "answer 1-3 for 10"), "BE"));
        temporaryApplicationRepository.save(new TemporaryApplication("2", Map.of(
                "8", "answer 2-1 for 8",
                "9", "answer 2-1 for 9",
                "10", "answer 2-1 for 10"), "BE"));
        temporaryApplicationRepository.save(new TemporaryApplication("2", Map.of(
                "8", "answer 2-2 for 8",
                "9", "answer 2-2 for 9",
                "10", "answer 2-2 for 10"), "BE"));

        // when, then
        mockMvc.perform(put("/apply/job")
                        .contentType("application/json")
                        .content("\"FE\"")
                )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SUCCESS")))
                .andDo(print())
                .andReturn();

        assertThat(temporaryApplicationRepository.findByPartitionKey(new CompositeKey("MEMBER", "1"))).isEmpty();
        assertThat(temporaryApplicationRepository.findByPartitionKey(new CompositeKey("MEMBER", "2"))).hasSize(2);
    }
}
