package org.ject.support.domain.recruit.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.dto.RecruitRegisterRequest;
import org.ject.support.domain.recruit.dto.SemesterRegistered;
import org.ject.support.domain.recruit.exception.RecruitErrorCode;
import org.ject.support.domain.recruit.exception.RecruitException;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecruitRegisterServiceTest extends UnitTestSupport {

    @Mock
    RecruitRepository recruitRepository;
    @Mock
    SemesterRepository semesterRepository;
    @InjectMocks
    RecruitRegisterService recruitRegisterService;

    @Test
    void 일반_구성원_공고는_지정한_기수에_등록한다() {
        // given
        Semester semester = Semester.builder().id(1L).name("1기").build();
        when(semesterRepository.findById(1L)).thenReturn(Optional.of(semester));
        RecruitRegisterRequest request = request(RecruitType.SEMESTER, JobFamily.BE, 1L);

        // when
        recruitRegisterService.registerRecruits(List.of(request));

        // then
        ArgumentCaptor<List<Recruit>> captor = ArgumentCaptor.forClass(List.class);
        verify(recruitRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).singleElement().satisfies(recruit -> {
            assertThat(recruit.getSemester()).isSameAs(semester);
            assertThat(recruit.getRecruitType()).isEqualTo(RecruitType.SEMESTER);
            assertThat(recruit.getTitle()).isEqualTo("모집공고");
        });
    }

    @Test
    void 같은_조건의_미종료_공고가_있으면_등록할_수_없다() {
        // given
        when(recruitRepository.existsDuplicateRecruit(any(), any(), any(), any())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> recruitRegisterService.registerRecruits(
                List.of(request(RecruitType.MAKERS, JobFamily.BE, null))))
                .isInstanceOf(RecruitException.class)
                .hasFieldOrPropertyWithValue("errorCode", RecruitErrorCode.DUPLICATED_JOB_FAMILY);
    }

    private RecruitRegisterRequest request(RecruitType type, JobFamily jobFamily, Long semesterId) {
        return new RecruitRegisterRequest("모집공고", jobFamily, LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1), null, null, null, List.of(), type,
                type == RecruitType.SEMESTER ? RecruitTypeDetail.REGULAR : RecruitTypeDetail.NEW, semesterId);
    }
}
