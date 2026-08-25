package org.ject.support.admin.member.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.ject.support.domain.member.fixture.MakersActivityFixture.makersActivity;
import static org.ject.support.domain.member.fixture.MemberFixture.member;
import static org.ject.support.domain.member.fixture.SemesterActivityFixture.semesterActivity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.ject.support.admin.member.dto.request.CreateMemberSemesterRequest;
import org.ject.support.admin.member.dto.request.DeleteMembersRequest;
import org.ject.support.domain.member.ActivityStatus;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.MemberType;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.entity.MemberActivity;
import org.ject.support.domain.member.entity.Team;
import org.ject.support.domain.member.exception.MemberErrorCode;
import org.ject.support.domain.member.repository.MemberActivityRepository;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.domain.member.repository.TeamRepository;
import org.ject.support.domain.recruit.domain.RecruitTypeDetail;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.ject.support.testconfig.AuthenticatedUser;
import org.ject.support.testconfig.IntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.cache.support.NullValue;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@AutoConfigureMockMvc
@Transactional
@AuthenticatedUser(isAdmin = true)
@TestPropertySource(properties = {"spring.data.redis.repositories.enabled=false"})
class AdminMemberSemesterControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private MemberActivityRepository memberActivityRepository;

	@Autowired
	private SemesterRepository semesterRepository;

	@Autowired
	private TeamRepository teamRepository;

	@Test
	@DisplayName("일반 구성원을 추가한다")
	void 일반_구성원을_추가한다() throws Exception {
		// given
		Semester semester = saveSemester();
		Team team = saveTeam(semester.getId(), "1팀");
		String email = uniqueEmail("add");
		CreateMemberSemesterRequest request = createMemberSemesterRequest(email, semester.getId(), team.getId());

		// when
		mockMvc.perform(post("/admin/members/semester")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("SUCCESS"));

		// then
		Member member = memberRepository.findByEmail(email).orElseThrow();
		assertThat(member.getName()).isEqualTo(request.name());
		assertThat(member.getPhoneNumber()).isEqualTo(request.phoneNumber());
		assertThat(memberActivityRepository.existsSemesterActivity(
			member.getId(),
			MemberType.SEMESTER,
			semester.getId()
		)).isTrue();
		MemberActivity memberActivity = memberActivityRepository.findAll().stream()
			.filter(activity -> activity.getMemberId().equals(member.getId()))
			.findFirst()
			.orElseThrow();
		assertThat(memberActivity.getActivityStatus()).isEqualTo(request.activityStatus());
	}

	@Test
	@DisplayName("팀을 선택하지 않아도 일반 구성원을 추가한다")
	void 팀을_선택하지_않아도_일반_구성원을_추가한다() throws Exception {
		// given
		Semester semester = saveSemester();
		String email = uniqueEmail("noteam");
		CreateMemberSemesterRequest request = createMemberSemesterRequest(email, semester.getId(), null);

		// when
		mockMvc.perform(post("/admin/members/semester")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("SUCCESS"));

		// then
		Member member = memberRepository.findByEmail(email).orElseThrow();
		MemberActivity memberActivity = memberActivityRepository.findAll().stream()
			.filter(activity -> activity.getMemberId().equals(member.getId()))
			.findFirst()
			.orElseThrow();
		assertThat(memberActivity.getMemberSemester().getSemesterId()).isEqualTo(semester.getId());
		assertThat(memberActivity.getMemberSemester().getTeamId()).isNull();
	}

	@Test
	@DisplayName("필수값이 없으면 일반 구성원을 추가하지 않는다")
	void 필수값이_없으면_일반_구성원을_추가하지_않는다() throws Exception {
		// given
		Semester semester = saveSemester();
		CreateMemberSemesterRequest request = new CreateMemberSemesterRequest(
			null,
			uniqueEmail("invalid"),
			"01012345678",
			JobFamily.BE,
			RecruitTypeDetail.REGULAR,
			ActivityStatus.COMPLETED,
			CareerDetails.EMPLOYEE,
			semester.getId(),
			null,
			ExperiencePeriod.ONE_TO_TWO,
			"memo",
			List.of("HEALTHCARE"),
			Region.SEOUL
		);

		// when & then
		mockMvc.perform(post("/admin/members/semester")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("일반 구성원 목록을 커서 기반으로 조회한다")
	void 일반_구성원_목록을_커서_기반으로_조회한다() throws Exception {
		// given
		Semester semester = saveSemester();
		MemberActivity first = saveMemberSemesterActivity(
			uniqueEmail("list1"),
			semester.getId(),
			null,
			JobFamily.BE,
			RecruitTypeDetail.REGULAR,
			CareerDetails.EMPLOYEE,
			ExperiencePeriod.ONE_TO_TWO
		);
		MemberActivity second = saveMemberSemesterActivity(
			uniqueEmail("list2"),
			semester.getId(),
			null,
			JobFamily.FE,
			RecruitTypeDetail.REGULAR,
			CareerDetails.STUDENT,
			ExperiencePeriod.NONE
		);
		MemberActivity third = saveMemberSemesterActivity(
			uniqueEmail("list3"),
			semester.getId(),
			null,
			JobFamily.PM,
			RecruitTypeDetail.NEW,
			CareerDetails.JOB_SEEKER,
			ExperiencePeriod.THREE_TO_FOUR
		);

		// when & then
		mockMvc.perform(get("/admin/members/semester")
				.param("size", "2"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("SUCCESS"))
			.andExpect(jsonPath("$.data.content", hasSize(2)))
			.andExpect(jsonPath("$.data.content[0].memberActivityId").value(third.getId()))
			.andExpect(jsonPath("$.data.content[1].memberActivityId").value(second.getId()))
			.andExpect(jsonPath("$.data.hasNext").value(true))
			.andExpect(jsonPath("$.data.nextCursor").value(second.getId()))
			.andExpect(jsonPath("$.data.totalCount").value(3));

		mockMvc.perform(get("/admin/members/semester")
				.param("cursor", String.valueOf(second.getId()))
				.param("size", "2"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.content", hasSize(1)))
			.andExpect(jsonPath("$.data.content[0].memberActivityId").value(first.getId()))
			.andExpect(jsonPath("$.data.hasNext").value(false))
			.andExpect(jsonPath("$.data.nextCursor").value(nullValue()))
			.andExpect(jsonPath("$.data.totalCount").value(3));
	}

	@Test
	@DisplayName("필터 조건으로 일반 구성원 목록을 조회한다")
	void 필터_조건으로_일반_구성원_목록을_조회한다() throws Exception {
		// given
		Semester semester = saveSemester();
		Team team = saveTeam(semester.getId(), "필터팀");
		MemberActivity matched = saveMemberSemesterActivity(
			uniqueEmail("match"),
			semester.getId(),
			team.getId(),
			JobFamily.BE,
			RecruitTypeDetail.REGULAR,
			CareerDetails.EMPLOYEE,
			ExperiencePeriod.ONE_TO_TWO
		);
		saveMemberSemesterActivity(
			uniqueEmail("other"),
			semester.getId(),
			team.getId(),
			JobFamily.FE,
			RecruitTypeDetail.REGULAR,
			CareerDetails.EMPLOYEE,
			ExperiencePeriod.ONE_TO_TWO
		);

		// when & then
		mockMvc.perform(get("/admin/members/semester")
				.param("size", "30")
				.param("semesterId", String.valueOf(semester.getId()))
				.param("jobFamily", "BE")
				.param("recruitTypeDetail", "REGULAR")
				.param("careerDetails", "EMPLOYEE")
				.param("teamId", String.valueOf(team.getId()))
				.param("status", ActivityStatus.ACTIVE.name()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("SUCCESS"))
			.andExpect(jsonPath("$.data.content", hasSize(1)))
			.andExpect(jsonPath("$.data.content[0].memberActivityId").value(matched.getId()))
			.andExpect(jsonPath("$.data.content[0].status").value(ActivityStatus.ACTIVE.name()))
			.andExpect(jsonPath("$.data.totalCount").value(1));
	}

	@Test
	@DisplayName("조회 개수가 1개보다 작으면 일반 구성원 목록을 조회하지 않는다")
	void 조회_개수가_1개보다_작으면_일반_구성원_목록을_조회하지_않는다() throws Exception {
		// when & then
		mockMvc.perform(get("/admin/members/semester")
				.param("size", "0"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value("GLOBAL-15"))
			.andExpect(jsonPath("$.data[0]").value("조회 개수는 1개 이상이어야 합니다."));
	}

	@Test
	@DisplayName("조회 개수가 100개보다 크면 일반 구성원 목록을 조회하지 않는다")
	void 조회_개수가_100개보다_크면_일반_구성원_목록을_조회하지_않는다() throws Exception {
		// when & then
		mockMvc.perform(get("/admin/members/semester")
				.param("size", "101"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value("GLOBAL-15"))
			.andExpect(jsonPath("$.data[0]").value("조회 개수는 100개 이하여야 합니다."));
	}

	@Test
	@DisplayName("팀 필터만 있으면 일반 구성원 목록을 조회하지 않는다")
	void 팀_필터만_있으면_일반_구성원_목록을_조회하지_않는다() throws Exception {
		// when & then
		mockMvc.perform(get("/admin/members/semester")
				.param("teamId", "1"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(MemberErrorCode.REQUIRED_SEMESTER_FOR_TEAM_FILTER.getCode()));
	}

	@Test
	@DisplayName("잘못된 enum 값이면 일반 구성원 목록을 조회하지 않는다")
	void 잘못된_enum_값이면_일반_구성원_목록을_조회하지_않는다() throws Exception {
		// when & then
		mockMvc.perform(get("/admin/members/semester")
				.param("jobFamily", "WRONG"))
			.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("일반 구성원에서 사용할 수 없는 활동 상태면 목록을 조회하지 않는다")
	void 일반_구성원에서_사용할_수_없는_활동_상태면_목록을_조회하지_않는다() throws Exception {
		// when & then
		mockMvc.perform(get("/admin/members/semester")
				.param("status", ActivityStatus.DROPOUT.name()))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(MemberErrorCode.INVALID_ACTIVITY_STATUS.getCode()));
	}

	@Test
	@DisplayName("일반 구성원을 삭제한다")
	void 일반_구성원을_삭제한다() throws Exception {
		// given
		Semester semester = saveSemester();
		MemberActivity memberActivity = saveMemberSemesterActivity(
			uniqueEmail("delete"),
			semester.getId(),
			null,
			JobFamily.BE,
			RecruitTypeDetail.REGULAR,
			CareerDetails.EMPLOYEE,
			ExperiencePeriod.ONE_TO_TWO
		);

		// when
		mockMvc.perform(delete("/admin/members/semester/{memberActivityId}", memberActivity.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("SUCCESS"));

		// then
		assertThat(memberActivityRepository.findById(memberActivity.getId())).isEmpty();
		assertThat(memberRepository.findById(memberActivity.getMemberId())).isEmpty();
	}

	@Test
	@DisplayName("선택한 일반 구성원을 모두 삭제한다")
	void 선택한_일반_구성원을_모두_삭제한다() throws Exception {
		// given
		Semester semester = saveSemester();
		MemberActivity first = saveMemberSemesterActivity(
			uniqueEmail("bulk1"),
			semester.getId(),
			null,
			JobFamily.BE,
			RecruitTypeDetail.REGULAR,
			CareerDetails.EMPLOYEE,
			ExperiencePeriod.ONE_TO_TWO
		);
		MemberActivity second = saveMemberSemesterActivity(
			uniqueEmail("bulk2"),
			semester.getId(),
			null,
			JobFamily.FE,
			RecruitTypeDetail.REGULAR,
			CareerDetails.STUDENT,
			ExperiencePeriod.NONE
		);
		DeleteMembersRequest request = new DeleteMembersRequest(Set.of(first.getId(), second.getId()));

		// when
		mockMvc.perform(delete("/admin/members/semester")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("SUCCESS"));

		// then
		assertThat(memberActivityRepository.findAllById(List.of(first.getId(), second.getId()))).isEmpty();
		assertThat(memberRepository.findAllById(List.of(first.getMemberId(), second.getMemberId()))).isEmpty();
	}

	@Test
	@DisplayName("일반 구성원이 아닌 구성원은 삭제할 수 없다")
	void 일반_구성원이_아닌_구성원은_삭제할_수_없다() throws Exception {
		// given
		Member member = memberRepository.save(member().email(uniqueEmail("invalid-type")).build());
		MemberActivity makers = memberActivityRepository.saveAndFlush(makersActivity().memberId(member.getId()).build());

		// when & then
		mockMvc.perform(delete("/admin/members/semester/{memberActivityId}", makers.getId()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(MemberErrorCode.NOT_FOUND_MEMBER_SEMESTER_ACTIVITY.getCode()));
		assertThat(memberActivityRepository.findById(makers.getId())).isPresent();
		assertThat(memberRepository.findById(member.getId())).isPresent();
	}

	@Test
	@DisplayName("삭제할 일반 구성원이 없으면 요청에 실패한다")
	void 삭제할_일반_구성원이_없으면_요청에_실패한다() throws Exception {
		mockMvc.perform(delete("/admin/members/semester")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"memberActivityIds\":[]}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value("GLOBAL-15"));
	}

	private CreateMemberSemesterRequest createMemberSemesterRequest(String email, Long semesterId, Long teamId) {
		return new CreateMemberSemesterRequest(
			"김젝트",
			email,
			"01012345678",
			JobFamily.BE,
			RecruitTypeDetail.REGULAR,
			ActivityStatus.COMPLETED,
			CareerDetails.EMPLOYEE,
			semesterId,
			teamId,
			ExperiencePeriod.ONE_TO_TWO,
			"memo",
			List.of("HEALTHCARE", "FINTECH", "AI"),
			Region.SEOUL
		);
	}

	private Semester saveSemester() {
		return semesterRepository.save(Semester.builder()
			.name("테스트기수" + uniqueSuffix())
			.isRecruiting(true)
			.build());
	}

	private Team saveTeam(Long semesterId, String name) {
		return teamRepository.save(Team.builder()
			.name(name + uniqueSuffix())
			.semesterId(semesterId)
			.build());
	}

	private MemberActivity saveMemberSemesterActivity(
		String email,
		Long semesterId,
		Long teamId,
		JobFamily jobFamily,
		RecruitTypeDetail recruitTypeDetail,
		CareerDetails careerDetails,
		ExperiencePeriod experiencePeriod
	) {
		Member member = memberRepository.save(member()
			.email(email)
			.build());

		return memberActivityRepository.saveAndFlush(semesterActivity()
			.memberId(member.getId())
			.semesterId(semesterId)
			.teamId(teamId)
			.jobFamily(jobFamily)
			.recruitTypeDetail(recruitTypeDetail)
			.careerDetails(careerDetails)
			.experiencePeriod(experiencePeriod)
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
