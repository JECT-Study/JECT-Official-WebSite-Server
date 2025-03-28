package org.ject.support.domain.recruit.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.dto.QQuestionResponse;
import org.ject.support.domain.recruit.dto.QuestionResponse;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static org.ject.support.domain.recruit.domain.QQuestion.question;
import static org.ject.support.domain.recruit.domain.QRecruit.recruit;

@Repository
@RequiredArgsConstructor
public class QuestionQueryRepositoryImpl implements QuestionQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<QuestionResponse> findByJobFamilyOfActiveRecruit(final LocalDateTime now,
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
                .where(isWithinRecruitPeriod(now), recruit.jobFamily.eq(jobFamily))
                .orderBy(question.sequence.asc())
                .fetch();
    }

    private BooleanExpression isWithinRecruitPeriod(LocalDateTime now) {
        return recruit.startDate.before(now).and(recruit.endDate.after(now));
    }
}
