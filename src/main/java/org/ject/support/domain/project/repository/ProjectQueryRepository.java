package org.ject.support.domain.project.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.project.dto.ProjectDetailResponse;
import org.ject.support.domain.project.dto.ProjectResponse;
import org.ject.support.domain.project.dto.QProjectResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.ject.support.domain.member.JobFamily.*;
import static org.ject.support.domain.member.QMember.member;
import static org.ject.support.domain.member.QTeam.team;
import static org.ject.support.domain.member.QTeamMember.teamMember;
import static org.ject.support.domain.project.entity.QProject.project;

@Repository
@RequiredArgsConstructor
public class ProjectQueryRepository {

    private final JPAQueryFactory queryFactory;

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

    public ProjectDetailResponse findProjectDetails(Long projectId) {
        List<Tuple> tuples = queryFactory.select(
                        project.thumbnailUrl,
                        project.name,
                        project.startDate,
                        project.endDate,
                        project.techStack,
                        project.description,
                        project.serviceUrl,
                        team.id,
                        member.name,
                        member.jobFamily)
                .from(project)
                .join(project.team, team)
                .leftJoin(teamMember).on(teamMember.team.id.eq(team.id))
                .leftJoin(teamMember.member, member)
                .where(project.id.eq(projectId))
                .fetch();

        return createProjectDetailResponse(tuples);
    }

    private ProjectDetailResponse createProjectDetailResponse(List<Tuple> tuples) {
        Tuple firstTuple = tuples.getFirst();
        return ProjectDetailResponse.builder()
                .thumbnailUrl(firstTuple.get(project.thumbnailUrl))
                .name(firstTuple.get(project.name))
                .startDate(firstTuple.get(project.startDate))
                .endDate(firstTuple.get(project.endDate))
                .projectManagers(getNames(tuples, PM))
                .productDesigners(getNames(tuples, PD))
                .frontendDevelopers(getNames(tuples, FE))
                .backendDevelopers(getNames(tuples, BE))
                .techStack(firstTuple.get(project.techStack))
                .description(firstTuple.get(project.description))
                .serviceUrl(firstTuple.get(project.serviceUrl))
                .build();
    }

    private List<String> getNames(List<Tuple> tuples, JobFamily jobFamily) {
        return tuples.stream()
                .filter(tuple -> tuple.get(member.jobFamily) == jobFamily)
                .map(tuple -> tuple.get(member.name))
                .toList();
    }
}
