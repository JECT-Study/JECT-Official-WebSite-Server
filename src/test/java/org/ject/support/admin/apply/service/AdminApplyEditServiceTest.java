package org.ject.support.admin.apply.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ject.support.admin.apply.dto.EditApplyRequest;
import org.ject.support.admin.apply.repository.AdminApplyRepository;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.apply.domain.ApplicationForm;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.domain.Portfolio;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.domain.apply.exception.ApplyErrorCode;
import org.ject.support.domain.apply.exception.ApplyException;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.recruit.domain.Question;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.exception.QuestionErrorCode;
import org.ject.support.domain.recruit.exception.QuestionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

class AdminApplyEditServiceTest extends UnitTestSupport {

    @InjectMocks
    private AdminApplyService service;
    @Mock
    private AdminApplyRepository repository;
    @Spy
    private Map2JsonSerializer serializer = new Map2JsonSerializer(new ObjectMapper());

    private Apply apply;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        Applicant applicant = Applicant.builder().name("김젝트").phoneNumber("01012345678")
                .email("before@test.com").jobFamily(JobFamily.BE).careerDetails(CareerDetails.STUDENT)
                .region(Region.SEOUL).experiencePeriod(ExperiencePeriod.NONE)
                .interestedDomains(new ArrayList<>(List.of("GAME"))).build();
        Recruit recruit = Recruit.builder().id(1L).jobFamily(JobFamily.BE)
                .questions(List.of(Question.builder().id(1L).build(), Question.builder().id(2L).build())).build();
        apply = Apply.builder().id(1L).applicant(applicant).recruit(recruit).status(ApplyStatus.SUBMITTED)
                .submittedAt(LocalDateTime.of(2026, 10, 24, 10, 0)).note("기존 비고")
                .applicationForm(ApplicationForm.builder().content("{\"1\":\"기존 답변\",\"2\":\"기존 답변2\"}")
                        .portfolios(new ArrayList<>(List.of(
                                Portfolio.builder().id(10L).fileUrl("old.pdf").build(),
                                Portfolio.builder().id(20L).fileUrl("keep.pdf").build()))).build()).build();
    }

    @ParameterizedTest
    @EnumSource(value = ApplyStatus.class, names = {"TEMP_SAVED", "SUBMITTED"})
    void 지원서의_수정_가능한_정보를_요청값으로_교체한다(ApplyStatus status) throws Exception {
        // given
        if (status == ApplyStatus.TEMP_SAVED) {
            apply.saveTemporarily();
        }
        LocalDateTime submittedAt = apply.getSubmittedAt();
        Recruit recruit = apply.getRecruit();
        EditApplyRequest request = request(Map.of("1", "새 답변"),
                status == ApplyStatus.SUBMITTED ? SelectionResult.PASSED : null, "새 비고", Set.of(10L));
        given(repository.findAppliesWithApplicant(List.of(1L))).willReturn(List.of(apply));

        // when
        service.editApply(1L, request);

        // then
        Applicant applicant = apply.getApplicant();
        assertThat(applicant.getName()).isEqualTo(request.name());
        assertThat(applicant.getPhoneNumber()).isEqualTo(request.phoneNumber());
        assertThat(applicant.getEmail()).isEqualTo(request.email());
        assertThat(applicant.getCareerDetails()).isEqualTo(request.careerDetails());
        assertThat(applicant.getRegion()).isEqualTo(request.region());
        assertThat(applicant.getExperiencePeriod()).isEqualTo(request.experiencePeriod());
        assertThat(applicant.getInterestedDomains()).isEqualTo(request.interestedDomains());
        assertThat(applicant.getJobFamily()).isEqualTo(JobFamily.BE);
        assertThat(apply.getRecruit()).isSameAs(recruit);
        assertThat(apply.getStatus()).isEqualTo(status);
        assertThat(apply.getSubmittedAt()).isEqualTo(submittedAt);
        assertThat(apply.getSelectionResult()).isEqualTo(status == ApplyStatus.SUBMITTED
                ? SelectionResult.PASSED : SelectionResult.UNDECIDED);
        assertThat(apply.getNote()).isEqualTo("새 비고");
        assertThat(objectMapper.readValue(apply.getApplicationForm().getContent(), Map.class))
                .isEqualTo(request.answers());
        assertThat(apply.getApplicationForm().getPortfolios()).extracting(Portfolio::getId).containsExactly(20L);
    }

    @Test
    void 선택_정보를_비우면_기존_값을_제거한다() {
        // given
        EditApplyRequest request = new EditApplyRequest("이수정", "01098765432", "after@test.com",
                CareerDetails.EMPLOYEE, null, null, List.of("GAME"), SelectionResult.PASSED,
                null, Map.of(), null);
        given(repository.findAppliesWithApplicant(List.of(1L))).willReturn(List.of(apply));

        // when
        service.editApply(1L, request);

        // then
        assertThat(apply.getApplicant().getRegion()).isNull();
        assertThat(apply.getApplicant().getExperiencePeriod()).isNull();
        assertThat(apply.getNote()).isEmpty();
        assertThat(apply.getApplicationForm().getPortfolios()).extracting(Portfolio::getId).containsExactly(10L, 20L);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void 답변을_교체하면_전달하지_않은_기존_답변은_제거한다(boolean empty) throws Exception {
        // given
        Map<String, String> answers = empty ? Map.of() : Map.of("1", "새 답변");
        given(repository.findAppliesWithApplicant(List.of(1L))).willReturn(List.of(apply));

        // when
        service.editApply(1L, request(answers, SelectionResult.PASSED, "", Set.of()));

        // then
        assertThat(objectMapper.readValue(apply.getApplicationForm().getContent(), Map.class)).isEqualTo(answers);
        assertThat(apply.getApplicationForm().getPortfolios()).hasSize(2);
    }

    @Test
    void 다른_공고의_질문이_포함되면_수정을_거부한다() {
        // given
        given(repository.findAppliesWithApplicant(List.of(1L))).willReturn(List.of(apply));

        // when, then
        assertThatThrownBy(() -> service.editApply(1L,
                request(Map.of("99", "다른 공고 질문"), SelectionResult.PASSED, "", null)))
                .isInstanceOf(QuestionException.class)
                .hasFieldOrPropertyWithValue("errorCode", QuestionErrorCode.NOT_FOUND_QUESTION);
    }

    @Test
    void 존재하지_않는_지원서는_수정할_수_없다() {
        // given
        given(repository.findAppliesWithApplicant(List.of(1L))).willReturn(List.of());

        // when, then
        assertThatThrownBy(() -> service.editApply(1L, request(Map.of(), SelectionResult.PASSED, "", null)))
                .isInstanceOf(ApplyException.class)
                .hasFieldOrPropertyWithValue("errorCode", ApplyErrorCode.NOT_FOUND_APPLY);
    }

    private EditApplyRequest request(Map<String, String> answers, SelectionResult result, String note, Set<Long> ids) {
        return new EditApplyRequest("이수정", "01098765432", "after@test.com", CareerDetails.EMPLOYEE,
                Region.BUSAN, ExperiencePeriod.ONE_TO_TWO, List.of("GAME", "EDUCATION"), result, note, answers, ids);
    }
}
