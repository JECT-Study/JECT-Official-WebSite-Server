package org.ject.support.domain.jectalk.repository;

import static org.ject.support.domain.jectalk.entity.QJectalk.jectalk;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.data.PageResponse;
import org.ject.support.domain.jectalk.dto.JectalkResponse;
import org.ject.support.domain.jectalk.dto.QJectalkResponse;
import org.ject.support.domain.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JectalkQueryRepositoryImpl implements JectalkQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<JectalkResponse> findJectalks(Pageable pageable, Project.Category category) {
        List<JectalkResponse> content = queryFactory
                .select(new QJectalkResponse(
                        jectalk.id,
                        jectalk.title,
                        jectalk.description,
                        jectalk.contentUrl,
                        jectalk.contentType,
                        jectalk.thumbnailUrl,
                        jectalk.author
                ))
                .from(jectalk)
                .where(categoryEq(category))
                .orderBy(jectalk.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(jectalk.count())
                .from(jectalk)
                .where(categoryEq(category));

        return PageResponse.from(content, pageable, countQuery.fetchFirst());
    }

    private BooleanExpression categoryEq(Project.Category category) {
        return Optional.ofNullable(category)
                .map(jectalk.category::eq)
                .orElse(null);
    }
}
