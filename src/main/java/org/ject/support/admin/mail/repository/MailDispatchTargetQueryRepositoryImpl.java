package org.ject.support.admin.mail.repository;

import static org.ject.support.admin.mail.domain.QMailDispatchTarget.mailDispatchTarget;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.mail.domain.MailDispatchTarget;
import org.ject.support.admin.mail.domain.MailDispatchTargetStatus;
import org.ject.support.common.data.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MailDispatchTargetQueryRepositoryImpl implements MailDispatchTargetQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<MailDispatchTarget> findTargets(Long dispatchJobId,
                                                MailDispatchTargetStatus status,
                                                Pageable pageable) {
        List<MailDispatchTarget> content = queryFactory
                .selectFrom(mailDispatchTarget)
                .where(
                        mailDispatchTarget.dispatchJob.id.eq(dispatchJobId),
                        eqStatus(status)
                )
                .orderBy(mailDispatchTarget.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(mailDispatchTarget.count())
                .from(mailDispatchTarget)
                .where(
                        mailDispatchTarget.dispatchJob.id.eq(dispatchJobId),
                        eqStatus(status)
                )
                .fetchOne();

        return PageResponse.from(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression eqStatus(MailDispatchTargetStatus status) {
        return Optional.ofNullable(status)
                .map(mailDispatchTarget.status::eq)
                .orElse(null);
    }
}
