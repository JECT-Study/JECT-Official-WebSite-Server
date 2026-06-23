package org.ject.support.admin.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;

import java.util.List;
import org.ject.support.admin.member.dto.request.CreateMemberSemesterRequest;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.member.entity.MemberActivity;
import org.ject.support.domain.member.exception.MemberErrorCode;
import org.ject.support.domain.member.exception.MemberException;
import org.ject.support.domain.member.repository.MemberActivityRepository;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminMemberActivityServiceTest {

    @Mock
    private MemberActivityRepository memberActivityRepository;

    @InjectMocks
    private AdminMemberActivityService adminMemberActivityService;

    @Test
    @DisplayName("동일 기수 활동이 없으면 일반 구성원 활동을 저장한다")
    void 동일_기수_활동이_없으면_일반_구성원_활동을_저장한다() {
        // given
        CreateMemberSemesterRequest request = createMemberSemesterRequest();
        Long memberId = 1L;
        given(memberActivityRepository.existsSemesterActivity(
            memberId,
            MemberType.SEMESTER,
            request.semesterId()
        )).willReturn(false);

        // when
        adminMemberActivityService.createMemberSemesterActivity(request, memberId);

        // then
        ArgumentCaptor<MemberActivity> captor = ArgumentCaptor.forClass(MemberActivity.class);
        verify(memberActivityRepository).save(captor.capture());

        MemberActivity memberActivity = captor.getValue();
        assertThat(memberActivity.getMemberId()).isEqualTo(memberId);
        assertThat(memberActivity.getJobFamily()).isEqualTo(request.jobFamily());
        assertThat(memberActivity.getRecruitTypeDetail()).isEqualTo(request.recruitTypeDetail());
        assertThat(memberActivity.getCareerDetails()).isEqualTo(request.careerDetails());
        assertThat(memberActivity.getExperiencePeriod()).isEqualTo(request.experiencePeriod());
        assertThat(memberActivity.getMemo()).isEqualTo(request.memo());
        assertThat(memberActivity.getMemberSemester().getSemesterId()).isEqualTo(request.semesterId());
        assertThat(memberActivity.getMemberSemester().getTeamId()).isEqualTo(request.teamId());
    }

    @Test
    @DisplayName("동일 기수 활동이 있으면 예외가 발생한다")
    void 동일_기수_활동이_있으면_예외가_발생한다() {
        // given
        CreateMemberSemesterRequest request = createMemberSemesterRequest();
        Long memberId = 1L;
        given(memberActivityRepository.existsSemesterActivity(
            memberId,
            MemberType.SEMESTER,
            request.semesterId()
        )).willReturn(true);

        // when
        Throwable throwable = catchThrowable(() ->
            adminMemberActivityService.createMemberSemesterActivity(request, memberId)
        );

        // then
        assertThat(throwable)
            .isInstanceOf(MemberException.class)
            .extracting("errorCode")
            .isEqualTo(MemberErrorCode.ALREADY_EXIST_MEMBER_SEMESTER_ACTIVITY);
        verify(memberActivityRepository, never()).save(any(MemberActivity.class));
    }

    private CreateMemberSemesterRequest createMemberSemesterRequest() {
        return new CreateMemberSemesterRequest(
            "김젝트",
            "jectkim@ject.kr",
            "01012345678",
            JobFamily.BE,
            RecruitTypeDetail.REGULAR,
            CareerDetails.EMPLOYEE,
            1L,
            2L,
            ExperiencePeriod.ONE_TO_TWO,
            "memo",
            List.of("HEALTHCARE", "FINTECH", "AI"),
            Region.SEOUL
        );
    }
}
