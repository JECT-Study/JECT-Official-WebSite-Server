package org.ject.support.domain.member.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.member.entity.MemberActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberActivityRepository extends JpaRepository<MemberActivity, Long>, MemberActivityQueryRepository {

	Optional<MemberActivity> findByIdAndMemberType(Long id, MemberType memberType);

	List<MemberActivity> findAllByIdInAndMemberType(Collection<Long> ids, MemberType memberType);

	boolean existsByMemberId(Long memberId);

	@Query("select distinct ma.memberId from MemberActivity ma where ma.memberId in :memberIds")
	Set<Long> findMemberIdsWithActivity(@Param("memberIds") Collection<Long> memberIds);

	@Query("""
		select count(ma) > 0
		from MemberActivity ma
		join ma.memberSemester ms
		where ma.memberId = :memberId
		  and ma.memberType = :memberType
		  and ms.semesterId = :semesterId
		""")
	boolean existsSemesterActivity(
		@Param("memberId") Long memberId,
		@Param("memberType") MemberType memberType,
		@Param("semesterId") Long semesterId
	);

	@Query("""
		select count(ma) > 0
		from MemberActivity ma
		where ma.memberId = :memberId
		  and ma.memberType = org.ject.support.domain.member.MemberType.MAKERS
		  and ma.activityStatus = org.ject.support.domain.member.ActivityStatus.ACTIVE
		""")
	boolean existsActiveMakersActivityByMemberId(@Param("memberId") Long memberId);

	@Query("""
		select count(ma) > 0
		from MemberActivity ma
		where ma.memberId = :memberId
		  and ma.memberType = org.ject.support.domain.member.MemberType.SUPPORTERS
		  and ma.activityStatus = org.ject.support.domain.member.ActivityStatus.ACTIVE
		""")
	boolean existsActiveSupportersActivityByMemberId(@Param("memberId") Long memberId);
}
