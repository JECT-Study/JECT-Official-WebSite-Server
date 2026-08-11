package org.ject.support.admin.mail.domain;

import static org.ject.support.admin.mail.domain.QMailScenario.mailScenario;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.data.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MailScenarioQueryRepositoryImpl implements MailScenarioQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<MailScenario> findScenarios(MailScenarioCategory category,
                                            MailScenarioType type,
                                            Pageable pageable) {
        List<MailScenario> content = queryFactory
                .selectFrom(mailScenario)
                .where(eqCategory(category), eqType(type))
                .orderBy(mailScenario.createdAt.desc(), mailScenario.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(mailScenario.count())
                .from(mailScenario)
                .where(eqCategory(category), eqType(type))
                .fetchOne();

        return PageResponse.from(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression eqCategory(MailScenarioCategory category) {
        return Optional.ofNullable(category)
                .map(mailScenario.category::eq)
                .orElse(null);
    }

    private BooleanExpression eqType(MailScenarioType type) {
        return Optional.ofNullable(type)
                .map(mailScenario.type::eq)
                .orElse(null);
    }
}
