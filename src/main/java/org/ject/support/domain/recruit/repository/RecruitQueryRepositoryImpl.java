package org.ject.support.domain.recruit.repository;

import static org.ject.support.domain.recruit.domain.QRecruit.recruit;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.ject.support.domain.recruit.domain.Recruit;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RecruitQueryRepositoryImpl implements RecruitQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<Recruit> findByStartDateAfterAndEndDateBefore(final LocalDateTime time) {
        LocalDate date = time.toLocalDate();
        Recruit fetched = jpaQueryFactory.selectFrom(recruit)
                .where(recruit.startDate.after(date).and(recruit.endDate.before(date)))
                .fetchFirst();
        return Optional.ofNullable(fetched);
    }
}
