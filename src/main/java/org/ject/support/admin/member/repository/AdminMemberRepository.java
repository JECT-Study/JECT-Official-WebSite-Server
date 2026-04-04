package org.ject.support.admin.member.repository;

import java.util.List;
import org.ject.support.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminMemberRepository extends JpaRepository<Member, Long>, AdminMemberQueryRepository {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Member m SET m.isDeleted = true WHERE m.id IN :ids")
    void deleteAllByIds(@Param("ids") List<Long> ids);
}
