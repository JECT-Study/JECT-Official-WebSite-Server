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
import org.ject.support.admin.apply.dto.AdminApplySearchCondition;
import org.ject.support.admin.apply.dto.SubmittedApplyProjection;
import org.ject.support.admin.apply.dto.SubmittedApplySearchCondition;
import org.ject.support.admin.apply.dto.TempSavedApplyProjection;
import org.ject.support.admin.apply.dto.TempSavedApplySearchCondition;
import org.ject.support.common.data.PageResponse;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdminApplyQueryRepositoryImpl implements AdminApplyQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Apply> findApplies(AdminApplySearchCondition condition, Pageable pageable) {
        List<Apply> content = queryFactory
                .selectFrom(apply)
                .distinct()
                .join(apply.applicant, applicant).fetchJoin()
                .join(apply.applicationForm, applicationForm).fetchJoin()
                .join(apply.recruit, recruit).fetchJoin()
                .leftJoin(apply.applicationForm.portfolios, portfolio).fetchJoin()
                .where(
                        apply.applicant.isDeleted.eq(false),
                        eqJobFamily(condition.jobFamily()),
                        eqApplyStatus(condition.applyStatus()),
                        eqSemesterId(condition.semesterId()),
                        eqRecruitType(condition.recruitType()),
                        eqRecruitTypeDetail(condition.recruitTypeDetail()),
                        eqRecruitId(condition.recruitId())
                )
                .orderBy(apply.createdAt.desc(), apply.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(apply.count())
                .from(apply)
                .join(apply.applicant, applicant)
                .join(apply.recruit, recruit)
                .where(
                        apply.applicant.isDeleted.eq(false),
                        eqJobFamily(condition.jobFamily()),
                        eqApplyStatus(condition.applyStatus()),
                        eqSemesterId(condition.semesterId()),
                        eqRecruitType(condition.recruitType()),
                        eqRecruitTypeDetail(condition.recruitTypeDetail()),
                        eqRecruitId(condition.recruitId())
                ).fetchOne();

        return PageResponse.from(content, pageable, total != null ? total : 0L);
    }

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

    private BooleanExpression eqJobFamily(JobFamily jobFamily) {
        return Optional.ofNullable(jobFamily)
                .map(apply.applicant.jobFamily::eq)
                .orElse(null);
    }

    private BooleanExpression eqSemesterId(Long semesterId) {
        return Optional.ofNullable(semesterId)
                .map(apply.recruit.semester.id::eq)
                .orElse(null);
    }

    private BooleanExpression eqRecruitType(RecruitType recruitType) {
        return Optional.ofNullable(recruitType)
                .map(apply.recruit.recruitType::eq)
                .orElse(null);
    }

    private BooleanExpression eqRecruitTypeDetail(RecruitTypeDetail recruitTypeDetail) {
        return Optional.ofNullable(recruitTypeDetail)
                .map(apply.recruit.recruitTypeDetail::eq)
                .orElse(null);
    }

    private BooleanExpression eqRecruitId(Long recruitId) {
        return Optional.ofNullable(recruitId)
                .map(apply.recruit.id::eq)
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
