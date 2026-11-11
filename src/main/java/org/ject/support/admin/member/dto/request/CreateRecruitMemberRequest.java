package org.ject.support.admin.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.apply.domain.Apply;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.recruit.domain.Recruit;
import org.ject.support.domain.recruit.domain.RecruitType;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;

public record CreateRecruitMemberRequest(
        @NotBlank @Size(max = 20) @Pattern(regexp = "^[^\\s]+$") String name,
        @NotBlank @Email @Size(max = 30) String email,
        @NotBlank @Pattern(regexp = "^010\\d{8}$") String phoneNumber,
        List<String> interestedDomains,
        Region region,
        @NotNull JobFamily jobFamily,
        @NotNull RecruitType recruitType,
        @NotNull RecruitTypeDetail recruitTypeDetail,
        CareerDetails careerDetails,
        ExperiencePeriod experiencePeriod,
        @Size(max = 100) String memo,
        Long semesterId
) implements CreateMemberRequest {

    public static CreateRecruitMemberRequest from(Apply apply) {
        Applicant applicant = apply.getApplicant();
        Recruit recruit = apply.getRecruit();
        return new CreateRecruitMemberRequest(applicant.getName(), applicant.getEmail(), applicant.getPhoneNumber(),
                applicant.getInterestedDomains(), applicant.getRegion(), recruit.getJobFamily(), recruit.getRecruitType(),
                recruit.getRecruitTypeDetail(), applicant.getCareerDetails(), applicant.getExperiencePeriod(),
                apply.getNote(), recruit.getSemester() == null ? null : recruit.getSemester().getId());
    }
}
