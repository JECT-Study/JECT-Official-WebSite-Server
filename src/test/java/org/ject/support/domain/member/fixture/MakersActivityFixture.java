package org.ject.support.domain.member.fixture;

import org.ject.support.domain.member.Availability;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.CareerLevel;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MakersTeam;
import org.ject.support.domain.member.entity.MemberActivity;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

public final class MakersActivityFixture {

    private Long memberId = 1L;
    private JobFamily jobFamily = JobFamily.FE;
    private MakersTeam makersTeam = MakersTeam.TEAM_1;

    private MakersActivityFixture() {
    }

    public static MakersActivityFixture makersActivity() {
        return new MakersActivityFixture();
    }

    public MakersActivityFixture memberId(Long memberId) {
        this.memberId = memberId;
        return this;
    }

    public MakersActivityFixture jobFamily(JobFamily jobFamily) {
        this.jobFamily = jobFamily;
        return this;
    }

    public MakersActivityFixture makersTeam(MakersTeam makersTeam) {
        this.makersTeam = makersTeam;
        return this;
    }

    public MemberActivity build() {
        return MemberActivity.createMakersActivity(
            memberId,
            jobFamily,
            RecruitTypeDetail.REGULAR,
            CareerDetails.EMPLOYEE,
            ExperiencePeriod.ONE_TO_TWO,
            "테스트 메모",
            makersTeam,
            Availability.HIGHLY_AVAILABLE,
            Availability.AVAILABLE_BY_TOPIC,
            Availability.CONSIDER_LATER,
            CareerLevel.JUNIOR,
            "Spring",
            "JECT",
            "백오피스",
            "MK-001"
        );
    }
}
