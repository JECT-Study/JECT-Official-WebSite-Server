package org.ject.support.domain.recruit.controller;

import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.domain.recruit.domain.Question;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.dto.ApplyPortfolioDto;
import org.ject.support.domain.recruit.repository.ApplicationFormRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.ject.support.domain.member.Role.USER;
import static org.ject.support.domain.recruit.domain.Question.InputType.TEXT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Autowired
    ApplicationFormRepository applicationFormRepository;

    Member member;

    @BeforeEach
    void setUp() {
        List<Question> questions = List.of(
                Question.builder().sequence(1).inputType(TEXT).isRequired(true).title("title1").label("label1").build(),
                Question.builder().sequence(2).inputType(TEXT).isRequired(true).title("title2").label("label2").build(),
                Question.builder().sequence(3).inputType(TEXT).isRequired(true).title("title3").label("label3").build(),
                Question.builder().sequence(4).inputType(TEXT).isRequired(true).title("title4").label("label4").build(),
                Question.builder().sequence(5).inputType(TEXT).isRequired(true).title("title5").label("label5").build()
        );

        Recruit recruit = Recruit.builder()
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .semesterId(1L)
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
                .semesterId(1L)
                .pin("123456") // PIN 필드 추가
                .status(MemberStatus.ACTIVE)
                .build();
        memberRepository.save(member);
    }

    @AfterEach
    void tearDown() {
        temporaryApplicationRepository.deleteAll();
        applicationFormRepository.deleteAll();
        recruitRepository.deleteAll();
        memberRepository.deleteAll();
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
                                    "answers": {
                                        "1": "1번 답변임",
                                        "2": "2번 답변임~",
                                        "3": "3번 답변임~~",
                                        "4": "4번.",
                                        "5": "5번 답변~"
                                    },
                                    "portfolios": [
                                        {
                                            "fileUrl": "filrUrlA",
                                            "fileName": "fileNameA",
                                            "fileSize": "105021",
                                            "sequence": "1"
                                        },
                                        {
                                            "fileUrl": "filrUrlB",
                                            "fileName": "fileNameB",
                                            "fileSize": "105021",
                                            "sequence": "2"
                                        }
                                    ]
                                }
                                """)
                )
//                .andExpect(status().isOk())
//                .andExpect(content().string(containsString("SUCCESS")))
                .andDo(print())
                .andReturn();
    }

    @Test
    @DisplayName("invalid question semesterId")
    @AuthenticatedUser
    @Transactional
    void invalid_question_id() throws Exception {
        mockMvc.perform(post("/apply/temp")
                        .contentType("application/json")
                        .param("jobFamily", "BE")
                        .content("""
                                {
                                    "answers": {
                                        "1": "1번 답변임",
                                        "2": "2번 답변임~",
                                        "3": "3번 답변임~~",
                                        "4": "4번.",
                                        "6": "???"
                                    }
                                }
                                """)
                )
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("QUESTION_NOT_FOUND")))
                .andDo(print())
                .andReturn();
    }

    @Test
    @AuthenticatedUser
    void inquire_temporal_application() throws Exception {
        // given: 테스트 데이터 저장
        temporaryApplicationRepository.save(createTemporaryApplication(
                "1",
                Map.of("1", "답변1", "2", "답변2"),
                "PM",
                List.of(createApplyTemporaryPortfolio("1"))));
        temporaryApplicationRepository.save(createTemporaryApplication(
                "1",
                Map.of("3", "답변3", "4", "답변4", "5", "답변5"),
                "BE",
                List.of(createApplyTemporaryPortfolio("1"), createApplyTemporaryPortfolio("2"))));

        // when & then
        ResultActions resultActions = mockMvc.perform(get("/apply/temp"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SUCCESS")))
                .andExpectAll(
                        content().string(containsString("jobFamily")),
                        content().string(containsString("답변3")),
                        content().string(containsString("답변4")),
                        content().string(containsString("답변5")),
                        content().string(containsString("fileName")),
                        content().string(containsString("fileUrl"))
                );

        resultActions.andDo(print());
    }

    @Test
    @DisplayName("delete temp application")
    @AuthenticatedUser
    void delete_temp_application() throws Exception {
        // given
        temporaryApplicationRepository.save(new TemporaryApplication("1", Map.of(
                "8", "answer 1-1 for 8",
                "9", "answer 1-1 for 9",
                "10", "answer 1-1 for 10"), "BE", List.of(createApplyTemporaryPortfolio("1"))));
        temporaryApplicationRepository.save(new TemporaryApplication("1", Map.of(
                "8", "answer 1-2 for 8",
                "9", "answer 1-2 for 9",
                "10", "answer 1-2 for 10"), "BE", List.of(createApplyTemporaryPortfolio("1"))));
        temporaryApplicationRepository.save(new TemporaryApplication("1", Map.of(
                "8", "answer 1-3 for 8",
                "9", "answer 1-3 for 9",
                "10", "answer 1-3 for 10"), "BE", List.of(createApplyTemporaryPortfolio("1"))));
        temporaryApplicationRepository.save(new TemporaryApplication("2", Map.of(
                "8", "answer 2-1 for 8",
                "9", "answer 2-1 for 9",
                "10", "answer 2-1 for 10"), "BE", List.of(createApplyTemporaryPortfolio("1"))));
        temporaryApplicationRepository.save(new TemporaryApplication("2", Map.of(
                "8", "answer 2-2 for 8",
                "9", "answer 2-2 for 9",
                "10", "answer 2-2 for 10"), "BE", List.of(createApplyTemporaryPortfolio("1"))));

        // when, then
        mockMvc.perform(delete("/apply/temp")
                        .contentType("application/json")
                )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SUCCESS")))
                .andDo(print())
                .andReturn();

        assertThat(temporaryApplicationRepository.findByPartitionKey(new CompositeKey("MEMBER", "1"))).isEmpty();
        assertThat(temporaryApplicationRepository.findByPartitionKey(new CompositeKey("MEMBER", "2"))).hasSize(2);
    }

    @Test
    @DisplayName("submit application form")
    @AuthenticatedUser
    void submit_application_form() throws Exception {
        // when, then
        mockMvc.perform(post("/apply/submit")
                        .contentType("application/json")
                        .param("jobFamily", "BE")
                        .content("""
                                {
                                    "answers": {
                                        "1": "1번 답변임",
                                        "2": "2번 답변임~",
                                        "3": "3번 답변임~~",
                                        "4": "4번.",
                                        "5": "5번 답변~"
                                    },
                                    "portfolios": [
                                        {
                                            "fileUrl": "filrUrlA",
                                            "fileName": "fileNameA",
                                            "fileSize": "105021",
                                            "sequence": "1"
                                        },
                                        {
                                            "fileUrl": "filrUrlB",
                                            "fileName": "fileNameB",
                                            "fileSize": "105021",
                                            "sequence": "2"
                                        }
                                    ]
                                }
                                """)
                )
//                .andExpect(status().isOk())
//                .andExpect(content().string(containsString("SUCCESS")))
                .andDo(print())
                .andReturn();
    }

    private TemporaryApplication createTemporaryApplication(String memberId,
                                                            Map<String, String> answers,
                                                            String jobFamily,
                                                            List<ApplyPortfolioDto> portfolios) {
        return new TemporaryApplication(memberId, answers, jobFamily, portfolios);
    }

    private ApplyPortfolioDto createApplyTemporaryPortfolio(String sequence) {
        return new ApplyPortfolioDto("url", "name", "10202", sequence);
    }
}
