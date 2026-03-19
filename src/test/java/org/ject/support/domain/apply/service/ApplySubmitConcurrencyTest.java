package org.ject.support.domain.apply.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.repository.ApplicationFormRepository;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.domain.recruit.domain.Question;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ApplySubmitConcurrencyTest {

    @Autowired
    private ApplyService applyService;

    @Autowired
    private ApplyRepository applyRepository;

    @Autowired
    private ApplicationFormRepository applicationFormRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RecruitRepository recruitRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    private Long memberId;
    private JobFamily jobFamily;
    private Map<String, String> answers;

    @BeforeEach
    void setUp() {
        // 기존 데이터 정리 (테스트 격리)
        applicationFormRepository.deleteAll();
        applyRepository.deleteAll();
        recruitRepository.deleteAll();
        memberRepository.deleteAll();
        semesterRepository.deleteAll();

        jobFamily = JobFamily.BE;

        // 1. Semester 생성
        Semester semester = semesterRepository.save(
                Semester.builder()
                        .name("test1")
                        .build()
        );

        // 2. Member 생성
        Member member = memberRepository.save(
                Member.builder()
                        .email("test@test.com")
                        .name("테스터")
                        .phoneNumber("01012345678")
                        .role(Role.APPLY)
                        .status(MemberStatus.ACTIVE)
                        .semesterId(semester.getId())
                        .jobFamily(jobFamily)
                        .build()
        );
        memberId = member.getId();

        // 3. Recruit 생성 (현재 활성 모집) + Question (CASCADE로 함께 저장)
        Recruit recruit = Recruit.builder()
                .semester(semester)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .jobFamily(jobFamily)
                .build();

        Question question = Question.builder()
                .sequence(1)
                .inputType(Question.InputType.TEXT)
                .isRequired(true)
                .title("Q1")
                .label("label1")
                .build();
        recruit.addQuestion(question);
        recruitRepository.save(recruit);

        // 4. Apply 생성 (TEMP_SAVED) + ApplicationForm (제출 시 업데이트 대상)
        Apply apply = Apply.createApply(member, recruit);
        apply.updateStatus(ApplyStatus.TEMP_SAVED);
        applyRepository.save(apply);

        ApplicationForm applicationForm = ApplicationForm.builder()
                .apply(apply)
                .content("{\"" + question.getId() + "\":\"temp\"}")
                .build();
        applicationFormRepository.save(applicationForm);
        apply.updateApplicationForm(applicationForm);
        applyRepository.save(apply);

        // 5. answers
        answers = Map.of(String.valueOf(question.getId()), "answer1");
    }

    @Test
    @DisplayName("동시 제출 시 Race Condition 발생 — 락 없이 두 요청 모두 성공하면 버그")
    void 동시_제출_시_하나만_성공해야_한다() throws InterruptedException {
        int threadCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await();

                    applyService.submitApplication(memberId, jobFamily, answers, List.of());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println("[" + Thread.currentThread().getName() + "] 예외: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        doneLatch.await();
        executorService.shutdown();

        System.out.println("=== 동시성 테스트 결과 ===");
        System.out.println("성공: " + successCount.get());
        System.out.println("실패: " + failCount.get());

        // ✅ 비관적 락 적용 후: 정확히 1건만 성공, 1건은 실패
        // → 이 테스트가 PASS하면 Race Condition이 해결되었다는 증거
        assertThat(successCount.get())
                .as("비관적 락 적용 후: 정확히 1건만 성공해야 합니다.")
                .isEqualTo(1);
    }
}
