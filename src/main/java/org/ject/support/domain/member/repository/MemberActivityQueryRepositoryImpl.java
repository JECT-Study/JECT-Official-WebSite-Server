package org.ject.support.domain.member.repository;

import static org.ject.support.domain.member.entity.QMember.*;
import static org.ject.support.domain.member.entity.QMemberActivity.*;
import static org.ject.support.domain.member.entity.QMemberSemester.*;

import java.util.List;

import org.ject.support.admin.member.dto.projection.SearchMemberSemesterProjection;
import org.ject.support.admin.member.dto.request.MemberSemesterSearchCondition;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MemberActivityQueryRepositoryImpl implements MemberActivityQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	//다중 값은 in, 단일 값은 eq로 필터링
	//id기준 내림차순 - 최신순
	//size+1개 조회
	@Override
	public List<SearchMemberSemesterProjection> searchMemberSemesters(MemberSemesterSearchCondition condition, int limit) {
		return jpaQueryFactory.select(Projections.constructor(
			SearchMemberSemesterProjection.class,
			memberActivity.id,
			member.name,
			memberActivity.jobFamily,
			member.phoneNumber,
			memberActivity.careerDetails,
			memberActivity.experiencePeriod
			))
			.from(memberActivity)
			.join(member).on(member.id.eq(memberActivity.memberId))
			.join(memberSemester).on(memberSemester.id.eq(memberActivity.id))
			.where(
				cursorLt(condition.cursor()),
				semesterIdIn(condition.semesterId()),
				jobFamilyIn(condition.jobFamilies()),
				careerDetailsIn(condition.careerDetails()),
				teamIdIn(condition.teamIds()),
				recruitTypeDetailsIn(condition.recruitTypeDetails()),
				member.isDeleted.isFalse(),
				memberActivity.isDeleted.isFalse(),
				memberActivity.memberType.eq(MemberType.SEMESTER)
			).orderBy(memberActivity.id.desc())
			.limit(limit)
			.fetch();
	}

	@Override
	public long countMemberSemesters(MemberSemesterSearchCondition condition) {
		Long count = jpaQueryFactory.select(memberActivity.id.count())
			.from(memberActivity)
			.join(member).on(member.id.eq(memberActivity.memberId))
			.join(memberSemester).on(memberSemester.id.eq(memberActivity.id))
			.where(
				semesterIdIn(condition.semesterId()),
				jobFamilyIn(condition.jobFamilies()),
				careerDetailsIn(condition.careerDetails()),
				teamIdIn(condition.teamIds()),
				recruitTypeDetailsIn(condition.recruitTypeDetails()),
				member.isDeleted.isFalse(),
				memberActivity.isDeleted.isFalse(),
				memberActivity.memberType.eq(MemberType.SEMESTER)
			).fetchOne();

		return count == null ? 0L : count;
	}

	private BooleanExpression cursorLt(Long cursor){
		return cursor == null ? null : memberActivity.id.lt(cursor);
	}

	private BooleanExpression semesterIdIn(Long semesterId) {
		return semesterId == null ? null
			: memberSemester.semesterId.eq(semesterId);
	}

	private BooleanExpression jobFamilyIn(List<JobFamily> jobFamilies) {
		return jobFamilies == null || jobFamilies.isEmpty()
			? null
			: memberActivity.jobFamily.in(jobFamilies);
	}

	private BooleanExpression careerDetailsIn(List<CareerDetails> careerDetails) {
		return careerDetails == null || careerDetails.isEmpty()
			? null
			: memberActivity.careerDetails.in(careerDetails);
	}

	private BooleanExpression teamIdIn(List<Long> teamIds) {
		return teamIds == null || teamIds.isEmpty()
			? null
			: memberSemester.teamId.in(teamIds);
	}

	private BooleanExpression recruitTypeDetailsIn(List<RecruitTypeDetail> recruitTypeDetails) {
		return recruitTypeDetails == null || recruitTypeDetails.isEmpty()
			? null
			: memberActivity.recruitTypeDetail.in(recruitTypeDetails);
	}
}
