package org.ject.support.domain.recruit.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.recruit.domain.Recruit;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.ject.support.domain.recruit.domain.QRecruit.recruit;

@Repository
@RequiredArgsConstructor
public class RecruitQueryRepositoryImpl implements RecruitQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    @Cacheable(value = "activeRecruit", key = "#jobFamily.name()", unless = "#result == null")
    public Optional<Recruit> findActiveRecruitByJobFamily(final JobFamily jobFamily, final LocalDateTime now) {
        Recruit fetched = jpaQueryFactory.selectFrom(recruit)
                .leftJoin(recruit.questions).fetchJoin()
                .where(recruit.jobFamily.eq(jobFamily), recruit.startDate.before(now).and(recruit.endDate.after(now)))
                .fetchFirst();
        return Optional.ofNullable(fetched);
    }
}
