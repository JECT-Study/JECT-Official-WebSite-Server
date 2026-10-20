package org.ject.support.admin.apply.repository;

import static org.ject.support.domain.apply.domain.QApplicationForm.applicationForm;
import static org.ject.support.domain.apply.domain.QApply.apply;
import static org.ject.support.domain.apply.domain.QPortfolio.portfolio;
import static org.ject.support.domain.applicant.entity.QApplicant.applicant;
import static org.ject.support.domain.recruit.domain.QRecruit.recruit;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.apply.dto.SubmittedApplyProjection;
import org.ject.support.admin.apply.dto.SubmittedApplySearchCondition;
import org.ject.support.admin.apply.dto.TempSavedApplyProjection;
import org.ject.support.admin.apply.dto.TempSavedApplySearchCondition;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.domain.member.CareerDetails;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdminApplyQueryRepositoryImpl implements AdminApplyQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<SubmittedApplyProjection> searchSubmittedApplies(
            SubmittedApplySearchCondition condition,
            int limit
    ) {
        return queryFactory
                .select(Projections.constructor(
                        SubmittedApplyProjection.class,
                        apply.id,
                        applicant.name,
                        applicant.phoneNumber,
                        applicant.careerDetails,
                        apply.selectionResult,
                        apply.waitlistNumber,
                        apply.note
                ))
                .from(apply)
                .join(apply.applicant, applicant)
                .where(
                        apply.isDeleted.isFalse(),
                        applicant.isDeleted.isFalse(),
                        apply.recruit.id.eq(condition.recruitId()),
                        apply.status.eq(ApplyStatus.SUBMITTED),
                        careerDetailsIn(condition.careerDetails()),
                        selectionResultIn(condition.selectionResults()),
                        cursorLt(condition.cursor())
                )
                .orderBy(apply.id.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public long countSubmittedApplies(SubmittedApplySearchCondition condition) {
        Long total = queryFactory
                .select(apply.count())
                .from(apply)
                .join(apply.applicant, applicant)
                .where(
                        apply.isDeleted.isFalse(),
                        applicant.isDeleted.isFalse(),
                        apply.recruit.id.eq(condition.recruitId()),
                        apply.status.eq(ApplyStatus.SUBMITTED),
                        careerDetailsIn(condition.careerDetails()),
                        selectionResultIn(condition.selectionResults())
                ).fetchOne();
        return total == null ? 0L : total;
    }

    @Override
    public List<TempSavedApplyProjection> searchTempSavedApplies(
            TempSavedApplySearchCondition condition,
            int limit
    ) {
        return queryFactory
                .select(Projections.constructor(
                        TempSavedApplyProjection.class,
                        apply.id,
                        applicant.name,
                        applicant.phoneNumber,
                        applicant.careerDetails
                ))
                .from(apply)
                .join(apply.applicant, applicant)
                .where(
                        apply.isDeleted.isFalse(),
                        applicant.isDeleted.isFalse(),
                        apply.recruit.id.eq(condition.recruitId()),
                        apply.status.eq(ApplyStatus.TEMP_SAVED),
                        careerDetailsIn(condition.careerDetails()),
                        cursorLt(condition.cursor())
                )
                .orderBy(apply.id.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public long countTempSavedApplies(TempSavedApplySearchCondition condition) {
        Long total = queryFactory
                .select(apply.count())
                .from(apply)
                .join(apply.applicant, applicant)
                .where(
                        apply.isDeleted.isFalse(),
                        applicant.isDeleted.isFalse(),
                        apply.recruit.id.eq(condition.recruitId()),
                        apply.status.eq(ApplyStatus.TEMP_SAVED),
                        careerDetailsIn(condition.careerDetails())
                )
                .fetchOne();
        return total == null ? 0L : total;
    }

    @Override
    public Optional<Apply> findApplyByIdByStatus(final Long applyId, final ApplyStatus status) {
        Apply result = queryFactory
                .selectFrom(apply)
                .distinct()
                .join(apply.applicant, applicant).fetchJoin()
                .leftJoin(apply.applicationForm, applicationForm).fetchJoin()
                .join(apply.recruit, recruit).fetchJoin()
                .leftJoin(apply.applicationForm.portfolios, portfolio).fetchJoin()
                .where(
                        apply.id.eq(applyId),
                        apply.applicant.isDeleted.eq(false),
                        eqApplyStatus(status)
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

    private BooleanExpression eqApplyStatus(final ApplyStatus status) {
        return Optional.ofNullable(status)
                .map(apply.status::eq)
                .orElse(null);
    }

    private BooleanExpression careerDetailsIn(Set<CareerDetails> careerDetails) {
        return careerDetails == null || careerDetails.isEmpty()
                ? null
                : applicant.careerDetails.in(careerDetails);
    }

    private BooleanExpression selectionResultIn(Set<SelectionResult> selectionResults) {
        return selectionResults == null || selectionResults.isEmpty()
                ? null
                : apply.selectionResult.in(selectionResults);
    }

    private BooleanExpression cursorLt(Long cursor) {
        return cursor == null ? null : apply.id.lt(cursor);
    }
}
