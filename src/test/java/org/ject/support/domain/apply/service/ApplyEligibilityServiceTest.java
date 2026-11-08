package org.ject.support.domain.apply.service;

import java.util.List;
import java.util.stream.Stream;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.repository.ApplyRepository;
import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.entity.MemberActivity;
import org.ject.support.domain.member.service.MemberQueryService;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
import org.ject.support.domain.recruit.domain.Semester;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.ject.support.domain.member.fixture.SemesterActivityFixture.semesterActivity;
import static org.ject.support.domain.member.fixture.MakersActivityFixture.makersActivity;
import static org.mockito.BDDMockito.given;

class ApplyEligibilityServiceTest extends UnitTestSupport {

    private static final String EMAIL = "applicant@test.com";

    @InjectMocks
    private ApplyEligibilityService service;
    @Mock
    private ApplyRepository applyRepository;
    @Mock
    private MemberQueryService memberQueryService;

    @ParameterizedTest
    @MethodSource("sameActivities")
    void 지원_대상_기수나_유형에서_활동_중이면_신규_지원을_거부한다(Recruit recruit, MemberActivity activity) {
        // given
        given(memberQueryService.searchActiveActivities(EMAIL)).willReturn(List.of(activity));

        // when, then
        assertThatThrownBy(() -> service.validateApplication(EMAIL, recruit))
                .hasFieldOrPropertyWithValue("errorCode", ApplyErrorCode.ACTIVE_MEMBER_APPLICATION_NOT_ALLOWED);
    }

    @ParameterizedTest
    @MethodSource("differentActivities")
    void 다른_기수나_유형에서_활동_중이면_신규_지원을_허용한다(Recruit recruit, MemberActivity activity) {
        // given
        given(memberQueryService.searchActiveActivities(EMAIL)).willReturn(List.of(activity));

        // when, then
        assertThatCode(() -> service.validateApplication(EMAIL, recruit)).doesNotThrowAnyException();
    }

    @Test
    void 다른_OPEN_공고에_임시저장하거나_제출했으면_신규_지원을_거부한다() {
        // given
        Recruit recruit = recruit(RecruitType.SEMESTER);
        given(applyRepository.existsActiveApplicationInOtherRecruit(EMAIL, recruit.getId())).willReturn(true);

        // when, then
        assertThatThrownBy(() -> service.validateApplication(EMAIL, recruit))
                .hasFieldOrPropertyWithValue("errorCode", ApplyErrorCode.APPLY_EXISTS_IN_OTHER_RECRUIT);
    }

    @Test
    void 기존_지원서의_중복_검사는_현재_구성원_활동을_제한하지_않는다() {
        // when, then
        assertThatCode(() -> service.validateDuplicateApplication(EMAIL, 1L)).doesNotThrowAnyException();
    }

    private static Stream<Arguments> sameActivities() {
        return Stream.of(RecruitType.values()).map(type -> Arguments.of(recruit(type), activity(type)));
    }

    private static Stream<Arguments> differentActivities() {
        return Stream.concat(Stream.of(RecruitType.values()).flatMap(target -> Stream.of(RecruitType.values())
                .filter(existing -> target != existing).map(existing -> Arguments.of(recruit(target), activity(existing)))),
                Stream.of(Arguments.of(recruit(RecruitType.SEMESTER), semesterActivity().semesterId(2L).build())));
    }

    private static Recruit recruit(RecruitType type) {
        return Recruit.builder().id(1L).recruitType(type)
                .semester(type == RecruitType.SEMESTER ? Semester.builder().id(1L).build() : null).build();
    }

    private static MemberActivity activity(RecruitType type) {
        return switch (type) {
            case SEMESTER -> semesterActivity().semesterId(1L).build();
            case MAKERS -> makersActivity().build();
            case SUPPORTERS -> MemberActivity.createSupportersActivity(1L, JobFamily.OPS, RecruitTypeDetail.NEW,
                    ActivityStatus.ACTIVE, null, null, null, null);
        };
    }
}
