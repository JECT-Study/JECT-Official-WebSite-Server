package org.ject.support.domain.recruit.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.dto.QQuestionResponse;
import org.ject.support.domain.recruit.dto.QuestionResponse;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import static org.ject.support.domain.recruit.domain.QQuestion.question;
import static org.ject.support.domain.recruit.domain.QRecruit.recruit;

@Repository
@RequiredArgsConstructor
public class QuestionQueryRepositoryImpl implements QuestionQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<QuestionResponse> findByJobFamilyOfActiveRecruit(final LocalDate currentDate,
                                                                 final JobFamily jobFamily) {
        return queryFactory.select(new QQuestionResponse(
                        question.id,
                        question.sequence,
                        question.inputType,
                        question.isRequired,
                        question.title,
                        question.body,
                        question.inputHint,
                        question.maxLength))
                .from(question)
                .leftJoin(question.recruit, recruit)
                .where(isWithinRecruitPeriod(currentDate), recruit.jobFamily.eq(jobFamily))
                .orderBy(question.sequence.asc())
                .fetch();
    }

    private BooleanExpression isWithinRecruitPeriod(LocalDate currentDate) {
        return recruit.startDate.before(currentDate).and(recruit.endDate.after(currentDate));
    }
}
