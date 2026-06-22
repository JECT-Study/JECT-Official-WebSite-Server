package org.ject.support.domain.member.fixture;

import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.entity.MemberActivity;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

public final class SemesterActivityFixture {

    private Long memberId = 1L;
    private JobFamily jobFamily = JobFamily.BE;
    private RecruitTypeDetail recruitTypeDetail = RecruitTypeDetail.REGULAR;
    private CareerDetails careerDetails = CareerDetails.EMPLOYEE;
    private ExperiencePeriod experiencePeriod = ExperiencePeriod.ONE_TO_TWO;
    private String memo = "테스트 메모";
    private Long semesterId = 2L;
    private Long teamId = 3L;

    private SemesterActivityFixture() {
    }

    public static SemesterActivityFixture semesterActivity() {
        return new SemesterActivityFixture();
    }

    public SemesterActivityFixture memberId(Long memberId) {
        this.memberId = memberId;
        return this;
    }

    public SemesterActivityFixture jobFamily(JobFamily jobFamily) {
        this.jobFamily = jobFamily;
        return this;
    }

    public SemesterActivityFixture recruitTypeDetail(RecruitTypeDetail recruitTypeDetail) {
        this.recruitTypeDetail = recruitTypeDetail;
        return this;
    }

    public SemesterActivityFixture careerDetails(CareerDetails careerDetails) {
        this.careerDetails = careerDetails;
        return this;
    }

    public SemesterActivityFixture experiencePeriod(ExperiencePeriod experiencePeriod) {
        this.experiencePeriod = experiencePeriod;
        return this;
    }

    public SemesterActivityFixture memo(String memo) {
        this.memo = memo;
        return this;
    }

    public SemesterActivityFixture semesterId(Long semesterId) {
        this.semesterId = semesterId;
        return this;
    }

    public SemesterActivityFixture teamId(Long teamId) {
        this.teamId = teamId;
        return this;
    }

    public MemberActivity build() {
        return MemberActivity.createSemesterActivity(
            memberId,
            jobFamily,
            recruitTypeDetail,
            careerDetails,
            experiencePeriod,
            memo,
            semesterId,
            teamId
        );
    }
}
