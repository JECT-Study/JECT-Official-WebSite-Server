package org.ject.support.domain.project.repository;

import static org.ject.support.domain.project.entity.Project.Category;
import static org.ject.support.domain.project.entity.QProject.project;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ject.support.domain.project.dto.ProjectResponse;
import org.ject.support.domain.project.dto.QProjectResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProjectQueryRepositoryImpl implements ProjectQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProjectResponse> findProjectsByCategoryAndSemester(final Category category,
                                                                   final String semester,
                                                                   final Pageable pageable) {
        List<ProjectResponse> content = queryFactory.select(new QProjectResponse(
                        project.id,
                        project.thumbnailUrl,
                        project.name,
                        project.summary,
                        project.description
                ))
                .from(project)
                .where(project.category.eq(category), project.semester.eq(semester))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(project.count())
                .from(project);

        return new PageImpl<>(content, pageable, countQuery.fetchFirst());
    }
}
