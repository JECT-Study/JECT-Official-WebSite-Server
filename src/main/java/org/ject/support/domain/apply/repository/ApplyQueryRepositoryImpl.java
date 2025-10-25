package org.ject.support.domain.apply.repository;

import static org.ject.support.domain.apply.domain.Apply.Status.SUBMITTED;
import static org.ject.support.domain.apply.domain.QApplicationForm.applicationForm;
import static org.ject.support.domain.apply.domain.QApply.apply;
import static org.ject.support.domain.apply.domain.QPortfolio.portfolio;
import static org.ject.support.domain.member.entity.QMember.member;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.data.PageResponse;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.member.JobFamily;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ApplyQueryRepositoryImpl implements ApplyQueryRepository {

    private final JPQLQueryFactory queryFactory;

    @Override
    public Page<Apply> findSubmittedApplies(final JobFamily jobFamily,
                                            final Pageable pageable) {
        List<Apply> content = queryFactory
                .selectFrom(apply)
                .join(apply.member, member).fetchJoin()
                .join(apply.applicationForm, applicationForm).fetchJoin()
                .leftJoin(apply.applicationForm.portfolios, portfolio).fetchJoin()
                .where(
                        apply.member.isDeleted.eq(false),
                        eqJobFamily(jobFamily),
                        apply.status.eq(SUBMITTED)
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
                        apply.status.eq(SUBMITTED)
                ).fetchOne();


        return PageResponse.from(content, pageable, total);
    }

    private BooleanExpression eqJobFamily(final JobFamily jobFamily) {
        return Optional.ofNullable(jobFamily)
                .map(apply.member.jobFamily::eq)
                .orElse(null);
    }
}
