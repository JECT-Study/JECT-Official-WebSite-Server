package org.ject.support.domain.recruit.repository;

import static org.ject.support.domain.apply.domain.QApply.apply;
import static org.ject.support.domain.recruit.domain.QRecruit.recruit;
import static org.ject.support.domain.recruit.domain.QSemester.semester;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.ject.support.domain.recruit.dto.ApplyManagementRecruitProjection;
import org.ject.support.domain.recruit.dto.ApplyManagementRecruitSearchCondition;
import org.ject.support.domain.recruit.dto.RecruitTypeCountResponse;

@RequiredArgsConstructor
public class RecruitQueryRepositoryImpl implements RecruitQueryRepository {

    private static final List<RecruitType> APPLY_MANAGEMENT_RECRUIT_TYPES = List.of(
            RecruitType.SEMESTER,
            RecruitType.MAKERS,
            RecruitType.SUPPORTERS
    );

    private final JPAQueryFactory jpaQueryFactory;

    // 지원서 관리 화면의 모집공고와 제출 지원서 통계 조회
    @Override
    public List<ApplyManagementRecruitProjection> searchApplyManagementRecruits(
            ApplyManagementRecruitSearchCondition condition,
            int limit
    ) {
        return jpaQueryFactory.select(Projections.constructor(
                        ApplyManagementRecruitProjection.class,
                        recruit.id,
                        recruit.title,
                        semester.id,
                        semester.name,
                        recruit.recruitType,
                        recruit.jobFamily,
                        recruit.startDate,
                        recruit.endDate,
                        countSubmittedApplies(),
                        countSelectionResult(SelectionResult.PASSED),
                        countSelectionResult(SelectionResult.FAILED),
                        countSelectionResult(SelectionResult.WAITLISTED)
                ))
                .from(recruit)
                .join(recruit.semester, semester)
                .where(
                        cursorLt(condition.cursor()),
                        recruit.recruitType.eq(condition.recruitType()),
                        jobFamilyIn(condition.jobFamilies())
                )
                .orderBy(recruit.id.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public long countApplyManagementRecruits(ApplyManagementRecruitSearchCondition condition) {
        Long count = jpaQueryFactory.select(recruit.id.count())
                .from(recruit)
                .where(
                        recruit.recruitType.eq(condition.recruitType()),
                        jobFamilyIn(condition.jobFamilies())
                )
                .fetchOne();
        return count == null ? 0L : count;
    }

    @Override
    public List<RecruitTypeCountResponse> countRecruitsByRecruitType() {
        List<Tuple> counts = jpaQueryFactory
                .select(recruit.recruitType, recruit.id.count())
                .from(recruit)
                .where(recruit.recruitType.in(APPLY_MANAGEMENT_RECRUIT_TYPES))
                .groupBy(recruit.recruitType)
                .fetch();

        return counts.stream()
                .map(tuple -> new RecruitTypeCountResponse(
                        tuple.get(recruit.recruitType),
                        getCount(tuple)))
                .toList();
    }

    private com.querydsl.jpa.JPQLQuery<Long> countSubmittedApplies() {
        return JPAExpressions.select(apply.id.count())
                .from(apply)
                .where(
                        apply.recruit.id.eq(recruit.id),
                        apply.status.eq(ApplyStatus.SUBMITTED),
                        apply.isDeleted.isFalse()
                );
    }

    private com.querydsl.jpa.JPQLQuery<Long> countSelectionResult(SelectionResult selectionResult) {
        return JPAExpressions.select(apply.id.count())
                .from(apply)
                .where(
                        apply.recruit.id.eq(recruit.id),
                        apply.status.eq(ApplyStatus.SUBMITTED),
                        apply.selectionResult.eq(selectionResult),
                        apply.isDeleted.isFalse()
                );
    }

    private BooleanExpression cursorLt(Long cursor) {
        return cursor == null ? null : recruit.id.lt(cursor);
    }

    private BooleanExpression jobFamilyIn(Set<JobFamily> jobFamilies) {
        return jobFamilies == null || jobFamilies.isEmpty() ? null : recruit.jobFamily.in(jobFamilies);
    }

    private long getCount(Tuple tuple) {
        Long count = tuple.get(1, Long.class);
        return count == null ? 0L : count;
    }
}
