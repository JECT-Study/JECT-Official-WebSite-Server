package org.ject.support.domain.recruit.repository;

import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.exception.RecruitErrorCode;
import org.ject.support.domain.recruit.exception.RecruitException;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.ject.support.domain.member.JobFamily.BE;
import static org.ject.support.domain.member.JobFamily.FE;
import static org.ject.support.domain.member.JobFamily.PM;
import static org.ject.support.domain.member.JobFamily.SUPPORTER;

@Import(QueryDslTestConfig.class)
@DataJpaTest
class RecruitRepositoryTest {

    @Autowired
    private RecruitRepository recruitRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Test
    void 모집_공고에_연결된_기수를_조회한다() {
        // given
        Semester savedSemester = semesterRepository.save(
                Semester.builder().name("5기").isRecruiting(true).build());
        Recruit savedRecruit = recruitRepository.save(Recruit.builder()
                .semester(savedSemester)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .jobFamily(BE)
                .build());

        // when
        Long semesterId = semesterRepository.findSemesterIdByRecruitId(savedRecruit.getId())
                .orElseThrow();

        // then
        assertThat(semesterId).isEqualTo(savedSemester.getId());
    }

    @Test
    void 특정_직군의_마감되지_않은_모집_정보가_존재하면_true를_반환한다() {
        // given
        Semester savedSemester = semesterRepository.save(Semester.builder().name("3기").isRecruiting(true).build());

        recruitRepository.saveAll(
                List.of(
                        Recruit.builder()
                                .semester(savedSemester)
                                .startDate(LocalDateTime.now().minusDays(1))
                                .endDate(LocalDateTime.now().plusDays(1))
                                .jobFamily(PM)
                                .build(),
                        Recruit.builder()
                                .semester(savedSemester)
                                .startDate(LocalDateTime.now().minusDays(1))
                                .endDate(LocalDateTime.now().plusDays(1))
                                .jobFamily(BE)
                                .build()
                )
        );

        // when
        boolean result = recruitRepository.existsByJobFamilyAndIsNotClosed(savedSemester.getId(), List.of(FE, BE));

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 활성_모집_공고만_시작일_순서로_조회한다() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Semester savedSemester = semesterRepository.save(Semester.builder().name("3기").isRecruiting(true).build());
        Recruit earlierActiveRecruit = recruitRepository.save(Recruit.builder()
                .semester(savedSemester)
                .startDate(now.minusDays(2))
                .endDate(now.plusDays(2))
                .jobFamily(BE)
                .recruitType(RecruitType.SEMESTER)
                .recruitTypeDetail(RecruitTypeDetail.REGULAR)
                .build());
        Recruit laterActiveRecruit = recruitRepository.save(Recruit.builder()
                .semester(savedSemester)
                .startDate(now.minusDays(1))
                .endDate(now.plusDays(2))
                .jobFamily(SUPPORTER)
                .recruitType(RecruitType.SUPPORTERS)
                .recruitTypeDetail(RecruitTypeDetail.REFILL)
                .build());
        recruitRepository.save(Recruit.builder()
                .semester(savedSemester)
                .startDate(now.minusDays(4))
                .endDate(now.minusDays(1))
                .jobFamily(FE)
                .recruitType(RecruitType.MAKERS)
                .recruitTypeDetail(RecruitTypeDetail.NEW)
                .build());
        recruitRepository.save(Recruit.builder()
                .semester(savedSemester)
                .startDate(now.plusDays(1))
                .endDate(now.plusDays(2))
                .jobFamily(PM)
                .recruitType(RecruitType.SEMESTER)
                .recruitTypeDetail(RecruitTypeDetail.REFILL)
                .build());

        // when
        List<Recruit> result = recruitRepository.findActiveRecruitments(now);

        // then
        assertThat(result)
                .extracting(Recruit::getId)
                .containsExactly(earlierActiveRecruit.getId(), laterActiveRecruit.getId());
        assertThat(result.get(0).getSemester().getName()).isEqualTo("3기");
    }

    @Test
    void 허용되지_않은_모집_유형과_모집_사유_조합은_저장할_수_없다() {
        // given
        Semester savedSemester = semesterRepository.save(Semester.builder().name("3기").isRecruiting(true).build());
        Recruit recruit = Recruit.builder()
                .semester(savedSemester)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .jobFamily(BE)
                .recruitType(RecruitType.MAKERS)
                .recruitTypeDetail(RecruitTypeDetail.REGULAR)
                .build();

        // when & then
        assertThatThrownBy(() -> recruitRepository.saveAndFlush(recruit))
                .isInstanceOf(RecruitException.class)
                .hasFieldOrPropertyWithValue("errorCode", RecruitErrorCode.INVALID_RECRUIT_TYPE_DETAIL);
    }
}
