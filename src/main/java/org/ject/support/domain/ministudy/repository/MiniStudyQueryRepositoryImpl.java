package org.ject.support.domain.ministudy.repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.data.RestPage;
import org.ject.support.domain.ministudy.dto.MiniStudyResponse;
import org.ject.support.domain.ministudy.dto.QMiniStudyResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import static org.ject.support.domain.ministudy.entity.QMiniStudy.miniStudy;

@Repository
@RequiredArgsConstructor
public class MiniStudyQueryRepositoryImpl implements MiniStudyQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public RestPage<MiniStudyResponse> findMiniStudies(Pageable pageable) {
        List<MiniStudyResponse> content = queryFactory
                .select(new QMiniStudyResponse(
                        miniStudy.id,
                        miniStudy.name,
                        miniStudy.linkUrl,
                        miniStudy.imageUrl,
                        miniStudy.summary
                ))
                .from(miniStudy)
                .orderBy(miniStudy.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(miniStudy.count())
                .from(miniStudy);

        return new RestPage<>(content, pageable.getPageNumber(), pageable.getPageSize(), countQuery.fetchFirst());
    }
}
