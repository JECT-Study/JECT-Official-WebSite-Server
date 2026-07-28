package org.ject.support.domain.member.repository;

import java.util.List;
import java.util.Optional;

import org.ject.support.admin.member.dto.projection.MemberMakersDetailProjection;
import org.ject.support.admin.member.dto.projection.MemberMakersListProjection;
import org.ject.support.admin.member.dto.projection.MemberSupportersDetailProjection;
import org.ject.support.admin.member.dto.projection.MemberSupportersListProjection;
import org.ject.support.admin.member.dto.projection.SearchMemberSemesterProjection;
import org.ject.support.admin.member.dto.request.MemberSemesterSearchCondition;
import org.ject.support.domain.member.dto.TeamMemberNames;

public interface MemberActivityQueryRepository {
	List<SearchMemberSemesterProjection> searchMemberSemesters(
		MemberSemesterSearchCondition condition,
		int limit
	);

	long countMemberSemesters(MemberSemesterSearchCondition conditon);

	TeamMemberNames findMemberNamesByTeamId(Long teamId);

	List<MemberMakersListProjection> findMemberMakersList(Long cursor, Integer limit);

	long countMemberMakersList();

	Optional<MemberMakersDetailProjection> findMemberMakersDetail(Long memberActivityId);

	List<MemberSupportersListProjection> findMemberSupportersList(Long cursor, int limit);

	long countMemberSupportersList();

	Optional<MemberSupportersDetailProjection> findMemberSupportersDetail(Long memberActivityId);
}
