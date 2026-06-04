package org.ject.support.domain.applicant.repository;

import static org.ject.support.domain.applicant.entity.QApplicant.applicant;
import static org.ject.support.domain.apply.domain.ApplyStatus.SUBMITTED;
import static org.ject.support.domain.apply.domain.QApply.apply;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.account.dto.AdminAccountSearchCondition;
import org.ject.support.common.data.PageResponse;
import org.ject.support.domain.applicant.dto.ApplicantAccountProjection;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ApplicantQueryRepositoryImpl implements ApplicantQueryRepository {

    private final JPQLQueryFactory queryFactory;

    @Override
    public List<String> findEmailsByIdsAndNotSubmitted(List<Long> applicantIds) {
        return queryFactory.select(applicant.email)
                .from(applicant)
                .join(apply)
                .on(applicant.id.eq(apply.member.id))
                .where(applicant.id.in(applicantIds),
                        applicant.isDeleted.eq(false),
                        apply.status.eq(SUBMITTED).not())
                .fetch();
    }

    @Override
    public Page<ApplicantAccountProjection> findAccounts(final AdminAccountSearchCondition condition,
                                                         final Pageable pageable) {
        List<ApplicantAccountProjection> content = queryFactory
                .select(Projections.constructor(
                        ApplicantAccountProjection.class,
                        applicant.id,
                        applicant.email,
                        applicant.name,
                        applicant.role,
                        applicant.status))
                .from(applicant)
                .where(
                        applicant.isDeleted.eq(false),
                        applicant.role.in(Role.backofficeRoles()),
                        inRoles(condition.roles()),
                        inStatuses(condition.statuses())
                )
                .orderBy(applicant.createdAt.desc(), applicant.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(applicant.count())
                .from(applicant)
                .where(
                        applicant.isDeleted.eq(false),
                        applicant.role.in(Role.backofficeRoles()),
                        inRoles(condition.roles()),
                        inStatuses(condition.statuses())
                )
                .fetchOne();

        return PageResponse.from(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression inRoles(final List<Role> roles) {
        return Optional.ofNullable(roles)
                .filter(values -> !values.isEmpty())
                .map(applicant.role::in)
                .orElse(null);
    }

    private BooleanExpression inStatuses(final List<MemberStatus> statuses) {
        return Optional.ofNullable(statuses)
                .filter(values -> !values.isEmpty())
                .map(applicant.status::in)
                .orElse(null);
    }
}
