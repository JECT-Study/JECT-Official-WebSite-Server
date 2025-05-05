package org.ject.support.domain.member.repository;

import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQueryFactory;
import lombok.RequiredArgsConstructor;
import org.ject.support.domain.member.dto.QTeamMemberNames;
import org.ject.support.domain.member.dto.TeamMemberNames;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.ject.support.domain.member.JobFamily.BE;
import static org.ject.support.domain.member.JobFamily.FE;
import static org.ject.support.domain.member.JobFamily.PD;
import static org.ject.support.domain.member.JobFamily.PM;
import static org.ject.support.domain.member.entity.QMember.member;
import static org.ject.support.domain.member.entity.QTeamMember.teamMember;
import static org.ject.support.domain.recruit.domain.QApplicationForm.applicationForm;

@Repository
@RequiredArgsConstructor
public class MemberQueryRepositoryImpl implements MemberQueryRepository {

    private final JPQLQueryFactory queryFactory;

    @Override
    public TeamMemberNames findMemberNamesByTeamId(Long teamId) {
        return queryFactory.selectFrom(member)
                .join(member.teamMembers, teamMember)
                .where(teamMember.team.id.eq(teamId))
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
                        member.id.notIn(JPAExpressions
                                .select(applicationForm.member.id)
                                .from(applicationForm)))
                .fetch();
    }
}
