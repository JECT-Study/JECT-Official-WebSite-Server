package org.ject.support.domain.project.repository;

import com.querydsl.core.group.GroupBy;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.ject.support.domain.project.dto.ProjectDetailResponse;
import org.ject.support.domain.project.dto.ProjectResponse;
import org.ject.support.domain.project.dto.QProjectDetailResponse;
import org.ject.support.domain.project.dto.QProjectResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static org.ject.support.domain.member.JobFamily.*;
import static org.ject.support.domain.member.QMember.member;
import static org.ject.support.domain.member.QTeam.team;
import static org.ject.support.domain.member.QTeamMember.teamMember;
import static org.ject.support.domain.project.entity.QProject.project;

@Repository
@RequiredArgsConstructor
public class ProjectQueryRepositoryImpl implements ProjectQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProjectResponse> findProjectsBySemester(final String semester, Pageable pageable) {
        List<ProjectResponse> content = queryFactory.select(new QProjectResponse(
                        project.id,
                        project.thumbnailUrl,
                        project.name,
                        project.summary,
                        project.startDate,
                        project.endDate
                ))
                .from(project)
                .where(project.semester.eq(semester))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(project.count())
                .from(project);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Optional<ProjectDetailResponse> findProjectDetails(Long projectId) {
        return Optional.ofNullable(queryFactory.selectFrom(project)
                .join(project.team, team)
                .leftJoin(team.teamMembers, teamMember)
                .transform(GroupBy.groupBy(project.id).as(new QProjectDetailResponse(
                        project.thumbnailUrl,
                        project.name,
                        project.startDate,
                        project.endDate,
                        GroupBy.list(JPAExpressions.select(member.name)
                                .from(member)
                                .where(teamMember.member.eq(member), member.jobFamily.eq(PM))),
                        GroupBy.list(JPAExpressions.select(member.name)
                                .from(member)
                                .where(teamMember.member.eq(member), member.jobFamily.eq(PD))),
                        GroupBy.list(JPAExpressions.select(member.name)
                                .from(member)
                                .where(teamMember.member.eq(member), member.jobFamily.eq(FE))),
                        GroupBy.list(JPAExpressions.select(member.name)
                                .from(member)
                                .where(teamMember.member.eq(member), member.jobFamily.eq(BE))),
                        project.techStack,
                        project.description,
                        project.serviceUrl
                ))).get(projectId));
    }
}
