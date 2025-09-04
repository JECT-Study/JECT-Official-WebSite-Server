package org.ject.support.domain.recruit.controller;

import org.assertj.core.api.Assertions;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.domain.recruit.domain.Question;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.repository.QuestionRepository;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.testconfig.AuthenticatedUser;
import org.ject.support.testconfig.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.ject.support.domain.member.Role.USER;
import static org.ject.support.domain.recruit.domain.Question.InputType.TEXT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.data.redis.repositories.enabled=false"})
class QuestionControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    RecruitRepository recruitRepository;

    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    Member member;

    @BeforeEach
    void setUp() {
        List<Question> questions = List.of(
                Question.builder().sequence(1).inputType(TEXT).isRequired(true).title("title1").label("label").selectOptions(List.of("a", "b", "c")).build(),
                Question.builder().sequence(2).inputType(TEXT).isRequired(true).title("title2").label("label").build(),
                Question.builder().sequence(3).inputType(TEXT).isRequired(true).title("title3").label("label").build(),
                Question.builder().sequence(4).inputType(TEXT).isRequired(true).title("title4").label("label").build(),
                Question.builder().sequence(5).inputType(TEXT).isRequired(true).title("title5").label("label").build()
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

    @Test
    @DisplayName("지원서 문항 조회 시 redis에 캐싱")
    @AuthenticatedUser
    void find_questions_cache() throws Exception {
        // when
        mockMvc.perform(get("/apply/questions")
                        .param("jobFamily", "BE"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SUCCESS")))
                .andDo(print());

        // then
        Long countExistingKeys = redisTemplate.countExistingKeys(List.of("question::BE"));
        Assertions.assertThat(countExistingKeys).isEqualTo(1);
    }
}
