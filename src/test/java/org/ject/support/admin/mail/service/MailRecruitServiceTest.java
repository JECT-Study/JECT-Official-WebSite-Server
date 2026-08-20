package org.ject.support.admin.mail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.List;
import org.ject.support.admin.mail.dto.MailRecruitResponse;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

class MailRecruitServiceTest extends UnitTestSupport {

    @InjectMocks
    private MailRecruitService mailRecruitService;

    @Mock
    private RecruitRepository recruitRepository;

    @Test
    void 모집_공고를_메일_발송용_응답으로_변환한다() {
        // given
        LocalDateTime startDate = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2026, 8, 31, 23, 59);
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 1, 10, 0);
        Semester semester = Semester.builder().id(2L).name("10기").isRecruiting(true).build();
        Recruit recruit = Recruit.builder()
                .id(1L)
                .semester(semester)
                .startDate(startDate)
                .endDate(endDate)
                .jobFamily(JobFamily.BE)
                .recruitType(RecruitType.SEMESTER)
                .recruitTypeDetail(RecruitTypeDetail.REGULAR)
                .build();
        ReflectionTestUtils.setField(recruit, "createdAt", createdAt);
        given(recruitRepository.findAllForMailDispatch()).willReturn(List.of(recruit));

        // when
        List<MailRecruitResponse> result = mailRecruitService.getRecruits();

        // then
        assertThat(result).singleElement().satisfies(response -> {
            assertThat(response.recruitId()).isEqualTo(1L);
            assertThat(response.semesterId()).isEqualTo(2L);
            assertThat(response.semesterName()).isEqualTo("10기");
            assertThat(response.jobFamily()).isEqualTo(JobFamily.BE);
            assertThat(response.jobFamilyDescription()).isEqualTo("백엔드 개발자(BE)");
            assertThat(response.recruitType()).isEqualTo(RecruitType.SEMESTER);
            assertThat(response.recruitTypeDescription()).isEqualTo("정규 기수 모집");
            assertThat(response.recruitTypeDetail()).isEqualTo(RecruitTypeDetail.REGULAR);
            assertThat(response.recruitTypeDetailDescription()).isEqualTo("정규 모집");
            assertThat(response.startDate()).isEqualTo(startDate);
            assertThat(response.endDate()).isEqualTo(endDate);
            assertThat(response.createdAt()).isEqualTo(createdAt);
        });
    }
}
