package org.ject.support.admin.member.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ject.support.domain.member.fixture.MemberFixture.member;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ject.support.admin.member.dto.request.CreateMemberSupportersRequest;
import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.entity.MemberActivity;
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
class AdminMemberSupportersControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private MemberActivityRepository memberActivityRepository;

	@Test
	@DisplayName("운영 서포터즈 구성원을 추가한다")
	void 운영_서포터즈_구성원을_추가한다() throws Exception {
		// given
		String email = uniqueEmail("supporter");
		CreateMemberSupportersRequest request = createMemberSupportersRequest(email, JobFamily.OPS, "memo");

		// when
		mockMvc.perform(post("/admin/members/supporters")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("SUCCESS"));

		// then
		Member member = memberRepository.findByEmail(email).orElseThrow();
		MemberActivity memberActivity = findOnlyActivity(member.getId());

		assertThat(member.getName()).isEqualTo(request.name());
		assertThat(member.getPhoneNumber()).isEqualTo(request.phoneNumber());
		assertThat(memberActivity.getMemberType()).isEqualTo(MemberType.SUPPORTERS);
		assertThat(memberActivity.getJobFamily()).isEqualTo(request.jobFamily());
		assertThat(memberActivity.getRecruitTypeDetail()).isEqualTo(request.recruitTypeDetail());
		assertThat(memberActivity.getActivityStatus()).isEqualTo(request.activityStatus());
		assertThat(memberActivity.getStartDate()).isEqualTo(request.startDate());
		assertThat(memberActivity.getEndDate()).isEqualTo(request.endDate());
		assertThat(memberActivity.getMemo()).isEqualTo(request.memo());
		assertThat(memberActivity.getMemberSupporters().getActivityCertNumber()).isEqualTo(request.activityCertNumber());
	}

	@Test
	@DisplayName("운영 서포터즈에 맞지 않는 포지션이면 추가하지 않는다")
	void 운영_서포터즈에_맞지_않는_포지션이면_추가하지_않는다() throws Exception {
		// given
		CreateMemberSupportersRequest request = createMemberSupportersRequest(uniqueEmail("invalid"), JobFamily.FE, "memo");

		// when & then
		mockMvc.perform(post("/admin/members/supporters")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(MemberErrorCode.INVALID_JOB_FAMILY.getCode()));
	}

	@Test
	@DisplayName("활동 중인 운영 서포터즈 구성원이 있으면 추가하지 않는다")
	void 활동_중인_운영_서포터즈_구성원이_있으면_추가하지_않는다() throws Exception {
		// given
		String email = uniqueEmail("duplicate");
		CreateMemberSupportersRequest request = createMemberSupportersRequest(email, JobFamily.BX, "memo");
		CreateMemberSupportersRequest activeRequest = new CreateMemberSupportersRequest(
			request.name(),
			request.phoneNumber(),
			request.email(),
			request.memberType(),
			request.jobFamily(),
			request.recruitTypeDetail(),
			ActivityStatus.ACTIVE,
			request.startDate(),
			request.endDate(),
			request.activityCertNumber(),
			request.memo()
		);

		mockMvc.perform(post("/admin/members/supporters")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(activeRequest)))
			.andExpect(status().isOk());

		// when & then
		mockMvc.perform(post("/admin/members/supporters")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(activeRequest)))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.status")
				.value(MemberErrorCode.ALREADY_EXIST_ACTIVE_MEMBER_SUPPORTERS_ACTIVITY.getCode()));
	}

	@Test
	@DisplayName("운영 서포터즈 구성원 목록을 커서 기반으로 조회한다")
	void 운영_서포터즈_구성원_목록을_커서_기반으로_조회한다() throws Exception {
		// given
		MemberActivity firstActivity = saveSupportersActivity(uniqueEmail("list1"), ActivityStatus.ACTIVE);
		MemberActivity secondActivity = saveSupportersActivity(uniqueEmail("list2"), ActivityStatus.ENDED);
		MemberActivity thirdActivity = saveSupportersActivity(uniqueEmail("list3"), ActivityStatus.ACTIVE);

		mockMvc.perform(get("/admin/members/supporters")
				.param("size", "2"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.content[0].memberActivityId").value(thirdActivity.getId()))
			.andExpect(jsonPath("$.data.content[1].memberActivityId").value(secondActivity.getId()))
			.andExpect(jsonPath("$.data.hasNext").value(true))
			.andExpect(jsonPath("$.data.nextCursor").value(secondActivity.getId()))
			.andExpect(jsonPath("$.data.totalCount").value(3));

		// when & then
		mockMvc.perform(get("/admin/members/supporters")
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
	@DisplayName("유효하지 않은 페이징 값이면 운영 서포터즈 목록을 조회하지 않는다")
	void 유효하지_않은_페이징_값이면_운영_서포터즈_목록을_조회하지_않는다() throws Exception {
		mockMvc.perform(get("/admin/members/supporters").param("size", "0"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value("GLOBAL-15"));

		mockMvc.perform(get("/admin/members/supporters").param("size", "101"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value("GLOBAL-15"));

		mockMvc.perform(get("/admin/members/supporters").param("cursor", "0"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value("GLOBAL-15"));
	}

	@Test
	@DisplayName("운영 서포터즈 구성원의 상세정보를 조회한다")
	void 운영_서포터즈_구성원의_상세정보를_조회한다() throws Exception {
		// given
		MemberActivity memberActivity = saveSupportersActivity(uniqueEmail("detail"), ActivityStatus.ENDED);

		// when & then
		mockMvc.perform(get("/admin/members/supporters/{memberActivityId}", memberActivity.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("SUCCESS"))
			.andExpect(jsonPath("$.data.memberActivityId").value(memberActivity.getId()))
			.andExpect(jsonPath("$.data.name").value("김젝트"))
			.andExpect(jsonPath("$.data.phoneNumber").value("01012345678"))
			.andExpect(jsonPath("$.data.email").exists())
			.andExpect(jsonPath("$.data.memberType").value(MemberType.SUPPORTERS.name()))
			.andExpect(jsonPath("$.data.jobFamily").value(JobFamily.OPS.name()))
			.andExpect(jsonPath("$.data.recruitTypeDetail").value(RecruitTypeDetail.REGULAR.name()))
			.andExpect(jsonPath("$.data.activityStatus").value(ActivityStatus.ENDED.name()))
			.andExpect(jsonPath("$.data.activityCertNumber").value("SP-001"))
			.andExpect(jsonPath("$.data.memo").value("memo"));
	}

	@Test
	@DisplayName("존재하지 않는 운영 서포터즈 구성원을 조회하면 404를 반환한다")
	void 존재하지_않는_운영_서포터즈_구성원을_조회하면_404를_반환한다() throws Exception {
		// when & then
		mockMvc.perform(get("/admin/members/supporters/{memberActivityId}", 999999999L))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(MemberErrorCode.NOT_FOUND_MEMBER.getCode()));
	}

	private CreateMemberSupportersRequest createMemberSupportersRequest(String email, JobFamily jobFamily, String memo) {
		return new CreateMemberSupportersRequest(
			"김서포터",
			"01012345678",
			email,
			MemberType.SUPPORTERS,
			jobFamily,
			RecruitTypeDetail.REGULAR,
			ActivityStatus.ENDED,
			java.time.LocalDate.of(2025, 5, 19),
			java.time.LocalDate.of(2025, 12, 19),
			"SP-001",
			memo
		);
	}

	private MemberActivity findOnlyActivity(Long memberId) {
		java.util.List<MemberActivity> activities = memberActivityRepository.findAll().stream()
			.filter(activity -> activity.getMemberId().equals(memberId))
			.toList();

		assertThat(activities).hasSize(1);
		return activities.get(0);
	}

	private MemberActivity saveSupportersActivity(String email, ActivityStatus activityStatus) {
		Member member = memberRepository.save(member().email(email).build());
		return memberActivityRepository.saveAndFlush(MemberActivity.createSupportersActivity(
			member.getId(),
			JobFamily.OPS,
			RecruitTypeDetail.REGULAR,
			activityStatus,
			null,
			null,
			"SP-001",
			"memo"
		));
	}

	private String uniqueEmail(String prefix) {
		return prefix + uniqueSuffix() + "@t.kr";
	}

	private String uniqueSuffix() {
		String suffix = String.valueOf(System.nanoTime());
		return suffix.substring(suffix.length() - 6);
	}
}
