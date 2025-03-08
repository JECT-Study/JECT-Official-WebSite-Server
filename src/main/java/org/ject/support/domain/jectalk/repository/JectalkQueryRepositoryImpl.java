package org.ject.support.domain.jectalk.repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.data.RestPage;
import org.ject.support.domain.jectalk.dto.JectalkResponse;
import org.ject.support.domain.jectalk.dto.QJectalkResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import static org.ject.support.domain.jectalk.entity.QJectalk.jectalk;

@Repository
@RequiredArgsConstructor
public class JectalkQueryRepositoryImpl implements JectalkQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public RestPage<JectalkResponse> findJectalks(Pageable pageable) {
        List<JectalkResponse> content = queryFactory
                .select(new QJectalkResponse(
                        jectalk.id,
                        jectalk.name,
                        jectalk.youtubeUrl,
                        jectalk.imageUrl,
                        jectalk.summary
                ))
                .from(jectalk)
                .orderBy(jectalk.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(jectalk.count())
                .from(jectalk);

        return new RestPage<>(content, pageable.getPageNumber(), pageable.getPageSize(), countQuery.fetchFirst());
    }
}
