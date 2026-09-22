package org.ject.support.admin.mail.repository;

import static org.ject.support.admin.mail.domain.QMailDispatchJob.mailDispatchJob;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.domain.MailDispatchJobStatus;
import org.ject.support.admin.mail.dto.MailDispatchJobSearchCondition;
import org.ject.support.common.data.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MailDispatchJobQueryRepositoryImpl implements MailDispatchJobQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<MailDispatchJob> findJobs(Long requestedByAdminId,
                                          MailDispatchJobSearchCondition condition,
                                          Pageable pageable) {
        List<MailDispatchJob> content = queryFactory
                .selectFrom(mailDispatchJob)
                .where(
                        mailDispatchJob.requestedByAdminId.eq(requestedByAdminId),
                        eqRecruitId(condition.recruitId()),
                        eqStatus(condition.status())
                )
                .orderBy(mailDispatchJob.requestedAt.desc(), mailDispatchJob.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(mailDispatchJob.count())
                .from(mailDispatchJob)
                .where(
                        mailDispatchJob.requestedByAdminId.eq(requestedByAdminId),
                        eqRecruitId(condition.recruitId()),
                        eqStatus(condition.status())
                )
                .fetchOne();

        return PageResponse.from(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression eqRecruitId(Long recruitId) {
        return Optional.ofNullable(recruitId)
                .map(mailDispatchJob.recruitId::eq)
                .orElse(null);
    }

    private BooleanExpression eqStatus(MailDispatchJobStatus status) {
        return Optional.ofNullable(status)
                .map(mailDispatchJob.status::eq)
                .orElse(null);
    }
}
