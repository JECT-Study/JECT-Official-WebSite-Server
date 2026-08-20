package org.ject.support.admin.mail.repository;

import static org.ject.support.domain.apply.domain.QApplicationForm.applicationForm;
import static org.ject.support.domain.apply.domain.QApply.apply;
import static org.ject.support.domain.applicant.entity.QApplicant.applicant;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.mail.dto.MailTargetResponse;
import org.ject.support.domain.apply.domain.ApplyStatus;
import org.ject.support.domain.apply.domain.SelectionResult;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MailTargetQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<MailTargetResponse> findTargets(Long recruitId, SelectionResult selectionResult) {
        return queryFactory
                .select(Projections.constructor(
                        MailTargetResponse.class,
                        apply.id,
                        applicant.name,
                        applicant.phoneNumber,
                        applicant.email,
                        apply.selectionResult,
                        apply.waitlistNumber))
                .from(apply)
                .join(apply.applicant, applicant)
                .join(apply.applicationForm, applicationForm)
                .where(
                        apply.recruit.id.eq(recruitId),
                        apply.status.eq(ApplyStatus.SUBMITTED),
                        apply.isDeleted.eq(false),
                        applicant.isDeleted.eq(false),
                        eqSelectionResult(selectionResult))
                .orderBy(apply.submittedAt.desc(), apply.id.desc())
                .fetch();
    }

    private BooleanExpression eqSelectionResult(SelectionResult selectionResult) {
        return Optional.ofNullable(selectionResult)
                .map(apply.selectionResult::eq)
                .orElse(null);
    }
}
