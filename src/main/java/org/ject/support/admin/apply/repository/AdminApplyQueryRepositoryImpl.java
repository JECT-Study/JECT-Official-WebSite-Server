package org.ject.support.admin.apply.repository;

import static org.ject.support.domain.apply.domain.QApplicationForm.applicationForm;
import static org.ject.support.domain.apply.domain.QApply.apply;
import static org.ject.support.domain.apply.domain.QPortfolio.portfolio;
import static org.ject.support.domain.member.entity.QMember.member;
import static org.ject.support.domain.recruit.domain.QRecruit.recruit;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.apply.dto.AdminApplySearchCondition;
import org.ject.support.common.data.PageResponse;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.apply.domain.ApplyStatus;
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
    public Page<Apply> findApplies(final AdminApplySearchCondition condition,
                                   final Pageable pageable) {

        List<Apply> content = queryFactory
                .selectFrom(apply)
                .distinct()
                .join(apply.member, member).fetchJoin()
                .join(apply.applicationForm, applicationForm).fetchJoin()
                .join(apply.recruit, recruit).fetchJoin()
                .leftJoin(apply.applicationForm.portfolios, portfolio).fetchJoin()
                .where(
                        apply.member.isDeleted.eq(false),
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
                .join(apply.member, member)
                .join(apply.recruit, recruit)
                .where(
                        apply.member.isDeleted.eq(false),
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
    public Optional<Apply> findApplyByIdByStatus(final Long applyId, final ApplyStatus status) {
        Apply result = queryFactory
                .selectFrom(apply)
                .distinct()
                .join(apply.member, member).fetchJoin()
                .join(apply.applicationForm, applicationForm).fetchJoin()
                .join(apply.recruit, recruit).fetchJoin()
                .leftJoin(apply.applicationForm.portfolios, portfolio).fetchJoin()
                .where(
                        apply.id.eq(applyId),
                        apply.member.isDeleted.eq(false),
                        eqApplyStatus(status)
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

    private BooleanExpression eqJobFamily(final JobFamily jobFamily) {
        return Optional.ofNullable(jobFamily)
                .map(apply.member.jobFamily::eq)
                .orElse(null);
    }

    private BooleanExpression eqApplyStatus(final ApplyStatus status) {
        return Optional.ofNullable(status)
                .map(apply.status::eq)
                .orElse(null);
    }

    private BooleanExpression eqSemesterId(final Long semesterId) {
        return Optional.ofNullable(semesterId)
                .map(apply.recruit.semester.id::eq)
                .orElse(null);
    }

    private BooleanExpression eqRecruitType(final RecruitType recruitType) {
        return Optional.ofNullable(recruitType)
                .map(apply.recruit.recruitType::eq)
                .orElse(null);
    }

    private BooleanExpression eqRecruitTypeDetail(final RecruitTypeDetail recruitTypeDetail) {
        return Optional.ofNullable(recruitTypeDetail)
                .map(apply.recruit.recruitTypeDetail::eq)
                .orElse(null);
    }

    private BooleanExpression eqRecruitId(final Long recruitId) {
        return Optional.ofNullable(recruitId)
                .map(apply.recruit.id::eq)
                .orElse(null);
    }
}
