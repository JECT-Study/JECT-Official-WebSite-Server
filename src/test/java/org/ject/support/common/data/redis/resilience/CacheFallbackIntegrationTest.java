package org.ject.support.common.data.redis.resilience;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.ject.support.domain.recruit.domain.Question.InputType.TEXT;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import java.time.LocalDateTime;
import java.util.List;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.domain.recruit.domain.Question;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.dto.QuestionResponses;
import org.ject.support.domain.recruit.repository.QuestionRepository;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.ject.support.domain.recruit.service.QuestionService;
import org.ject.support.testconfig.IntegrationTest;
import org.ject.support.testconfig.RedisTestContainersConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@IntegrationTest
class CacheFallbackIntegrationTest {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private RecruitRepository recruitRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private RedisCacheCircuitBreakerProvider circuitBreakerProvider;

    private Recruit recruit;

    @BeforeEach
    void setUp() {
        String uniqueSuffix = String.valueOf(System.currentTimeMillis());
        List<Question> questions = List.of(
                Question.builder().sequence(1).inputType(TEXT).isRequired(true).title("title1").label("label").build()
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

        Member member = Member.create(
                "김젝트",
                "test" + uniqueSuffix + "@gmail.com",
                "01012345678",
                List.of(),
                null
        );
        memberRepository.save(member);
    }

    @AfterEach
    void restoreRedis() {
        RedisTestContainersConfig.start();
        circuitBreakerProvider.resetAll(); // 테스트 간 격리를 위해 서킷 브레이커 메트릭 초기화
    }

    @Test
    @DisplayName("레디스가 다운되어도 DB에서 데이터를 정상적으로 조회하고 서킷 브레이커가 동작한다")
    void shouldFallbackToDbWhenRedisIsDown() {
        // given
        RedisTestContainersConfig.stop(); // Redis 중단 시뮬레이션

        // when: 캐시가 적용된 서비스 메서드 호출
        // Redis가 죽어있으므로 CacheErrorHandler가 예외를 잡고, DB에서 데이터를 가져와야 함
        QuestionResponses response = questionService.findQuestions(recruit.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.questionResponses()).isNotEmpty();
        assertThat(response.questionResponses().get(0).title()).isEqualTo("title1");

        // 서킷 브레이커 상태 확인 최소 호출 횟수(5회)를 채워야 상태가 변하지만, 에러가 기록되었는지는 확인할 수 있음
        CircuitBreaker breaker = circuitBreakerProvider.get("question");
        assertThat(breaker.getMetrics().getNumberOfFailedCalls()).isPositive();
    }

    @Test
    @DisplayName("직렬화 오류와 같은 비인프라 예외는 서킷 브레이커에 기록되지 않고 즉시 발생해야 한다")
    void shouldNotSwallowNonInfrastructureExceptions() {
        // given: 직렬화 예외 발생 시나리오
        SerializationException serializationException = new SerializationException("Serialization failed");
        
        // expected
        // ErrorHandler가 이 예외를 다시 던지는지 확인
        ResilientCacheErrorHandler errorHandler = new ResilientCacheErrorHandler(circuitBreakerProvider);
        assertThatThrownBy(() -> errorHandler.handleCacheGetError(serializationException, null, "key"))
                .isSameAs(serializationException);
        
        // 서킷 브레이커에 실패가 기록되지 않았어야 함
        CircuitBreaker breaker = circuitBreakerProvider.get("question");
        long failedCallsBefore = breaker.getMetrics().getNumberOfFailedCalls();
        assertThat(failedCallsBefore).isZero(); // 이전 테스트의 영향 없이 0이어야 함
        
        // handleCacheGetError 호출 후에도 실패 횟수가 그대로인지 확인 (이미 위에서 exception이 던져졌으므로 기록 안 됨)
        assertThat(breaker.getMetrics().getNumberOfFailedCalls()).isEqualTo(failedCallsBefore);
    }
}
