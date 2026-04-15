package org.ject.support;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.ject.support.admin.apply.repository.AdminApplyRepository;
import org.ject.support.admin.apply.service.AdminApplyService;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.domain.recruit.domain.Question;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.repository.QuestionRepository;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest // 실제 스프링 컨텍스트와 DB 커넥션을 띄웁니다.
class AdminApplyServiceIntegrationTest {

    @Autowired
    private AdminApplyService submittedApplyService;

    @Autowired
    private AdminApplyRepository adminApplyRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RecruitRepository recruitRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private QuestionRepository questionRepository;


    @Test
    @DisplayName("100건의 지원서를 IN 절을 통해 단 1회의 DB 쿼리로 소프트 삭제한다.")
    void 제출된_지원서_여러건_삭제_통합테스트() {
        // given: 실제 데이터베이스에 100건의 테스트 데이터 Insert
        Semester semester = semesterRepository.save(Semester.builder().name("1").build());



        Recruit recruit = recruitRepository.save(Recruit.builder()
                        .id(1L)
                .semester(semester)
                .recruitType(org.ject.support.domain.recruit.domain.RecruitType.REGULAR)
                .build());

        Question question1 = questionRepository.save(Question.builder().id(1L).recruit(recruit).build()); // ID는 DB가 자동 생성(Auto Increment)
        Question question2 = questionRepository.save(Question.builder().id(2L).recruit(recruit).build());

        List<Apply> appliesToSave = IntStream.rangeClosed(1, 100)
                .mapToObj(i -> {
                    // Cascade 옵션이 없다면 연관 엔티티도 명시적으로 먼저 저장해주어야 합니다.
                    char ch = (char) (0xAC00 + i);
                    Member member = memberRepository.save(Member.builder().name("김젝트" + ch).build());

                    return Apply.builder()
                            .member(member)
                            .recruit(recruit)
                            .status(ApplyStatus.SUBMITTED)
                            .applicationForm(ApplicationForm.builder().build())
                            .isDeleted(false) // 소프트 삭제 여부 초기값
                            .build();
                })
                .collect(Collectors.toList());

        adminApplyRepository.saveAll(appliesToSave);

        // 핵심 포인트 1: Insert 쿼리를 DB로 완전히 밀어내고 1차 캐시를 비워 실무 환경과 동일하게 만듭니다.
        entityManager.flush();
        entityManager.clear();

        List<Long> applyIds = appliesToSave.stream()
                .map(Apply::getId)
                .collect(Collectors.toList());

        // when: 실제 비즈니스 로직 실행 및 순수 쿼리 실행 시간 측정
        long startTime = System.currentTimeMillis();
        int deletedCount = submittedApplyService.deleteApplies(applyIds);
        long endTime = System.currentTimeMillis();

        // then: 콘솔에 성능 지표 출력 및 검증
        System.out.println("✅ 실제 DB 삭제 쿼리 실행 시간: " + (endTime - startTime) + "ms");

        assertThat(deletedCount).isEqualTo(100);

    }
}
