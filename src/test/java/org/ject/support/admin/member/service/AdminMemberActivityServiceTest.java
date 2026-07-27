package org.ject.support.admin.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.verify;

import java.util.List;
import java.util.Optional;

import org.ject.support.admin.member.dto.projection.MemberMakersDetailProjection;
import org.ject.support.admin.member.dto.projection.SearchMemberSemesterProjection;
import org.ject.support.admin.member.dto.request.CreateMemberMakersRequest;
import org.ject.support.admin.member.dto.request.CreateMemberSemesterRequest;
import org.ject.support.admin.member.dto.request.CreateMemberSupportersRequest;
import org.ject.support.admin.member.dto.request.MemberSemesterSearchCondition;
import org.ject.support.admin.member.dto.result.SearchMemberSemesterPageResult;
import org.ject.support.common.response.CursorPageResponse;
import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.Availability;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.CareerLevel;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MakersTeam;
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

    /**
     * 일반 구성원 추가 테스트
     */
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

    @Test
    @DisplayName("메이커스팀으로 활동 중인 이력이 없으면 신규 메이커스팀 활동 이력을 생성한다")
    void 메이커스팀으로_활동_중인_이력이_없으면_신규_메이커스팀_활동_이력을_생성한다() {
        // given
        CreateMemberMakersRequest request = createMemberMakersRequest();
        Long memberId = 1L;
        given(memberActivityRepository.existsActiveMakersActivityByMemberId(memberId)).willReturn(false);

        // when
        adminMemberActivityService.createMemberMakersActivity(request, memberId);

        // then
        ArgumentCaptor<MemberActivity> captor = ArgumentCaptor.forClass(MemberActivity.class);
        verify(memberActivityRepository).save(captor.capture());

        MemberActivity memberActivity = captor.getValue();
        assertThat(memberActivity.getMemberId()).isEqualTo(memberId);
        assertThat(memberActivity.getMemberType()).isEqualTo(MemberType.MAKERS);
        assertThat(memberActivity.getJobFamily()).isEqualTo(request.jobFamily());
        assertThat(memberActivity.getRecruitTypeDetail()).isEqualTo(request.recruitTypeDetail());
        assertThat(memberActivity.getCareerDetails()).isEqualTo(request.careerDetails());
        assertThat(memberActivity.getExperiencePeriod()).isEqualTo(request.experiencePeriod());
        assertThat(memberActivity.getMemo()).isEqualTo(request.memo());
        assertThat(memberActivity.getMemberMakers().getMakersTeam()).isEqualTo(request.makersTeam());
        assertThat(memberActivity.getMemberMakers().getMentoringAvailability()).isEqualTo(request.mentoringAvailability());
        assertThat(memberActivity.getMemberMakers().getProjectSupplementAvailability()).isEqualTo(request.projectSupplementAvailability());
        assertThat(memberActivity.getMemberMakers().getSpeakerAvailability()).isEqualTo(request.speakerAvailability());
        assertThat(memberActivity.getMemberMakers().getCareerLevel()).isEqualTo(request.careerLevel());
        assertThat(memberActivity.getMemberMakers().getSkills()).isEqualTo(request.skills());
        assertThat(memberActivity.getMemberMakers().getCompany()).isEqualTo(request.company());
        assertThat(memberActivity.getMemberMakers().getExpertTopics()).isEqualTo(request.expertTopics());
        assertThat(memberActivity.getMemberMakers().getActivityCertNumber()).isEqualTo(request.activityCertNumber());
    }

    @Test
    @DisplayName("메이커스팀으로 활동 중인 이력이 있으면 예외가 발생한다")
    void 메이커스팀으로_활동_중인_이력이_있으면_예외가_발생한다() {
        // given
        CreateMemberMakersRequest request = createMemberMakersRequest();
        Long memberId = 1L;
        given(memberActivityRepository.existsActiveMakersActivityByMemberId(memberId)).willReturn(true);

        // when
        Throwable throwable = catchThrowable(() ->
            adminMemberActivityService.createMemberMakersActivity(request, memberId)
        );

        // then
        assertThat(throwable)
            .isInstanceOf(MemberException.class)
            .extracting("errorCode")
            .isEqualTo(MemberErrorCode.ALREADY_EXIST_ACTIVE_MEMBER_MAKERS_ACTIVITY);
        verify(memberActivityRepository, never()).save(any(MemberActivity.class));
    }

    @Test
    @DisplayName("운영 서포터즈로 활동 중인 이력이 없으면 신규 운영 서포터즈 활동 이력을 생성한다")
    void 운영_서포터즈로_활동_중인_이력이_없으면_신규_운영_서포터즈_활동_이력을_생성한다() {
        // given
        CreateMemberSupportersRequest request = createMemberSupportersRequest();
        Long memberId = 1L;

        // when
        adminMemberActivityService.createMemberSupportersActivity(request, memberId);

        // then
        ArgumentCaptor<MemberActivity> captor = ArgumentCaptor.forClass(MemberActivity.class);
        verify(memberActivityRepository).save(captor.capture());

        MemberActivity memberActivity = captor.getValue();
        assertThat(memberActivity.getMemberId()).isEqualTo(memberId);
        assertThat(memberActivity.getMemberType()).isEqualTo(MemberType.SUPPORTERS);
        assertThat(memberActivity.getJobFamily()).isEqualTo(request.jobFamily());
        assertThat(memberActivity.getRecruitTypeDetail()).isEqualTo(request.recruitTypeDetail());
        assertThat(memberActivity.getActivityStatus()).isEqualTo(request.activityStatus());
        assertThat(memberActivity.getStartDate()).isEqualTo(request.startDate());
        assertThat(memberActivity.getEndDate()).isEqualTo(request.endDate());
        assertThat(memberActivity.getMemo()).isEqualTo(request.memo());
        assertThat(memberActivity.getMemberSupporters().getActivityCertNumber()).isEqualTo(request.activityCertNumber());
        verify(memberActivityRepository, never()).existsActiveSupportersActivityByMemberId(memberId);
    }

    @Test
    @DisplayName("활동 중 상태로 운영 서포터즈를 추가할 때 기존 활동 중 이력이 있으면 예외가 발생한다")
    void 활동_중_상태로_운영_서포터즈를_추가할_때_기존_활동_중_이력이_있으면_예외가_발생한다() {
        // given
        CreateMemberSupportersRequest original = createMemberSupportersRequest();
        CreateMemberSupportersRequest request = new CreateMemberSupportersRequest(
            original.name(),
            original.phoneNumber(),
            original.email(),
            original.memberType(),
            original.jobFamily(),
            original.recruitTypeDetail(),
            ActivityStatus.ACTIVE,
            original.startDate(),
            original.endDate(),
            original.activityCertNumber(),
            original.memo()
        );
        Long memberId = 1L;
        given(memberActivityRepository.existsActiveSupportersActivityByMemberId(memberId)).willReturn(true);

        // when
        Throwable throwable = catchThrowable(() ->
            adminMemberActivityService.createMemberSupportersActivity(request, memberId)
        );

        // then
        assertThat(throwable)
            .isInstanceOf(MemberException.class)
            .extracting("errorCode")
            .isEqualTo(MemberErrorCode.ALREADY_EXIST_ACTIVE_MEMBER_SUPPORTERS_ACTIVITY);
        verify(memberActivityRepository, never()).save(any(MemberActivity.class));
    }

    @Test
    @DisplayName("운영 서포터즈가 아닌 구성원 유형이면 활동 이력을 생성하지 않는다")
    void 운영_서포터즈가_아닌_구성원_유형이면_활동_이력을_생성하지_않는다() {
        // given
        CreateMemberSupportersRequest original = createMemberSupportersRequest();
        CreateMemberSupportersRequest request = new CreateMemberSupportersRequest(
            original.name(),
            original.phoneNumber(),
            original.email(),
            MemberType.MAKERS,
            original.jobFamily(),
            original.recruitTypeDetail(),
            original.activityStatus(),
            original.startDate(),
            original.endDate(),
            original.activityCertNumber(),
            original.memo()
        );

        // when
        Throwable throwable = catchThrowable(() ->
            adminMemberActivityService.createMemberSupportersActivity(request, 1L)
        );

        // then
        assertThat(throwable)
            .isInstanceOf(MemberException.class)
            .extracting("errorCode")
            .isEqualTo(MemberErrorCode.INVALID_MEMBER_TYPE);
        verify(memberActivityRepository, never()).save(any(MemberActivity.class));
    }

    /**
     * 일반 구성원 목록 조회 테스트
     */

    @Test
    @DisplayName("단일 필터를 적용해서 일반 구성원 목록을 조회한다")
    void 단일_필터를_적용해서_일반_구성원_목록을_조회한다() {
        // given
        MemberSemesterSearchCondition condition = new MemberSemesterSearchCondition(
          null,
          20,
          1L,
          JobFamily.BE,
          RecruitTypeDetail.REGULAR,
          CareerDetails.EMPLOYEE,
          1L,
          ActivityStatus.ACTIVE
        );

        List<SearchMemberSemesterProjection> projections = List.of(
            new SearchMemberSemesterProjection(
                1L,
                "김젝트",
                JobFamily.BE,
                "01012345678",
                CareerDetails.EMPLOYEE,
                ExperiencePeriod.ONE_TO_TWO,
                ActivityStatus.ACTIVE
            )
        );
        given(memberActivityRepository.searchMemberSemesters(condition, condition.getSizeOrDefault()+1)).willReturn(projections);
        given(memberActivityRepository.countMemberSemesters(condition)).willReturn(1L);

        // when
        SearchMemberSemesterPageResult pageResult = adminMemberActivityService.searchMemberSemesterList(condition);
        // then
        assertThat(pageResult.content()).isEqualTo(projections);
        assertThat(pageResult.totalCount()).isEqualTo(1L);
        verify(memberActivityRepository).searchMemberSemesters(condition, condition.getSizeOrDefault()+1);
        verify(memberActivityRepository).countMemberSemesters(condition);
    }

    @Test
    @DisplayName("메이커스팀 구성원 상세를 조회한다")
    void 메이커스팀_구성원_상세를_조회한다() {
        // given
        Long memberActivityId = 1L;
        MemberMakersDetailProjection projection = memberMakersDetailProjection(memberActivityId);
        given(memberActivityRepository.findMemberMakersDetail(memberActivityId)).willReturn(Optional.of(projection));

        // when
        MemberMakersDetailProjection result = adminMemberActivityService.getMemberMakersDetail(memberActivityId);

        // then
        assertThat(result).isEqualTo(projection);
        verify(memberActivityRepository).findMemberMakersDetail(memberActivityId);
    }

    @Test
    @DisplayName("메이커스팀 구성원 상세가 없으면 예외가 발생한다")
    void 메이커스팀_구성원_상세가_없으면_예외가_발생한다() {
        // given
        Long memberActivityId = 1L;
        given(memberActivityRepository.findMemberMakersDetail(memberActivityId)).willReturn(Optional.empty());

        // when
        Throwable throwable = catchThrowable(() -> adminMemberActivityService.getMemberMakersDetail(memberActivityId));

        // then
        assertThat(throwable)
            .isInstanceOf(MemberException.class)
            .extracting("errorCode")
            .isEqualTo(MemberErrorCode.NOT_FOUND_MEMBER);
        verify(memberActivityRepository).findMemberMakersDetail(memberActivityId);
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

    private CreateMemberMakersRequest createMemberMakersRequest() {
        return new CreateMemberMakersRequest(
            "김메이커",
            "maker@ject.kr",
            "01087654321",
            JobFamily.FE,
            CareerDetails.EMPLOYEE,
            MakersTeam.TEAM_1,
            RecruitTypeDetail.REGULAR,
            Region.SEOUL,
            List.of("HEALTHCARE", "FINTECH", "AI"),
            ExperiencePeriod.ONE_TO_TWO,
            Availability.HIGHLY_AVAILABLE,
            Availability.AVAILABLE_BY_TOPIC,
            Availability.CONSIDER_LATER,
            CareerLevel.JUNIOR,
            "Spring",
            "JECT",
            "백오피스",
            "MK-001",
            "memo"
        );
    }

    private CreateMemberSupportersRequest createMemberSupportersRequest() {
        return new CreateMemberSupportersRequest(
            "김서포터",
            "01012341234",
            "supporter@ject.kr",
            MemberType.SUPPORTERS,
            JobFamily.OPS,
            RecruitTypeDetail.REGULAR,
            ActivityStatus.ENDED,
            java.time.LocalDate.of(2025, 5, 19),
            java.time.LocalDate.of(2025, 12, 19),
            "SP-001",
            "memo"
        );
    }

    private MemberMakersDetailProjection memberMakersDetailProjection(Long memberActivityId) {
        return new MemberMakersDetailProjection(
            memberActivityId,
            "김메이커",
            "maker@ject.kr",
            "01087654321",
            JobFamily.FE,
            CareerDetails.EMPLOYEE,
            MakersTeam.TEAM_1,
            RecruitTypeDetail.REGULAR,
            Region.SEOUL,
            List.of("HEALTHCARE"),
            ExperiencePeriod.ONE_TO_TWO,
            Availability.HIGHLY_AVAILABLE,
            Availability.AVAILABLE_BY_TOPIC,
            Availability.CONSIDER_LATER,
            CareerLevel.JUNIOR,
            ActivityStatus.ACTIVE,
            "Spring",
            "JECT",
            "백오피스",
            "MK-001",
            "memo"
        );
    }


}
