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
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPQLQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ject.support.domain.member.dto.QTeamMemberNames;
import org.ject.support.domain.member.dto.TeamMemberNames;
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
}
