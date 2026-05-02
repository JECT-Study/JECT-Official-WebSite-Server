package org.ject.support.domain.recruit.repository;

import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Question;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.dto.QuestionResponse;
import org.ject.support.testconfig.QueryDslTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ject.support.domain.member.JobFamily.BE;
import static org.ject.support.domain.member.JobFamily.FE;
import static org.ject.support.domain.recruit.domain.Question.InputType.FILE;
import static org.ject.support.domain.recruit.domain.Question.InputType.TEXT;

@Import(QueryDslTestConfig.class)
@DataJpaTest
class QuestionQueryRepositoryTest {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private RecruitRepository recruitRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Test
    void 현재_모집중인_직군의_지원서_문항을_조회한다() {
        // given
        Semester savedSemester = semesterRepository.save(Semester.builder()
                .name("1기")
                .isRecruiting(true)
                .build());

        LocalDateTime now = LocalDateTime.now();
        Recruit feRecruit = createRecruit(savedSemester, now, FE);
        Recruit beRecruit = createRecruit(savedSemester, now, BE);
        recruitRepository.saveAll(List.of(feRecruit, beRecruit));

        Question feQuestion1 = createQuestion(1, TEXT, feRecruit);
        Question feQuestion2 = createQuestion(2, FILE, feRecruit);
        Question beQuestion1 = createQuestion(1, TEXT, beRecruit);
        Question beQuestion2 = createQuestion(2, TEXT, beRecruit);
        Question beQuestion3 = createQuestion(3, FILE, beRecruit);
        questionRepository.saveAll(List.of(feQuestion1, feQuestion2, beQuestion3, beQuestion1, beQuestion2));

        // when
        List<QuestionResponse> result = questionRepository.findByJobFamilyOfActiveRecruit(now, BE);

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting(QuestionResponse::sequence)
                .containsExactly(1, 2, 3);
    }

    @Test
    void 현재_모집중인_모집_공고의_지원서_문항을_조회한다() {
        // given
        Semester savedSemester = semesterRepository.save(Semester.builder()
                .name("1기")
                .isRecruiting(true)
                .build());

        LocalDateTime now = LocalDateTime.now();
        Recruit feRecruit = createRecruit(savedSemester, now, FE);
        Recruit beRecruit = createRecruit(savedSemester, now, BE);
        recruitRepository.saveAll(List.of(feRecruit, beRecruit));

        Question feQuestion1 = createQuestion(1, TEXT, feRecruit);
        Question feQuestion2 = createQuestion(2, FILE, feRecruit);
        Question beQuestion1 = createQuestion(1, TEXT, beRecruit);
        questionRepository.saveAll(List.of(beQuestion1, feQuestion2, feQuestion1));

        // when
        List<QuestionResponse> result = questionRepository.findByRecruitIdOfActiveRecruit(now, feRecruit.getId());

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(QuestionResponse::sequence)
                .containsExactly(1, 2);
    }

    @Test
    void selectOptions가_List_String과_JSON_문자열_간_정상_변환된다() {
        // given
        List<String> selectOptions = List.of("재직", "재학", "졸업", "휴학");

        Semester savedSemester = semesterRepository.save(Semester.builder()
                .name("1기")
                .isRecruiting(true)
                .build());

        LocalDateTime now = LocalDateTime.now();
        Recruit recruit = createRecruit(savedSemester, now, FE);
        recruitRepository.save(recruit);

        Question question = Question.builder()
                .sequence(1)
                .inputType(Question.InputType.SELECT)
                .isRequired(true)
                .title("title")
                .label("label")
                .selectOptions(selectOptions)
                .inputHint("inputHint")
                .recruit(recruit)
                .build();

        // when
        Question saved = questionRepository.save(question);
        Question found = questionRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(found.getSelectOptions()).containsExactly("재직", "재학", "졸업", "휴학");
    }

    private Recruit createRecruit(Semester semester, LocalDateTime now, JobFamily be) {
        return Recruit.builder()
                .semester(semester)
                .startDate(now.minusDays(1))
                .endDate(now.plusDays(1))
                .jobFamily(be)
                .build();
    }

    private Question createQuestion(int sequence, Question.InputType inputType, Recruit recruit) {
        return Question.builder()
                .sequence(sequence)
                .inputType(inputType)
                .isRequired(true)
                .title("title")
                .label("label")
                .inputHint("inputHint")
                .maxTextLength(500)
                .recruit(recruit)
                .build();
    }
}
