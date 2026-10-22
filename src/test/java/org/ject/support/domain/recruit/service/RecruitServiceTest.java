package org.ject.support.domain.recruit.service;

import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.RecruitFaq;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.dto.ActiveRecruitmentResponses;
import org.ject.support.domain.recruit.dto.ApplyManagementRecruitProjection;
import org.ject.support.domain.recruit.dto.ApplyManagementRecruitSearchCondition;
import org.ject.support.domain.recruit.dto.ApplyManagementRecruitsResponse;
import org.ject.support.domain.recruit.dto.RecruitRegisterRequest;
import org.ject.support.domain.recruit.dto.RecruitResponse;
import org.ject.support.domain.recruit.dto.RecruitUpdateRequest;
import org.ject.support.domain.recruit.dto.RecruitUpdatedEvent;
import org.ject.support.domain.recruit.dto.RecruitTypeCountResponse;
import org.ject.support.domain.recruit.exception.RecruitException;
import org.ject.support.domain.recruit.repository.RecruitRepository;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.ject.support.domain.member.JobFamily.BE;
import static org.ject.support.domain.member.JobFamily.FE;
import static org.ject.support.domain.member.JobFamily.SUPPORTER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecruitServiceTest extends UnitTestSupport {

    @InjectMocks
    RecruitService recruitService;

    @Mock
    RecruitRepository recruitRepository;

    @Mock
    SemesterRepository semesterRepository;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("임시저장 지원서 화면에서도 공고와 제출 통계를 조회한다")
    void 임시저장_지원서_화면에서도_공고와_제출_통계를_조회한다() {
        // given
        ApplyManagementRecruitSearchCondition condition = searchCondition(
                ApplyStatus.TEMP_SAVED, RecruitType.MAKERS, Set.of(FE), null, 20);
        ApplyManagementRecruitProjection projection = recruitProjection(
                1L, RecruitType.MAKERS, FE, 10L, 2L, 6L, 1L);
        when(recruitRepository.searchApplyManagementRecruits(condition, 21)).thenReturn(List.of(projection));
        when(recruitRepository.countApplyManagementRecruits(condition)).thenReturn(1L);
        when(recruitRepository.countRecruitsByRecruitType()).thenReturn(List.of(
                new RecruitTypeCountResponse(RecruitType.MAKERS, 1L)));

        // when
        ApplyManagementRecruitsResponse response = recruitService.searchApplyManagementRecruits(condition);

        // then
        assertThat(response.recruits().content()).singleElement().satisfies(recruit -> {
            assertThat(recruit.title()).isEqualTo("공고 1");
            assertThat(recruit.semesterId()).isNull();
            assertThat(recruit.semesterName()).isNull();
            assertThat(recruit.submittedApplyCount()).isEqualTo(10L);
            assertThat(recruit.passedCount()).isEqualTo(2L);
            assertThat(recruit.failedCount()).isEqualTo(6L);
            assertThat(recruit.waitlistedCount()).isEqualTo(1L);
        });
    }

    @ParameterizedTest
    @EnumSource(value = ApplyStatus.class, names = {"JOINED", "REJECTED"})
    @DisplayName("지원서 관리에서 허용되지 않은 지원 상태로 공고를 조회하면 실패한다")
    void 지원서_관리에서_허용되지_않은_지원_상태로_공고를_조회하면_실패한다(ApplyStatus status) {
        ApplyManagementRecruitSearchCondition condition = searchCondition(
                status, RecruitType.SEMESTER, Set.of(FE), null, 20);

        assertThatThrownBy(() -> recruitService.searchApplyManagementRecruits(condition))
                .isInstanceOf(RecruitException.class);
    }

    @Test
    @DisplayName("모집 유형에서 지원하지 않는 직군으로 공고를 조회하면 실패한다")
    void 모집_유형에서_지원하지_않는_직군으로_공고를_조회하면_실패한다() {
        ApplyManagementRecruitSearchCondition condition = searchCondition(
                ApplyStatus.SUBMITTED, RecruitType.MAKERS, Set.of(JobFamily.APP), null, 20);

        assertThatThrownBy(() -> recruitService.searchApplyManagementRecruits(condition))
                .isInstanceOf(RecruitException.class);
    }

    @Test
    @DisplayName("조회 결과가 요청 크기보다 많으면 다음 공고 커서를 반환한다")
    void 조회_결과가_요청_크기보다_많으면_다음_공고_커서를_반환한다() {
        // given
        ApplyManagementRecruitSearchCondition condition = searchCondition(
                ApplyStatus.SUBMITTED, RecruitType.SEMESTER, Set.of(FE), null, 2);
        when(recruitRepository.searchApplyManagementRecruits(condition, 3)).thenReturn(List.of(
                recruitProjection(3L, RecruitType.SEMESTER, FE, 0L, 0L, 0L, 0L),
                recruitProjection(2L, RecruitType.SEMESTER, FE, 0L, 0L, 0L, 0L),
                recruitProjection(1L, RecruitType.SEMESTER, FE, 0L, 0L, 0L, 0L)));
        when(recruitRepository.countApplyManagementRecruits(condition)).thenReturn(3L);

        // when
        ApplyManagementRecruitsResponse response = recruitService.searchApplyManagementRecruits(condition);

        // then
        assertThat(response.recruits().content()).hasSize(2);
        assertThat(response.recruits().hasNext()).isTrue();
        assertThat(response.recruits().nextCursor()).isEqualTo(2L);
        assertThat(response.recruits().totalCount()).isEqualTo(3L);
    }

    @Test
    @DisplayName("조회 결과가 요청 크기 이하면 다음 공고 커서를 반환하지 않는다")
    void 조회_결과가_요청_크기_이하면_다음_공고_커서를_반환하지_않는다() {
        // given
        ApplyManagementRecruitSearchCondition condition = searchCondition(
                ApplyStatus.SUBMITTED, RecruitType.SEMESTER, Set.of(FE), null, 3);
        when(recruitRepository.searchApplyManagementRecruits(condition, 4)).thenReturn(List.of(
                recruitProjection(2L, RecruitType.SEMESTER, FE, 0L, 0L, 0L, 0L),
                recruitProjection(1L, RecruitType.SEMESTER, FE, 0L, 0L, 0L, 0L)));
        when(recruitRepository.countApplyManagementRecruits(condition)).thenReturn(2L);

        // when
        ApplyManagementRecruitsResponse response = recruitService.searchApplyManagementRecruits(condition);

        // then
        assertThat(response.recruits().content()).hasSize(2);
        assertThat(response.recruits().hasNext()).isFalse();
        assertThat(response.recruits().nextCursor()).isNull();
        assertThat(response.recruits().totalCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("공고가 없는 모집 유형도 0건으로 반환한다")
    void 공고가_없는_모집_유형도_0건으로_반환한다() {
        // given
        ApplyManagementRecruitSearchCondition condition = searchCondition(
                ApplyStatus.SUBMITTED, RecruitType.SEMESTER, null, null, 20);
        when(recruitRepository.countRecruitsByRecruitType()).thenReturn(List.of(
                new RecruitTypeCountResponse(RecruitType.SEMESTER, 4L)));

        // when
        ApplyManagementRecruitsResponse response = recruitService.searchApplyManagementRecruits(condition);

        // then
        assertThat(response.recruitTypeCounts())
                .containsExactly(
                        new RecruitTypeCountResponse(RecruitType.SEMESTER, 4L),
                        new RecruitTypeCountResponse(RecruitType.MAKERS, 0L),
                        new RecruitTypeCountResponse(RecruitType.SUPPORTERS, 0L));
    }

    @Test
    void 모집_등록_성공() {
        // given
        Semester semester = getSemester(false);
        when(semesterRepository.findRecruitingSemester()).thenReturn(Optional.ofNullable(semester));
        when(recruitRepository.existsByJobFamilyAndIsNotClosed(eq(semester.getId()), any())).thenReturn(false);

        List<RecruitRegisterRequest> requests = List.of(
                new RecruitRegisterRequest("백엔드 개발자 모집", BE,
                        LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1),
                        "백엔드 모집 요약", null, null, List.of()),
                new RecruitRegisterRequest("프론트엔드 개발자 모집", FE,
                        LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1),
                        null, null, null, List.of())
        );

        // when
        recruitService.registerRecruits(requests);

        // then
        ArgumentCaptor<List<JobFamily>> jobFamiliesCaptor = ArgumentCaptor.forClass(List.class);
        verify(recruitRepository).existsByJobFamilyAndIsNotClosed(eq(semester.getId()), jobFamiliesCaptor.capture());
        assertThat(jobFamiliesCaptor.getValue()).containsExactly(BE, FE);

        ArgumentCaptor<List<Recruit>> recruitsCaptor = ArgumentCaptor.forClass(List.class);
        verify(recruitRepository).saveAll(recruitsCaptor.capture());
        assertThat(recruitsCaptor.getValue()).hasSize(2);
        assertThat(recruitsCaptor.getValue().get(0).getTitle()).isEqualTo("백엔드 개발자 모집");
        assertThat(recruitsCaptor.getValue().get(0).getSummary()).isEqualTo("백엔드 모집 요약");
    }

    @Test
    void 활성_모집_공고_목록을_응답으로_변환한다() {
        // given
        Recruit recruit = Recruit.builder()
                .id(1L)
                .semester(getSemester(true))
                .title("운영 서포터즈 모집")
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .jobFamily(SUPPORTER)
                .recruitType(RecruitType.SUPPORTERS)
                .recruitTypeDetail(RecruitTypeDetail.REFILL)
                .summary("운영 서포터즈 모집 요약")
                .build();
        when(recruitRepository.findActiveRecruitments(any())).thenReturn(List.of(recruit));

        // when
        ActiveRecruitmentResponses result = recruitService.findActiveRecruitments();

        // then
        assertThat(result.recruitments()).hasSize(1);
        assertThat(result.recruitments().get(0).recruitId()).isEqualTo(1L);
        assertThat(result.recruitments().get(0).title()).isEqualTo("운영 서포터즈 모집");
        assertThat(result.recruitments().get(0).recruitType()).isEqualTo(RecruitType.SUPPORTERS);
        assertThat(result.recruitments().get(0).recruitTypeDetail()).isEqualTo(RecruitTypeDetail.REFILL);
        assertThat(result.recruitments().get(0).jobFamily()).isEqualTo(SUPPORTER);
        assertThat(result.recruitments().get(0).summary()).isEqualTo("운영 서포터즈 모집 요약");
    }

    @Test
    void 모집공고의_상세정보와_안내사항_및_FAQ를_조회한다() {
        // given
        Recruit recruit = Recruit.builder()
                .id(1L)
                .semester(getSemester(true))
                .title("백엔드 개발자 모집")
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .jobFamily(BE)
                .recruitInformation("<h2>모집 정보</h2>")
                .notice("<h2>안내사항</h2>")
                .faqs(List.of(RecruitFaq.create("지원 자격이 있나요?", "<p>누구나 지원할 수 있습니다.</p>")))
                .build();
        when(recruitRepository.findByIdWithSemester(1L)).thenReturn(Optional.of(recruit));

        // when
        RecruitResponse result = recruitService.getRecruit(1L);

        // then
        assertThat(result.title()).isEqualTo("백엔드 개발자 모집");
        assertThat(result.recruitInformation()).isEqualTo("<h2>모집 정보</h2>");
        assertThat(result.notice()).isEqualTo("<h2>안내사항</h2>");
        assertThat(result.faqs()).hasSize(1);
    }

    @Test
    void 기존_모집_유형은_활성_목록_응답에서_정규기수_모집으로_표준화한다() {
        // given
        Recruit recruit = Recruit.builder()
                .id(1L)
                .semester(getSemester(true))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .jobFamily(BE)
                .recruitType(RecruitType.BACKFILL)
                .recruitTypeDetail(RecruitTypeDetail.REFILL)
                .build();
        when(recruitRepository.findActiveRecruitments(any())).thenReturn(List.of(recruit));

        // when
        ActiveRecruitmentResponses result = recruitService.findActiveRecruitments();

        // then
        assertThat(result.recruitments().get(0).recruitType()).isEqualTo(RecruitType.SEMESTER);
        assertThat(result.recruitments().get(0).recruitTypeDetail()).isEqualTo(RecruitTypeDetail.REFILL);
    }

    private Semester getSemester() {
        Semester semester = Semester.builder()
                .id(1L)
                .name("1기")
                .isRecruiting(false)
                .build();
        return semester;
    }

    @Test
    void 모집_등록_시_이미_모집중인_직군이면_실패() {
        // given
        Semester semester = getSemester();
        RecruitRegisterRequest request =
                new RecruitRegisterRequest(BE, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        when(semesterRepository.findRecruitingSemester()).thenReturn(Optional.ofNullable(semester));
        when(recruitRepository.existsByJobFamilyAndIsNotClosed(any(), any())).thenReturn(true);

        // when, then
        assertThatThrownBy(() -> recruitService.registerRecruits(List.of(request)))
                .isInstanceOf(RecruitException.class);
    }

    @Test
    void 모집_정보_수정_성공() {
        // given
        Recruit recruit = Recruit.builder()
                .id(1L)
                .semester(getSemester(true))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .jobFamily(BE)
                .build();
        when(recruitRepository.findById(1L)).thenReturn(Optional.of(recruit));

        // 직군은 FE로, 마감일은 3일 후로 수정
        LocalDateTime newEndDate = LocalDateTime.now().plusDays(3);
        RecruitUpdateRequest request = new RecruitUpdateRequest(
                "수정된 프론트엔드 개발자 모집", FE,
                LocalDateTime.now().minusDays(1), newEndDate, "수정된 모집 요약", null, null, List.of());

        // when
        recruitService.updateRecruit(recruit.getId(), request);

        // then
        assertThat(recruit.getTitle()).isEqualTo("수정된 프론트엔드 개발자 모집");
        assertThat(recruit.getJobFamily()).isEqualTo(FE);
        assertThat(recruit.getEndDate()).isEqualTo(newEndDate);
        assertThat(recruit.getSummary()).isEqualTo("수정된 모집 요약");

        ArgumentCaptor<RecruitUpdatedEvent> eventCaptor = ArgumentCaptor.forClass(RecruitUpdatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().previousJobFamily()).isEqualTo(BE);
        assertThat(eventCaptor.getValue().currentJobFamily()).isEqualTo(FE);
    }

    @Test
    void 마감된_모집_정보는_수정_불가() {
        // given
        Recruit recruit = Recruit.builder()
                .semester(getSemester(false))
                .startDate(LocalDateTime.now().minusDays(3))
                .endDate(LocalDateTime.now().minusDays(1))
                .jobFamily(BE)
                .build();

        when(recruitRepository.findById(1L)).thenReturn(Optional.ofNullable(recruit));

        RecruitUpdateRequest request
                = new RecruitUpdateRequest(FE, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

        // when, then
        assertThatThrownBy(() -> recruitService.updateRecruit(1L, request))
                .isInstanceOf(RecruitException.class);
    }

    @Test
    void 수정하려는_recruit이_존재하지_않으면_실패() {
        // given
        RecruitUpdateRequest request =
                new RecruitUpdateRequest(BE, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

        // when, then
        assertThatThrownBy(() -> recruitService.updateRecruit(100L, request))
                .isInstanceOf(RecruitException.class);
    }

    @Test
    void 수정_시점이_모집기간이_아닌_경우_실패() {
        // given
        Recruit recruit = Recruit.builder()
                .id(1L)
                .semester(getSemester(false))
                .startDate(LocalDateTime.now().minusDays(3))
                .endDate(LocalDateTime.now().minusDays(1))
                .jobFamily(BE)
                .build();

        when(recruitRepository.findById(1L)).thenReturn(Optional.ofNullable(recruit));

        RecruitUpdateRequest request
                = new RecruitUpdateRequest(FE, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

        // when, then
        assertThatThrownBy(() -> recruitService.updateRecruit(1L, request))
                .isInstanceOf(RecruitException.class);
    }

    @Test
    void 모집_취소_성공() {
        // given
        Recruit recruit = Recruit.builder()
                .id(1L)
                .semester(getSemester(true))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .jobFamily(BE)
                .build();

        when(recruitRepository.findById(1L)).thenReturn(Optional.ofNullable(recruit));

        // when
        recruitService.cancelRecruit(1L);

        // then
        ArgumentCaptor<Recruit> recruitCaptor = ArgumentCaptor.forClass(Recruit.class);
        verify(recruitRepository).delete(recruitCaptor.capture());
        assertThat(recruitCaptor.getValue()).isSameAs(recruit);
    }

    private Semester getSemester(boolean isRecruiting) {
        return Semester.builder().id(1L).name("1기").isRecruiting(isRecruiting).build();
    }

    private ApplyManagementRecruitSearchCondition searchCondition(
            ApplyStatus status,
            RecruitType recruitType,
            Set<JobFamily> jobFamilies,
            Long cursor,
            Integer size
    ) {
        return new ApplyManagementRecruitSearchCondition(status, recruitType, jobFamilies, cursor, size);
    }

    private ApplyManagementRecruitProjection recruitProjection(
            Long recruitId,
            RecruitType recruitType,
            JobFamily jobFamily,
            long submittedApplyCount,
            long passedCount,
            long failedCount,
            long waitlistedCount
    ) {
        return new ApplyManagementRecruitProjection(
                recruitId,
                "공고 " + recruitId,
                1L,
                "5기",
                recruitType,
                jobFamily,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                submittedApplyCount,
                passedCount,
                failedCount,
                waitlistedCount);
    }

    @Test
    void 취소하려는_recruit이_존재하지_않으면_실패() {
        // when, then
        assertThatThrownBy(() -> recruitService.cancelRecruit(100L))
                .isInstanceOf(RecruitException.class);
    }
}
