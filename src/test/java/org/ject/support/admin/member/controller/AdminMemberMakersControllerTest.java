package org.ject.support.admin.member.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.ject.support.domain.member.fixture.MakersActivityFixture.makersActivity;
import static org.ject.support.domain.member.fixture.MemberFixture.member;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
		assertThat(memberActivity.getActivityStatus()).isEqualTo(request.activityStatus());
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

	@Test
	@DisplayName("메이커스팀 구성원 목록을 조회한다")
	void 메이커스팀_구성원_목록을_조회한다() throws Exception {
		// given
		MemberActivity firstActivity = saveMakersActivity(uniqueEmail("list1"), JobFamily.FE, MakersTeam.TEAM_1);
		MemberActivity secondActivity = saveMakersActivity(uniqueEmail("list2"), JobFamily.BE, MakersTeam.TEAM_2);

		// when & then
		mockMvc.perform(get("/admin/members/makers")
				.param("size", "30"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("SUCCESS"))
			.andExpect(jsonPath("$.data.content").isArray())
			.andExpect(jsonPath("$.data.content[0].memberActivityId").value(secondActivity.getId()))
			.andExpect(jsonPath("$.data.content[0].jobFamily").value(JobFamily.BE.name()))
			.andExpect(jsonPath("$.data.content[0].makersTeam").value(MakersTeam.TEAM_2.name()))
			.andExpect(jsonPath("$.data.content[1].memberActivityId").value(firstActivity.getId()))
			.andExpect(jsonPath("$.data.size").value(30))
			.andExpect(jsonPath("$.data.hasNext").value(false))
			.andExpect(jsonPath("$.data.nextCursor").value(nullValue()))
			.andExpect(jsonPath("$.data.totalCount").value(2));
	}

	@Test
	@DisplayName("cursor 이후 메이커스팀 구성원 목록을 조회한다")
	void cursor_이후_메이커스팀_구성원_목록을_조회한다() throws Exception {
		// given
		MemberActivity firstActivity = saveMakersActivity(uniqueEmail("cursor1"), JobFamily.FE, MakersTeam.TEAM_1);
		MemberActivity secondActivity = saveMakersActivity(uniqueEmail("cursor2"), JobFamily.BE, MakersTeam.TEAM_1);
		MemberActivity thirdActivity = saveMakersActivity(uniqueEmail("cursor3"), JobFamily.PM, MakersTeam.TEAM_2);

		mockMvc.perform(get("/admin/members/makers")
				.param("size", "2"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.content[0].memberActivityId").value(thirdActivity.getId()))
			.andExpect(jsonPath("$.data.content[1].memberActivityId").value(secondActivity.getId()))
			.andExpect(jsonPath("$.data.hasNext").value(true))
			.andExpect(jsonPath("$.data.nextCursor").value(secondActivity.getId()))
			.andExpect(jsonPath("$.data.totalCount").value(3));

		// when & then
		mockMvc.perform(get("/admin/members/makers")
				.param("cursor", String.valueOf(secondActivity.getId()))
				.param("size", "2"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("SUCCESS"))
			.andExpect(jsonPath("$.data.content[0].memberActivityId").value(firstActivity.getId()))
			.andExpect(jsonPath("$.data.hasNext").value(false))
			.andExpect(jsonPath("$.data.nextCursor").value(nullValue()))
			.andExpect(jsonPath("$.data.totalCount").value(3));
	}

	@Test
	@DisplayName("size가 1보다 작으면 메이커스팀 구성원 목록을 조회하지 않는다")
	void size가_1보다_작으면_메이커스팀_구성원_목록을_조회하지_않는다() throws Exception {
		// when & then
		mockMvc.perform(get("/admin/members/makers")
				.param("size", "0"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value("GLOBAL-15"));
	}

	@Test
	@DisplayName("size가 100보다 크면 메이커스팀 구성원 목록을 조회하지 않는다")
	void size가_100보다_크면_메이커스팀_구성원_목록을_조회하지_않는다() throws Exception {
		// when & then
		mockMvc.perform(get("/admin/members/makers")
				.param("size", "101"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value("GLOBAL-15"));
	}

	@Test
	@DisplayName("cursor가 1보다 작으면 메이커스팀 구성원 목록을 조회하지 않는다")
	void cursor가_1보다_작으면_메이커스팀_구성원_목록을_조회하지_않는다() throws Exception {
		// when & then
		mockMvc.perform(get("/admin/members/makers")
				.param("cursor", "0")
				.param("size", "2"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value("GLOBAL-15"));
	}

	@Test
	@DisplayName("메이커스팀 구성원 상세를 조회한다")
	void 메이커스팀_구성원_상세를_조회한다() throws Exception {
		// given
		MemberActivity memberActivity = saveMakersActivity(uniqueEmail("detail"), JobFamily.FE, MakersTeam.TEAM_1);

		// when & then
		mockMvc.perform(get("/admin/members/makers/{memberActivityId}", memberActivity.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("SUCCESS"))
			.andExpect(jsonPath("$.data.memberActivityId").value(memberActivity.getId()))
			.andExpect(jsonPath("$.data.name").value("김젝트"))
			.andExpect(jsonPath("$.data.email").exists())
			.andExpect(jsonPath("$.data.jobFamily").value(JobFamily.FE.name()))
			.andExpect(jsonPath("$.data.makersTeam").value(MakersTeam.TEAM_1.name()))
			.andExpect(jsonPath("$.data.activityStatus").value(ActivityStatus.ACTIVE.name()));
	}

	@Test
	@DisplayName("존재하지 않는 메이커스팀 구성원 상세를 조회하지 않는다")
	void 존재하지_않는_메이커스팀_구성원_상세를_조회하지_않는다() throws Exception {
		// when & then
		mockMvc.perform(get("/admin/members/makers/{memberActivityId}", 999999999L))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(MemberErrorCode.NOT_FOUND_MEMBER.getCode()));
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
			ActivityStatus.ACTIVE,
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

	private MemberActivity saveMakersActivity(String email, JobFamily jobFamily, MakersTeam makersTeam) {
		Member member = memberRepository.save(member()
			.email(email)
			.build());

		return memberActivityRepository.saveAndFlush(makersActivity()
			.memberId(member.getId())
			.jobFamily(jobFamily)
			.makersTeam(makersTeam)
			.build());
	}

	private String uniqueEmail(String prefix) {
		return prefix + uniqueSuffix() + "@t.kr";
	}

	private String uniqueSuffix() {
		String suffix = String.valueOf(System.nanoTime());
		return suffix.substring(suffix.length() - 6);
	}
}
