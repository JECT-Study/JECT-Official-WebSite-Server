package org.ject.support.domain.apply.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.data.PageResponse;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.member.JobFamily;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static org.ject.support.domain.apply.domain.QApplicationForm.applicationForm;
import static org.ject.support.domain.apply.domain.QApply.apply;
import static org.ject.support.domain.apply.domain.QPortfolio.portfolio;
import static org.ject.support.domain.member.entity.QMember.member;

@Repository
@RequiredArgsConstructor
public class ApplyQueryRepositoryImpl implements ApplyQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Apply> findApplies(final JobFamily jobFamily,
                                   final Apply.Status status,
                                   final Pageable pageable) {
        List<Apply> content = queryFactory
                .selectFrom(apply)
                .distinct()
                .join(apply.member, member).fetchJoin()
                .join(apply.applicationForm, applicationForm).fetchJoin()
                .leftJoin(apply.applicationForm.portfolios, portfolio).fetchJoin()
                .where(
                        apply.member.isDeleted.eq(false),
                        eqJobFamily(jobFamily),
                        eqApplyStatus(status)
                )
                .orderBy(apply.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(apply.count())
                .from(apply)
                .join(apply.member, member)
                .where(
                        apply.member.isDeleted.eq(false),
                        eqJobFamily(jobFamily),
                        eqApplyStatus(status)
                ).fetchOne();

        return PageResponse.from(content, pageable, total);
    }

    private BooleanExpression eqJobFamily(final JobFamily jobFamily) {
        return Optional.ofNullable(jobFamily)
                .map(apply.member.jobFamily::eq)
                .orElse(null);
    }
    private BooleanExpression eqApplyStatus(final Apply.Status status) {
        return Optional.ofNullable(status)
                .map(apply.status::eq)
                .orElse(null);
    }
}
