package org.ject.support.admin.mail.repository;

import static org.ject.support.admin.mail.domain.QMailDispatchOutbox.mailDispatchOutbox;
import static org.ject.support.admin.mail.domain.QMailDispatchTarget.mailDispatchTarget;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.mail.domain.MailDispatchTargetStatus;
import org.ject.support.admin.mail.dto.MailDispatchTargetResponse;
import org.ject.support.common.data.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MailDispatchTargetQueryRepositoryImpl implements MailDispatchTargetQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<MailDispatchTargetResponse> findTargets(Long dispatchJobId,
                                                      MailDispatchTargetStatus status,
                                                      Pageable pageable) {
        // Outbox 없는 과거 이력을 유지하고 본문·claim token 없이 조회 필드만 투영
        List<MailDispatchTargetResponse> content = queryFactory
                .select(Projections.constructor(MailDispatchTargetResponse.class,
                        mailDispatchTarget.id, mailDispatchTarget.applyId, mailDispatchTarget.email,
                        mailDispatchTarget.status, mailDispatchTarget.sentAt, mailDispatchTarget.failureReason,
                        mailDispatchOutbox.attemptCount, mailDispatchOutbox.nextAttemptAt,
                        mailDispatchOutbox.failureReason))
                .from(mailDispatchTarget)
                .leftJoin(mailDispatchOutbox).on(
                        mailDispatchOutbox.dispatchJob.id.eq(mailDispatchTarget.dispatchJob.id),
                        mailDispatchOutbox.applyId.eq(mailDispatchTarget.applyId))
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
