package org.ject.support.admin.apply.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import org.ject.support.admin.apply.dto.TempApplyDetailResponse;
import org.ject.support.admin.apply.dto.TempSavedApplyCountResponse;
import org.ject.support.admin.apply.dto.TempSavedApplyResponse;
import org.ject.support.admin.apply.repository.AdminApplyQueryRepository;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.util.String2MapSerializer;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class AdminTempApplyServiceTest extends UnitTestSupport {

    @InjectMocks
    private AdminTempApplyService adminTempApplyService;

    @Mock
    private ApplyRepository applyRepository;

    @Mock
    private AdminApplyQueryRepository adminApplyQueryRepository;

    @Mock
    private String2MapSerializer string2MapSerializer;

    @Test
    void 존재하지_않는_임시_저장된_지원서를_조회하면_예외_발생() {
        // given
        Long tempApplyId = 1L;
        given(applyRepository.findByIdAndStatusWithMember(tempApplyId, ApplyStatus.TEMP_SAVED))
                .willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> adminTempApplyService.getTempApplyDetail(tempApplyId))
                .isInstanceOf(ApplyException.class);
    }

    @Test
    void 임시_저장된_지원서를_상세_조회한다() {
        // given
        Long tempApplyId = 1L;
        Member member = Member.builder()
                .name("김젝트")
                .build();
        Semester semester = Semester.builder()
                .name("1")
                .build();
        Recruit recruit = Recruit.builder()
                .semester(semester)
                .build();
        Apply submittedApply = Apply.builder()
                .id(tempApplyId)
                .member(member)
                .recruit(recruit)
                .status(ApplyStatus.TEMP_SAVED)
                .applicationForm(ApplicationForm.builder().build())
                .build();
        given(applyRepository.findByIdAndStatusWithMember(tempApplyId, ApplyStatus.TEMP_SAVED))
                .willReturn(Optional.of(submittedApply));

        // when
        TempApplyDetailResponse result = adminTempApplyService.getTempApplyDetail(tempApplyId);

        // then
        verify(applyRepository).findByIdAndStatusWithMember(tempApplyId, ApplyStatus.TEMP_SAVED);
        assertThat(result.applyId()).isEqualTo(tempApplyId);
    }

    @Test
    void 임시_저장된_지원서의_총_개수를_조회한다() {
        // given
        ApplyStatus targetStatus = ApplyStatus.TEMP_SAVED;
        Long tempSavedCount = 5L;
        given(applyRepository.countByStatus(targetStatus))
                .willReturn(tempSavedCount);

        // when
        TempSavedApplyCountResponse result = adminTempApplyService.getTempSavedApplyCount();

        // then
        verify(applyRepository).countByStatus(targetStatus);
        assertThat(result).isEqualTo(new TempSavedApplyCountResponse(tempSavedCount));
    }

    @Test
    void 임시_저장된_지원서의_총_개수가_0개일_경우_0을_조회한다() {
        // given
        ApplyStatus targetStatus = ApplyStatus.TEMP_SAVED;
        Long tempSavedCount = 0L;
        given(applyRepository.countByStatus(targetStatus))
                .willReturn(tempSavedCount);

        // when
        TempSavedApplyCountResponse result = adminTempApplyService.getTempSavedApplyCount();

        // then
        verify(applyRepository).countByStatus(targetStatus);
        assertThat(result).isEqualTo(new TempSavedApplyCountResponse(tempSavedCount));
    }

    @Test
    void 존재하지_않는_임시_저장_상태의_지원서를_삭제_할_경우_예외_발생() {
        // given
        Long tempApplyId = 1L;
        given(applyRepository.findByIdAndStatusWithMember(tempApplyId, ApplyStatus.TEMP_SAVED))
                .willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> adminTempApplyService.deleteTempApply(tempApplyId))
                .isInstanceOf(ApplyException.class);
    }

    @Test
    void 임시_저장된_지원서를_삭제한다() {
        // given
        Long tempApplyId = 1L;
        Member member = Member.builder()
                .name("김젝트")
                .build();
        Semester semester = Semester.builder()
                .name("1")
                .build();
        Recruit recruit = Recruit.builder()
                .semester(semester)
                .build();
        ApplicationForm applicationForm = ApplicationForm.builder()
                .content("{}")
                .build();
        Apply tempSavedApply = Apply.builder()
                .id(tempApplyId)
                .member(member)
                .recruit(recruit)
                .status(ApplyStatus.TEMP_SAVED)
                .applicationForm(applicationForm)
                .build();
        given(applyRepository.findByIdAndStatusWithMember(tempApplyId, ApplyStatus.TEMP_SAVED))
                .willReturn(Optional.of(tempSavedApply));

        // when
        adminTempApplyService.deleteTempApply(tempApplyId);

        // then
        verify(applyRepository).findByIdAndStatusWithMember(tempApplyId, ApplyStatus.TEMP_SAVED);
        verify(applyRepository).delete(tempSavedApply);
    }

    @Test
    void 임시_저장된_지원서를_조회할때_결과가_없으면_빈페이지_반환한다() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Long semesterId = null;
        Page<Apply> applyPage = new PageImpl<>(List.of(), pageable, 0);

        given(adminApplyQueryRepository.findAppliesByStatus(ApplyStatus.TEMP_SAVED, semesterId, null, null, pageable))
                .willReturn(applyPage);

        // when
        Page<TempSavedApplyResponse> result = adminTempApplyService.getTempApplies(null, semesterId, pageable);

        // then
        verify(adminApplyQueryRepository).findAppliesByStatus(ApplyStatus.TEMP_SAVED, semesterId, null, null, pageable);
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void 임시_저장된_지원서를_조회_한다() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Long semesterId = null;

        Member m1 = Member.builder().name("김1").build();
        Member m2 = Member.builder().name("김2").build();
        Semester semester = Semester.builder().name("1").build();
        Recruit recruit = Recruit.builder().semester(semester).build();

        ApplicationForm form1 = ApplicationForm.builder().content("{}").build();
        ApplicationForm form2 = ApplicationForm.builder().content("{}").build();

        Apply a1 = Apply.builder()
                .id(1L)
                .member(m1)
                .recruit(recruit)
                .status(ApplyStatus.TEMP_SAVED)
                .applicationForm(form1)
                .build();

        Apply a2 = Apply.builder()
                .id(2L)
                .member(m2)
                .recruit(recruit)
                .status(ApplyStatus.TEMP_SAVED)
                .applicationForm(form2)
                .build();

        List<Apply> applies = List.of(a1, a2);
        Page<Apply> applyPage = new PageImpl<>(applies, pageable, applies.size());

        given(adminApplyQueryRepository.findAppliesByStatus(ApplyStatus.TEMP_SAVED, semesterId, null, null, pageable))
                .willReturn(applyPage);

        // applicationForm.content가 "{}" 이므로 이 호출을 stub 처리
        given(string2MapSerializer.serializeAsMap("{}")).willReturn(java.util.Map.of());

        Page<TempSavedApplyResponse> result =
                adminTempApplyService.getTempApplies(null, semesterId, pageable);

        verify(adminApplyQueryRepository).findAppliesByStatus(ApplyStatus.TEMP_SAVED, semesterId, null, null, pageable);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).applyId()).isEqualTo(1L);
        assertThat(result.getContent().get(1).applyId()).isEqualTo(2L);
    }

    @Test
    void 임시_저장된_지원서를_semesterId로_필터링하여_조회한다() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Long semesterId = 1L;

        Member member = Member.builder().name("김젝트").build();
        Semester semester = Semester.builder().name("1").build();
        Recruit recruit = Recruit.builder().semester(semester).build();
        ApplicationForm form = ApplicationForm.builder().content("{}").build();

        Apply apply = Apply.builder()
                .id(1L)
                .member(member)
                .recruit(recruit)
                .status(ApplyStatus.TEMP_SAVED)
                .applicationForm(form)
                .build();

        List<Apply> applies = List.of(apply);
        Page<Apply> applyPage = new PageImpl<>(applies, pageable, applies.size());

        given(adminApplyQueryRepository.findAppliesByStatus(ApplyStatus.TEMP_SAVED, semesterId, null, null, pageable))
                .willReturn(applyPage);
        given(string2MapSerializer.serializeAsMap("{}")).willReturn(java.util.Map.of());

        // when
        Page<TempSavedApplyResponse> result = adminTempApplyService.getTempApplies(null, semesterId, pageable);

        // then
        verify(adminApplyQueryRepository).findAppliesByStatus(ApplyStatus.TEMP_SAVED, semesterId, null, null, pageable);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).applyId()).isEqualTo(1L);
    }
}
