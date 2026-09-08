package org.ject.support.domain.recruit.domain;

import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.exception.RecruitErrorCode;
import org.ject.support.domain.recruit.exception.RecruitException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecruitTest {

    @Test
    @DisplayName("is recruiting period")
    void is_recruiting_period() {
        // given
        Recruit recruit = Recruit.builder()
                .semester(Semester.builder().id(1L).name("1기").isRecruiting(true).build())
                .jobFamily(JobFamily.BE)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .build();

        // when
        Boolean isRecruitingPeriod = recruit.isRecruitingPeriod();

        // then
        assertThat(isRecruitingPeriod).isTrue();
    }

    @Test
    void 모집_사유는_기본값으로_정규_모집을_사용한다() {
        // given
        Recruit recruit = Recruit.builder()
                .semester(Semester.builder().id(1L).name("1기").isRecruiting(true).build())
                .jobFamily(JobFamily.BE)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .build();

        // then
        assertThat(recruit.getRecruitTypeDetail()).isEqualTo(RecruitTypeDetail.REGULAR);
    }

    @Test
    void 허용된_모집_유형과_모집_사유_조합은_검증을_통과한다() {
        // given
        Recruit recruit = recruit(RecruitType.MAKERS, RecruitTypeDetail.NEW);

        // when & then
        assertThatCode(recruit::validateRecruitTypeDetail)
                .doesNotThrowAnyException();
    }

    @Test
    void 허용되지_않은_모집_유형과_모집_사유_조합이면_예외가_발생한다() {
        // given
        Recruit recruit = recruit(RecruitType.SUPPORTERS, RecruitTypeDetail.REGULAR);

        // when & then
        assertThatThrownBy(recruit::validateRecruitTypeDetail)
                .isInstanceOf(RecruitException.class)
                .hasFieldOrPropertyWithValue("errorCode", RecruitErrorCode.INVALID_RECRUIT_TYPE_DETAIL);
    }

    @Test
    void 모집_유형이_null이면_예외가_발생한다() {
        // given
        Recruit recruit = recruit(null, RecruitTypeDetail.REGULAR);

        // when & then
        assertThatThrownBy(recruit::validateRecruitTypeDetail)
                .isInstanceOf(RecruitException.class)
                .hasFieldOrPropertyWithValue("errorCode", RecruitErrorCode.INVALID_RECRUIT_TYPE_DETAIL);
    }

    @Test
    void 모집공고의_상세정보와_안내사항_및_FAQ를_변경한다() {
        // given
        Recruit recruit = recruit(RecruitType.SEMESTER, RecruitTypeDetail.REGULAR);
        List<RecruitFaq> faqs = List.of(RecruitFaq.create("지원 자격이 있나요?", "<p>누구나 지원할 수 있습니다.</p>"));

        // when
        recruit.update(JobFamily.FE, LocalDateTime.now(), LocalDateTime.now().plusDays(2),
                "<h2>모집 정보</h2>", "<h2>안내사항</h2>", faqs);

        // then
        assertThat(recruit.getRecruitInformation()).isEqualTo("<h2>모집 정보</h2>");
        assertThat(recruit.getNotice()).isEqualTo("<h2>안내사항</h2>");
        assertThat(recruit.getFaqs()).containsExactlyElementsOf(faqs);
    }

    @Test
    void 비어_있는_모집공고_내용은_기존값을_유지한다() {
        // given
        Recruit recruit = Recruit.builder()
                .semester(Semester.builder().id(1L).name("1기").isRecruiting(true).build())
                .jobFamily(JobFamily.BE)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .recruitInformation("<h2>모집 정보</h2>")
                .notice("<h2>안내사항</h2>")
                .faqs(List.of(RecruitFaq.create("지원 자격이 있나요?", "<p>누구나 지원할 수 있습니다.</p>")))
                .build();

        // when
        recruit.update(JobFamily.FE, LocalDateTime.now(), LocalDateTime.now().plusDays(2), "", " ", List.of());

        // then
        assertThat(recruit.getRecruitInformation()).isEqualTo("<h2>모집 정보</h2>");
        assertThat(recruit.getNotice()).isEqualTo("<h2>안내사항</h2>");
        assertThat(recruit.getFaqs()).hasSize(1);
    }

    @Test
    @DisplayName("is invalid question semesterId")
    void is_invalid_question_id() {
        // given
        Recruit recruit = Recruit.builder()
                .semester(Semester.builder().id(1L).name("1기").isRecruiting(true).build())
                .jobFamily(JobFamily.BE)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .build();

        Question question = Question.builder()
                .id(1L)
                .sequence(1)
                .inputType(Question.InputType.TEXT)
                .isRequired(true)
                .title("title")
                .recruit(recruit)
                .build();

        recruit.addQuestion(question);

        // when
        boolean isInvalidQuestionId = recruit.isInvalidQuestionId(2L);

        // then
        assertThat(isInvalidQuestionId).isTrue();
    }

    private Recruit recruit(RecruitType recruitType, RecruitTypeDetail recruitTypeDetail) {
        return Recruit.builder()
                .semester(Semester.builder().id(1L).name("1기").isRecruiting(true).build())
                .jobFamily(JobFamily.BE)
                .recruitType(recruitType)
                .recruitTypeDetail(recruitTypeDetail)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .build();
    }
}
