package org.ject.support.admin.member.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ject.support.domain.member.fixture.MemberFixture.member;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.ject.support.admin.member.dto.request.CreateMemberMakersRequest;
import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.Availability;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.CareerLevel;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MakersTeam;
import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.entity.MemberActivity;
import org.ject.support.domain.member.entity.MemberMakers;
import org.ject.support.domain.member.exception.MemberErrorCode;
import org.ject.support.domain.member.repository.MemberActivityRepository;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
import org.ject.support.testconfig.AuthenticatedUser;
import org.ject.support.testconfig.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@AutoConfigureMockMvc
@Transactional
@AuthenticatedUser(isAdmin = true)
@TestPropertySource(properties = {"spring.data.redis.repositories.enabled=false"})
class AdminMemberMakersControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private MemberActivityRepository memberActivityRepository;

	@Test
	@DisplayName("메이커스팀 구성원을 추가한다")
	void 메이커스팀_구성원을_추가한다() throws Exception {
		// given
		String email = uniqueEmail("makers");
		CreateMemberMakersRequest request = createMemberMakersRequest(email);

		// when
		mockMvc.perform(post("/admin/members/makers")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("SUCCESS"));

		// then
		Member member = memberRepository.findByEmail(email).orElseThrow();
		MemberActivity memberActivity = findOnlyActivity(member.getId());
		MemberMakers memberMakers = memberActivity.getMemberMakers();

		assertThat(member.getName()).isEqualTo(request.name());
		assertThat(member.getPhoneNumber()).isEqualTo(request.phoneNumber());
		assertThat(member.getInterestedDomains()).containsExactlyElementsOf(request.interestedDomains());
		assertThat(member.getRegion()).isEqualTo(request.region());
		assertThat(memberActivity.getMemberType()).isEqualTo(MemberType.MAKERS);
		assertThat(memberActivity.getActivityStatus()).isEqualTo(ActivityStatus.ACTIVE);
		assertThat(memberActivity.getJobFamily()).isEqualTo(request.jobFamily());
		assertThat(memberActivity.getRecruitTypeDetail()).isEqualTo(request.recruitTypeDetail());
		assertThat(memberActivity.getCareerDetails()).isEqualTo(request.careerDetails());
		assertThat(memberActivity.getExperiencePeriod()).isEqualTo(request.experiencePeriod());
		assertThat(memberActivity.getMemo()).isEqualTo(request.memo());
		assertThat(memberMakers.getMakersTeam()).isEqualTo(request.makersTeam());
		assertThat(memberMakers.getMentoringAvailability()).isEqualTo(request.mentoringAvailability());
		assertThat(memberMakers.getProjectSupplementAvailability()).isEqualTo(request.projectSupplementAvailability());
		assertThat(memberMakers.getSpeakerAvailability()).isEqualTo(request.speakerAvailability());
		assertThat(memberMakers.getCareerLevel()).isEqualTo(request.careerLevel());
		assertThat(memberMakers.getSkills()).isEqualTo(request.skills());
		assertThat(memberMakers.getCompany()).isEqualTo(request.company());
		assertThat(memberMakers.getExpertTopics()).isEqualTo(request.expertTopics());
		assertThat(memberMakers.getActivityCertNumber()).isEqualTo(request.activityCertNumber());
	}

	@Test
	@DisplayName("활동 중인 메이커스팀 구성원이 있으면 추가하지 않는다")
	void 활동_중인_메이커스팀_구성원이_있으면_추가하지_않는다() throws Exception {
		// given
		String email = uniqueEmail("duplicate");
		CreateMemberMakersRequest request = createMemberMakersRequest(email);
		mockMvc.perform(post("/admin/members/makers")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk());

		// when & then
		mockMvc.perform(post("/admin/members/makers")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.status").value(MemberErrorCode.ALREADY_EXIST_ACTIVE_MEMBER_MAKERS_ACTIVITY.getCode()));
	}

	@Test
	@DisplayName("삭제된 기존 구성원도 메이커스팀 구성원으로 복구해 추가한다")
	void 삭제된_기존_구성원도_메이커스팀_구성원으로_복구해_추가한다() throws Exception {
		// given
		String email = uniqueEmail("restore");
		Member deletedMember = memberRepository.save(member()
			.email(email)
			.build());
		memberRepository.delete(deletedMember);
		memberRepository.flush();
		CreateMemberMakersRequest request = createMemberMakersRequest(email);

		// when
		mockMvc.perform(post("/admin/members/makers")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("SUCCESS"));

		// then
		Member restoredMember = memberRepository.findByEmail(email).orElseThrow();
		MemberActivity memberActivity = findOnlyActivity(restoredMember.getId());

		assertThat(restoredMember.getIsDeleted()).isFalse();
		assertThat(restoredMember.getName()).isEqualTo(request.name());
		assertThat(restoredMember.getPhoneNumber()).isEqualTo(request.phoneNumber());
		assertThat(memberActivity.getMemberType()).isEqualTo(MemberType.MAKERS);
		assertThat(memberActivity.getMemberMakers().getMakersTeam()).isEqualTo(request.makersTeam());
	}

	private CreateMemberMakersRequest createMemberMakersRequest(String email) {
		return new CreateMemberMakersRequest(
			"김메이커",
			email,
			"01087654321",
			JobFamily.FE,
			CareerDetails.EMPLOYEE,
			MakersTeam.TEAM_1,
			RecruitTypeDetail.REGULAR,
			Region.SEOUL,
			List.of("HEALTHCARE", "FINTECH", "AI"),
			ExperiencePeriod.ONE_TO_TWO,
			Availability.HIGHLY_AVAILABLE,
			Availability.AVAILABLE_BY_TOPIC,
			Availability.CONSIDER_LATER,
			CareerLevel.JUNIOR,
			"Spring",
			"JECT",
			"백오피스",
			"MK-001",
			"memo"
		);
	}

	private MemberActivity findOnlyActivity(Long memberId) {
		List<MemberActivity> activities = memberActivityRepository.findAll().stream()
			.filter(activity -> activity.getMemberId().equals(memberId))
			.toList();

		assertThat(activities).hasSize(1);
		return activities.get(0);
	}

	private String uniqueEmail(String prefix) {
		return prefix + uniqueSuffix() + "@t.kr";
	}

	private String uniqueSuffix() {
		String suffix = String.valueOf(System.nanoTime());
		return suffix.substring(suffix.length() - 6);
	}
}
