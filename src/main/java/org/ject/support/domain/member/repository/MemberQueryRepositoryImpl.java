package org.ject.support.domain.member.repository;

import static org.ject.support.domain.member.JobFamily.BE;
import static org.ject.support.domain.member.JobFamily.FE;
import static org.ject.support.domain.member.JobFamily.PD;
import static org.ject.support.domain.member.JobFamily.PM;
import static org.ject.support.domain.member.entity.QMember.member;
import static org.ject.support.domain.member.entity.QTeamMember.teamMember;
import static org.ject.support.domain.recruit.domain.QApplicationForm.applicationForm;
import static org.ject.support.domain.recruit.domain.QSemester.semester;

import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.data.PageResponse;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.dto.MemberResponse;
import org.ject.support.domain.member.dto.QMemberResponse;
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
                                .when(member.jobFamily.eq(PM))
                                .then(member.name)
                                .otherwise((String) null)),
                        GroupBy.list(new CaseBuilder()
                                .when(member.jobFamily.eq(PD))
                                .then(member.name)
                                .otherwise((String) null)),
                        GroupBy.list(new CaseBuilder()
                                .when(member.jobFamily.eq(FE))
                                .then(member.name)
                                .otherwise((String) null)),
                        GroupBy.list(new CaseBuilder()
                                .when(member.jobFamily.eq(BE))
                                .then(member.name)
                                .otherwise((String) null))
                ))).get(teamId);
    }

    @Override
    public List<String> findEmailsByIdsAndNotApply(List<Long> applicantIds) {
        return queryFactory.select(member.email)
                .from(member)
                .where(member.id.in(applicantIds),
                        member.isDeleted.eq(false),
                        member.id.notIn(JPAExpressions
                                .select(applicationForm.member.id)
                                .from(applicationForm)))
                .fetch();
    }

    @Override
    public Page<MemberResponse> findMembers(
            final Role role,
            final JobFamily jobFamily,
            final Long semesterId,
            final Pageable pageable
    ) {
        List<MemberResponse> content = queryFactory
                .select(new QMemberResponse(
                        member.id,
                        member.name,
                        member.phoneNumber,
                        member.email,
                        member.jobFamily,
                        semester.name.as("semesterName")
                ))
                .from(member)
                .leftJoin(semester)
                    .on(semester.id.eq(member.semesterId))
                .where(
                        member.role.eq(role),
                        eqJobFamily(jobFamily),
                        eqSemesterId(semesterId),
                        member.isDeleted.eq(false)
                )
                .orderBy(member.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(member.count())
                .from(member)
                .leftJoin(semester)
                    .on(semester.id.eq(member.semesterId))
                .where(
                        member.role.eq(role),
                        eqJobFamily(jobFamily),
                        eqSemesterId(semesterId),
                        member.isDeleted.eq(false)
                ).fetchOne();


        return PageResponse.from(content, pageable, total);
    }

    private BooleanExpression eqJobFamily(JobFamily jobFamily) {
        return Optional.ofNullable(jobFamily)
                        .map(member.jobFamily::eq)
                        .orElse(null);
    }

    private BooleanExpression eqSemesterId(Long semesterId) {
        return Optional.ofNullable(semesterId)
                .map(semester.id::eq)
                .orElse(null);
    }
}