package org.ject.support.domain.member.repository;

import static org.ject.support.domain.apply.domain.ApplyStatus.SUBMITTED;
import static org.ject.support.domain.apply.domain.QApply.apply;
import static org.ject.support.domain.member.JobFamily.BE;
import static org.ject.support.domain.member.JobFamily.FE;
import static org.ject.support.domain.member.JobFamily.PD;
import static org.ject.support.domain.member.JobFamily.PM;
import static org.ject.support.domain.member.entity.QMember.member;
import static org.ject.support.domain.member.entity.QTeamMember.teamMember;

import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.account.dto.AdminAccountSearchCondition;
import org.ject.support.common.data.PageResponse;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.dto.MemberAccountProjection;
import org.ject.support.domain.member.dto.QTeamMemberNames;
import org.ject.support.domain.member.dto.TeamMemberNames;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberQueryRepositoryImpl implements MemberQueryRepository {

    private final JPQLQueryFactory queryFactory;

    @Override
    public TeamMemberNames findMemberNamesByTeamId(Long teamId) {
        return queryFactory.selectFrom(member)
                .join(member.teamMembers, teamMember)
                .where(teamMember.team.id.eq(teamId),
                        member.isDeleted.eq(false))
                .transform(GroupBy.groupBy(teamMember.team.id).as(new QTeamMemberNames(
                        GroupBy.list(new CaseBuilder()
                                .when(teamMember.jobFamily.eq(PM)
                                        .or(teamMember.jobFamily.isNull().and(member.jobFamily.eq(PM))))
                                .then(member.name)
                                .otherwise((String) null)),
                        GroupBy.list(new CaseBuilder()
                                .when(teamMember.jobFamily.eq(PD)
                                        .or(teamMember.jobFamily.isNull().and(member.jobFamily.eq(PD))))
                                .then(member.name)
                                .otherwise((String) null)),
                        GroupBy.list(new CaseBuilder()
                                .when(teamMember.jobFamily.eq(FE)
                                        .or(teamMember.jobFamily.isNull().and(member.jobFamily.eq(FE))))
                                .then(member.name)
                                .otherwise((String) null)),
                        GroupBy.list(new CaseBuilder()
                                .when(teamMember.jobFamily.eq(BE)
                                        .or(teamMember.jobFamily.isNull().and(member.jobFamily.eq(BE))))
                                .then(member.name)
                                .otherwise((String) null))
                ))).get(teamId);
    }

    @Override
    public List<String> findEmailsByIdsAndNotSubmitted(List<Long> applicantIds) {
        return queryFactory.select(member.email)
                .from(member)
                .join(apply)
                .on(member.id.eq(apply.member.id))
                .where(member.id.in(applicantIds),
                        member.isDeleted.eq(false),
                        apply.status.eq(SUBMITTED).not())
                .fetch();
    }

    @Override
    public Page<MemberAccountProjection> findAccounts(final AdminAccountSearchCondition condition,
                                                      final Pageable pageable) {
        List<MemberAccountProjection> content = queryFactory
                .select(Projections.constructor(
                        MemberAccountProjection.class,
                        member.id,
                        member.email,
                        member.name,
                        member.role,
                        member.status))
                .from(member)
                .where(
                        member.isDeleted.eq(false),
                        member.role.in(Role.backofficeRoles()),
                        inRoles(condition.roles()),
                        inStatuses(condition.statuses())
                )
                .orderBy(member.createdAt.desc(), member.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(member.count())
                .from(member)
                .where(
                        member.isDeleted.eq(false),
                        member.role.in(Role.backofficeRoles()),
                        inRoles(condition.roles()),
                        inStatuses(condition.statuses())
                )
                .fetchOne();

        return PageResponse.from(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression inRoles(final List<Role> roles) {
        return Optional.ofNullable(roles)
                .filter(values -> !values.isEmpty())
                .map(member.role::in)
                .orElse(null);
    }

    private BooleanExpression inStatuses(final List<MemberStatus> statuses) {
        return Optional.ofNullable(statuses)
                .filter(values -> !values.isEmpty())
                .map(member.status::in)
                .orElse(null);
    }
}
