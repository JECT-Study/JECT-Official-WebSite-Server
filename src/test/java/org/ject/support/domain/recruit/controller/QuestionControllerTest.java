package org.ject.support.domain.recruit.controller;

import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.domain.recruit.domain.Question;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.repository.QuestionRepository;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.ject.support.testconfig.AuthenticatedUser;
import org.ject.support.testconfig.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.ject.support.domain.recruit.domain.Question.InputType.TEXT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
    SemesterRepository semesterRepository;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    Member member;
    Recruit recruit;

    @BeforeEach
    void setUp() {
        String uniqueSuffix = String.valueOf(System.nanoTime());
        List<Question> questions = List.of(
                Question.builder().sequence(1).inputType(TEXT).isRequired(true).title("title1").label("label").selectOptions(List.of("a", "b", "c")).build(),
                Question.builder().sequence(2).inputType(TEXT).isRequired(true).title("title2").label("label").build(),
                Question.builder().sequence(3).inputType(TEXT).isRequired(true).title("title3").label("label").build(),
                Question.builder().sequence(4).inputType(TEXT).isRequired(true).title("title4").label("label").build(),
                Question.builder().sequence(5).inputType(TEXT).isRequired(true).title("title5").label("label").build()
        );

        Semester savedSemester = semesterRepository.save(Semester.builder()
                .name("1기" + uniqueSuffix)
                .isRecruiting(true)
                .build());

        recruit = Recruit.builder()
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .semester(savedSemester)
                .jobFamily(JobFamily.BE)
                .build();

        for (Question question : questions) {
            recruit.addQuestion(question);
        }

        recruitRepository.save(recruit);

        member = Member.builder()
                .email("test32" + uniqueSuffix + "@gmail.com")
                .semesterId(1L)
                .jobFamily(JobFamily.BE)
                .name("김젝트")
                .role(Role.SEMESTER)
                .phoneNumber("01012345678")
                .pin("123456") // PIN 필드 추가
                .status(MemberStatus.ACTIVE)
                .build();
        memberRepository.save(member);
    }

    @Test
    @AuthenticatedUser
    void 지원서_문항_조회_시_redis에_캐싱한다() throws Exception {
        // when
        mockMvc.perform(get("/apply/questions")
                        .param("jobFamily", "BE"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SUCCESS")))
                .andDo(print());

        // then
        Long countExistingKeys = redisTemplate.countExistingKeys(List.of("cache::question::BE"));
        assertThat(countExistingKeys).isEqualTo(1);
    }

    @Test
    @AuthenticatedUser
    void 모집_공고_기준으로_지원서_문항을_조회한다() throws Exception {
        // when, then
        mockMvc.perform(get("/apply/questions")
                        .param("recruitId", recruit.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.questionResponses[0].title").value("title1"))
                .andExpect(jsonPath("$.data.questionResponses[4].title").value("title5"));
    }

    @Test
    @AuthenticatedUser
    void 모집_공고와_직군이_다르면_에러를_반환한다() throws Exception {
        // when, then
        mockMvc.perform(get("/apply/questions")
                        .param("recruitId", recruit.getId().toString())
                        .param("jobFamily", "FE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("RECRUIT-5"));
    }
}
