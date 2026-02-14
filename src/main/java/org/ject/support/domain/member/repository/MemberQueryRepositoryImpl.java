package org.ject.support.domain.member.repository;

import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.JPQLQueryFactory;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.data.PageResponse;
import org.ject.support.domain.admin.dto.QMemberResponse;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.admin.dto.MemberResponse;
import org.ject.support.domain.member.dto.QTeamMemberNames;
import org.ject.support.domain.member.dto.TeamMemberNames;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static org.ject.support.domain.apply.domain.Apply.Status.SUBMITTED;
import static org.ject.support.domain.apply.domain.QApply.apply;
import static org.ject.support.domain.member.JobFamily.BE;
import static org.ject.support.domain.member.JobFamily.FE;
import static org.ject.support.domain.member.JobFamily.PD;
import static org.ject.support.domain.member.JobFamily.PM;
import static org.ject.support.domain.member.entity.QMember.member;
import static org.ject.support.domain.member.entity.QTeamMember.teamMember;
import org.ject.support.domain.member.entity.QTeam;
import static org.ject.support.domain.recruit.domain.QSemester.semester;

@Repository
@RequiredArgsConstructor
public class MemberQueryRepositoryImpl implements MemberQueryRepository {

        private final JPQLQueryFactory queryFactory;
        private final QTeam team = QTeam.team;

        @Override
        public TeamMemberNames findMemberNamesByTeamId(Long teamId) {
                return queryFactory.selectFrom(member)
                                .join(member.teamMembers, teamMember)
                                .where(teamMember.team.id.eq(teamId),
                                                member.isDeleted.eq(false))
                                .transform(GroupBy.groupBy(teamMember.team.id).as(new QTeamMemberNames(
                                                GroupBy.list(new CaseBuilder()
                                                                .when(teamMember.jobFamily.eq(PM))
                                                                .then(member.name)
                                                                .otherwise((String) null)),
                                                GroupBy.list(new CaseBuilder()
                                                                .when(teamMember.jobFamily.eq(PD))
                                                                .then(member.name)
                                                                .otherwise((String) null)),
                                                GroupBy.list(new CaseBuilder()
                                                                .when(teamMember.jobFamily.eq(FE))
                                                                .then(member.name)
                                                                .otherwise((String) null)),
                                                GroupBy.list(new CaseBuilder()
                                                                .when(teamMember.jobFamily.eq(BE))
                                                                .then(member.name)
                                                                .otherwise((String) null)))))
                                .get(teamId);
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
        public Page<MemberResponse> findMembers(
                        final Role role,
                        final JobFamily jobFamily,
                        final Long semesterId,
                        final Pageable pageable) {
                final List<MemberResponse> content = queryFactory
                                .select(new QMemberResponse(
                                                member.id,
                                                member.name,
                                                member.phoneNumber,
                                                member.email,
                                                teamMember.jobFamily,
                                                semester.name.as("semesterName")))
                                .from(member)
                                .join(member.teamMembers, teamMember)
                                .join(teamMember.team, team)
                                .join(semester).on(team.semesterId.eq(semester.id))
                                .where(
                                                eqRole(role),
                                                eqJobFamily(jobFamily),
                                                eqSemesterId(semesterId),
                                                member.isDeleted.eq(false))
                                .orderBy(member.createdAt.desc())
                                .offset(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .fetch();

                final Long total = queryFactory
                                .select(member.count())
                                .from(member)
                                .join(member.teamMembers, teamMember)
                                .join(teamMember.team, team)
                                .join(semester).on(team.semesterId.eq(semester.id))
                                .where(
                                                eqRole(role),
                                                eqJobFamily(jobFamily),
                                                eqSemesterId(semesterId),
                                                member.isDeleted.eq(false))
                                .fetchOne();

                return PageResponse.from(content, pageable, total);
        }

        private BooleanExpression eqRole(final Role role) {
                return Optional.ofNullable(role)
                                .map(member.role::eq)
                                .orElse(null);
        }

        private BooleanExpression eqJobFamily(final JobFamily jobFamily) {
                return Optional.ofNullable(jobFamily)
                                .map(teamMember.jobFamily::eq)
                                .orElse(null);
        }

        private BooleanExpression eqSemesterId(final Long semesterId) {
                return Optional.ofNullable(semesterId)
                                .map(semester.id::eq)
                                .orElse(null);
        }
}