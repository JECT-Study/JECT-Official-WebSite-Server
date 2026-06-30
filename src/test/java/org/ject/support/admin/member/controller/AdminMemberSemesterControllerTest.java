package org.ject.support.admin.member.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.ject.support.domain.member.fixture.MemberFixture.member;
import static org.ject.support.domain.member.fixture.SemesterActivityFixture.semesterActivity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Comparator;
import java.util.List;
import org.ject.support.admin.member.dto.request.CreateMemberSemesterRequest;
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
				.param("jobFamilies", "BE")
				.param("recruitTypeDetails", "REGULAR")
				.param("careerDetails", "EMPLOYEE")
				.param("teamIds", String.valueOf(team.getId())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("SUCCESS"))
			.andExpect(jsonPath("$.data.content", hasSize(1)))
			.andExpect(jsonPath("$.data.content[0].memberActivityId").value(matched.getId()))
			.andExpect(jsonPath("$.data.totalCount").value(1));
	}

	@Test
	@DisplayName("팀 필터만 있으면 일반 구성원 목록을 조회하지 않는다")
	void 팀_필터만_있으면_일반_구성원_목록을_조회하지_않는다() throws Exception {
		// when & then
		mockMvc.perform(get("/admin/members/semester")
				.param("teamIds", "1"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(MemberErrorCode.REQUIRED_SEMESTER_FOR_TEAM_FILTER.getCode()));
	}

	@Test
	@DisplayName("잘못된 enum 값이면 일반 구성원 목록을 조회하지 않는다")
	void 잘못된_enum_값이면_일반_구성원_목록을_조회하지_않는다() throws Exception {
		// when & then
		mockMvc.perform(get("/admin/members/semester")
				.param("jobFamilies", "WRONG"))
			.andExpect(status().isBadRequest());
	}

	private CreateMemberSemesterRequest createMemberSemesterRequest(String email, Long semesterId, Long teamId) {
		return new CreateMemberSemesterRequest(
			"김젝트",
			email,
			"01012345678",
			JobFamily.BE,
			RecruitTypeDetail.REGULAR,
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
