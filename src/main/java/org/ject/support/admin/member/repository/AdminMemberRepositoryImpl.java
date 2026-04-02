package org.ject.support.admin.member.repository;

import static org.ject.support.domain.member.entity.QMember.member;
import static org.ject.support.domain.recruit.domain.QSemester.semester;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.data.PageResponse;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.member.dto.MemberProjection;
import org.ject.support.domain.member.dto.QMemberProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdminMemberRepositoryImpl implements AdminMemberQueryRepository {

    private final JPQLQueryFactory queryFactory;

    @Override
    public Page<MemberProjection> findMembers(
            final Role role,
            final JobFamily jobFamily,
            final Long semesterId,
            final Pageable pageable
    ) {
        final List<MemberProjection> content = queryFactory
                .select(new QMemberProjection(
                        member.id,
                        member.name,
                        member.phoneNumber,
                        member.email,
                        member.jobFamily,
                        semester.name.as("semesterName")))
                .from(member)
                .leftJoin(semester)
                .on(semester.id.eq(member.semesterId))
                .where(
                        member.role.eq(role),
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
                .leftJoin(semester)
                .on(semester.id.eq(member.semesterId))
                .where(
                        member.role.eq(role),
                        eqJobFamily(jobFamily),
                        eqSemesterId(semesterId),
                        member.isDeleted.eq(false))
                .fetchOne();


        return PageResponse.from(content, pageable, total);
    }

    private BooleanExpression eqJobFamily(final JobFamily jobFamily) {
        return Optional.ofNullable(jobFamily)
                .map(member.jobFamily::eq)
                .orElse(null);
    }

    private BooleanExpression eqSemesterId(final Long semesterId) {
        return Optional.ofNullable(semesterId)
                .map(semester.id::eq)
                .orElse(null);
    }
}
