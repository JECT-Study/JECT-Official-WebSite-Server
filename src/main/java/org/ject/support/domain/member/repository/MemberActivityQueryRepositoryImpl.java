package org.ject.support.domain.member.repository;

import static org.ject.support.domain.member.entity.QMember.*;
import static org.ject.support.domain.member.entity.QMemberActivity.*;
import static org.ject.support.domain.member.entity.QMemberSemester.*;

import java.util.List;

import org.ject.support.admin.member.dto.projection.SearchMemberSemesterProjection;
import org.ject.support.admin.member.dto.request.MemberSemesterSearchCondition;
import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.member.dto.TeamMemberNames;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MemberActivityQueryRepositoryImpl implements MemberActivityQueryRepository {

	private final JPAQueryFactory jpaQueryFactory;

	// 단일 값 기준으로 일반 구성원 필터링
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
			memberActivity.experiencePeriod,
			memberActivity.activityStatus
			))
			.from(memberActivity)
			.join(member).on(member.id.eq(memberActivity.memberId))
			.join(memberSemester).on(memberSemester.id.eq(memberActivity.id))
			.where(
				cursorLt(condition.cursor()),
				semesterIdEq(condition.semesterId()),
				jobFamilyEq(condition.jobFamily()),
				careerDetailsEq(condition.careerDetails()),
				teamIdEq(condition.teamId()),
				recruitTypeDetailEq(condition.recruitTypeDetail()),
				statusEq(condition.status()),
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
				semesterIdEq(condition.semesterId()),
				jobFamilyEq(condition.jobFamily()),
				careerDetailsEq(condition.careerDetails()),
				teamIdEq(condition.teamId()),
				recruitTypeDetailEq(condition.recruitTypeDetail()),
				statusEq(condition.status()),
				member.isDeleted.isFalse(),
				memberActivity.isDeleted.isFalse(),
				memberActivity.memberType.eq(MemberType.SEMESTER)
			).fetchOne();

		return count == null ? 0L : count;
	}

	// 팀에 소속된 일반 구성원 이름을 직군별로 조회
	@Override
	public TeamMemberNames findMemberNamesByTeamId(Long teamId) {
		List<Tuple> teamMembers = jpaQueryFactory
			.select(member.name, memberActivity.jobFamily)
			.from(memberActivity)
			.join(member).on(member.id.eq(memberActivity.memberId))
			.join(memberSemester).on(memberSemester.id.eq(memberActivity.id))
			.where(
				memberSemester.teamId.eq(teamId),
				memberActivity.memberType.eq(MemberType.SEMESTER),
				memberActivity.activityStatus.in(ActivityStatus.ACTIVE, ActivityStatus.COMPLETED),
				member.isDeleted.isFalse(),
				memberActivity.isDeleted.isFalse(),
				member.name.isNotNull()
			)
			.orderBy(memberActivity.id.asc())
			.fetch();

		return new TeamMemberNames(
			findNamesByJobFamily(teamMembers, JobFamily.PM),
			findNamesByJobFamily(teamMembers, JobFamily.PD),
			findNamesByJobFamily(teamMembers, JobFamily.FE),
			findNamesByJobFamily(teamMembers, JobFamily.BE)
		);
	}

	// 조회 결과에서 지정 직군의 이름만 분리
	private List<String> findNamesByJobFamily(List<Tuple> teamMembers, JobFamily jobFamily) {
		return teamMembers.stream()
			.filter(teamMember -> jobFamily == teamMember.get(memberActivity.jobFamily))
			.map(teamMember -> teamMember.get(member.name))
			.toList();
	}

	private BooleanExpression cursorLt(Long cursor){
		return cursor == null ? null : memberActivity.id.lt(cursor);
	}

	private BooleanExpression semesterIdEq(Long semesterId) {
		return semesterId == null ? null
			: memberSemester.semesterId.eq(semesterId);
	}

	private BooleanExpression jobFamilyEq(JobFamily jobFamily) {
		return jobFamily == null ? null
			: memberActivity.jobFamily.eq(jobFamily);
	}

	private BooleanExpression careerDetailsEq(CareerDetails careerDetails) {
		return careerDetails == null ? null
			: memberActivity.careerDetails.eq(careerDetails);
	}

	private BooleanExpression teamIdEq(Long teamId) {
		return teamId == null ? null
			: memberSemester.teamId.eq(teamId);
	}

	private BooleanExpression recruitTypeDetailEq(RecruitTypeDetail recruitTypeDetail) {
		return recruitTypeDetail == null ? null
			: memberActivity.recruitTypeDetail.eq(recruitTypeDetail);
	}

	private BooleanExpression statusEq(ActivityStatus status) {
		return status == null ? null
			: memberActivity.activityStatus.eq(status);
	}
}
