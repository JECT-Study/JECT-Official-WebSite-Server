package org.ject.support.domain.admin.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.domain.admin.service.MemberManagementService;
import org.ject.support.domain.member.JobFamily;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.member.Role;
import org.ject.support.domain.admin.dto.MemberBulkDeleteRequest;
import org.ject.support.domain.admin.dto.MemberDetailResponse;
import org.ject.support.domain.admin.dto.MemberRegisterRequest;
import org.ject.support.domain.admin.dto.MemberResponse;
import org.ject.support.domain.admin.dto.MemberEditRequest;
import org.ject.support.domain.member.exception.MemberErrorCode;
import org.ject.support.domain.member.exception.MemberException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class MemberManagementControllerTest extends UnitTestSupport {

    @InjectMocks
    private MemberManagementController memberManagementController;

    @Mock
    private MemberManagementService memberManagementService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private final String TEST_NAME = "홍길동";
    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_PHONE_NUMBER = "01012345678";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(memberManagementController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new org.ject.support.common.exception.GlobalExceptionHandler())
                .build();
    }

    @Test
    void 회원_목록_조회_성공() throws Exception {
        // given
        var pageable = PageRequest.of(0, 15);
        var memberList = List.of(
                MemberResponse.builder()
                        .id(1L)
                        .name(TEST_NAME)
                        .phoneNumber(TEST_PHONE_NUMBER)
                        .email(TEST_EMAIL)
                        .jobFamily(JobFamily.BE)
                        .semesterName("1기")
                        .build()
        );
        var mockPage = new PageImpl<>(memberList, pageable, 1);

        given(memberManagementService.findMembers(any(Role.class), any(), any(), any(Pageable.class)))
                .willReturn(mockPage);

        // expected
        mockMvc.perform(get("/admin/members")
                        .param("role", "SEMESTER")
                        .param("page", "0")
                        .param("size", "15")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value(TEST_NAME))
                .andExpect(jsonPath("$.content[0].email").value(TEST_EMAIL))
                .andDo(print());

        verify(memberManagementService).findMembers(eq(Role.SEMESTER), eq(null), eq(null), any(Pageable.class));
    }

    @Test
    void 회원_목록_조회_성공_직군_필터_적용() throws Exception {
        // given
        var pageable = PageRequest.of(0, 15);
        var memberList = List.of(
                MemberResponse.builder()
                        .id(1L)
                        .name(TEST_NAME)
                        .phoneNumber(TEST_PHONE_NUMBER)
                        .email(TEST_EMAIL)
                        .jobFamily(JobFamily.BE)
                        .semesterName("1기")
                        .build()
        );
        var mockPage = new PageImpl<>(memberList, pageable, 1);

        given(memberManagementService.findMembers(any(Role.class), any(JobFamily.class), any(), any(Pageable.class)))
                .willReturn(mockPage);

        // expected
        mockMvc.perform(get("/admin/members")
                        .param("role", "SEMESTER")
                        .param("jobFamily", "BE")
                        .param("page", "0")
                        .param("size", "15")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].jobFamily").value("BE"))
                .andDo(print());

        verify(memberManagementService).findMembers(eq(Role.SEMESTER), eq(JobFamily.BE), eq(null), any(Pageable.class));
    }

    @Test
    void 회원_목록_조회_성공_기수_필터_적용() throws Exception {
        // given
        var pageable = PageRequest.of(0, 15);
        var semesterId = 1L;
        var memberList = List.of(
                MemberResponse.builder()
                        .id(1L)
                        .name(TEST_NAME)
                        .phoneNumber(TEST_PHONE_NUMBER)
                        .email(TEST_EMAIL)
                        .jobFamily(JobFamily.BE)
                        .semesterName("1")
                        .build()
        );
        var mockPage = new PageImpl<>(memberList, pageable, 1);

        given(memberManagementService.findMembers(any(Role.class), any(), eq(semesterId), any(Pageable.class)))
                .willReturn(mockPage);

        // expected
        mockMvc.perform(get("/admin/members")
                        .param("role", "SEMESTER")
                        .param("semesterId", "1")
                        .param("page", "0")
                        .param("size", "15")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].semesterName").value("1"))
                .andDo(print());

        verify(memberManagementService).findMembers(eq(Role.SEMESTER), eq(null), eq(semesterId), any(Pageable.class));
    }

    @Test
    void 회원_상세_조회_성공() throws Exception {
        // given
        var memberId = 1L;
        var response = MemberDetailResponse.builder()
                .id(memberId)
                .role(Role.SEMESTER)
                .name(TEST_NAME)
                .phoneNumber(TEST_PHONE_NUMBER)
                .email(TEST_EMAIL)
                .jobFamily(JobFamily.BE)
                .semesterName("1")
                .build();

        given(memberManagementService.findMemberDetail(memberId)).willReturn(response);

        // expected
        mockMvc.perform(get("/admin/members/{memberId}", memberId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(memberId))
                .andExpect(jsonPath("$.name").value(TEST_NAME))
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.role").value(Role.SEMESTER.name()))
                .andExpect(jsonPath("$.jobFamily").value(JobFamily.BE.name()))
                .andExpect(jsonPath("$.semesterName").value("1"))
                .andDo(print());

        verify(memberManagementService).findMemberDetail(memberId);
    }

    @Test
    void 회원_상세_조회_실패_존재하지_않는_회원() throws Exception {
        // given
        var nonExistentMemberId = 999L;

        given(memberManagementService.findMemberDetail(nonExistentMemberId))
                .willThrow(new MemberException(MemberErrorCode.NOT_FOUND_MEMBER));

        // expected
        mockMvc.perform(get("/admin/members/{memberId}", nonExistentMemberId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpectAll(
                        jsonPath("$.code").value(MemberErrorCode.NOT_FOUND_MEMBER.getCode()),
                        jsonPath("$.messages").value(MemberErrorCode.NOT_FOUND_MEMBER.getMessage()),
                        jsonPath("$.status").value(MemberErrorCode.NOT_FOUND_MEMBER.getHttpStatus().name())
                )
                .andDo(print());

        verify(memberManagementService).findMemberDetail(nonExistentMemberId);
    }

    @Test
    void 관리자용_회원_등록_성공() throws Exception {
        // given
        var request = new MemberRegisterRequest(
                Role.SEMESTER,
                TEST_NAME,
                TEST_PHONE_NUMBER,
                TEST_EMAIL,
                JobFamily.BE,
                Region.SEOUL,
                "1기"
        );

        doNothing().when(memberManagementService).registerMember(any(MemberRegisterRequest.class));

        // expected
        mockMvc.perform(post("/admin/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print());

        verify(memberManagementService).registerMember(any(MemberRegisterRequest.class));
    }

    @Test
    void 관리자용_회원_등록_실패_유효하지_않은_전화번호() throws Exception {
        // given
        var request = new MemberRegisterRequest(
                Role.SEMESTER,
                TEST_NAME,
                "123456789", // 유효하지 않은 전화번호
                TEST_EMAIL,
                JobFamily.BE,
                Region.SEOUL,
                "1"
        );

        // expected
        mockMvc.perform(post("/admin/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 관리자용_회원_등록_실패_유효하지_않은_이메일() throws Exception {
        // given
        var request = new MemberRegisterRequest(
                Role.SEMESTER,
                TEST_NAME,
                TEST_PHONE_NUMBER,
                "invalid-email", // 유효하지 않은 이메일
                JobFamily.BE,
                Region.SEOUL,
                "1"
        );

        // expected
        mockMvc.perform(post("/admin/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 관리자용_회원_등록_실패_유효하지_않은_이름() throws Exception {
        // given
        var request = new MemberRegisterRequest(
                Role.SEMESTER,
                "InvalidName123", // 유효하지 않은 이름 (영문+숫자)
                TEST_PHONE_NUMBER,
                TEST_EMAIL,
                JobFamily.BE,
                Region.SEOUL,
                "1"
        );

        // expected
        mockMvc.perform(post("/admin/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 관리자용_회원_등록_실패_이미_존재하는_회원() throws Exception {
        // given
        var request = new MemberRegisterRequest(
                Role.SEMESTER,
                TEST_NAME,
                TEST_PHONE_NUMBER,
                TEST_EMAIL,
                JobFamily.BE,
                Region.SEOUL,
                "1기"
        );

        doThrow(new MemberException(MemberErrorCode.ALREADY_EXIST_MEMBER))
                .when(memberManagementService).registerMember(any(MemberRegisterRequest.class));

        // expected
        mockMvc.perform(post("/admin/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpectAll(
                        jsonPath("$.code").value(MemberErrorCode.ALREADY_EXIST_MEMBER.getCode()),
                        jsonPath("$.messages").value(MemberErrorCode.ALREADY_EXIST_MEMBER.getMessage()),
                        jsonPath("$.status").value(MemberErrorCode.ALREADY_EXIST_MEMBER.getHttpStatus().name())
                )
                .andDo(print());

        verify(memberManagementService).registerMember(any(MemberRegisterRequest.class));
    }

    @Test
    void 회원_정보_수정_성공() throws Exception {
        // given
        var memberId = 1L;
        var request = MemberEditRequest.builder()
                .role(Role.SEMESTER)
                .name("수정된이름")
                .phoneNumber("01087654321")
                .email("updated@example.com")
                .jobFamily(JobFamily.FE)
                .region(Region.SEOUL)
                .semesterName("1기")
                .build();

        doNothing().when(memberManagementService).editMember(eq(memberId), any(MemberEditRequest.class));

        // expected
        mockMvc.perform(put("/admin/members/{memberId}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print());

        verify(memberManagementService).editMember(eq(memberId), any(MemberEditRequest.class));
    }

    @Test
    void 회원_정보_수정_실패_존재하지_않는_회원() throws Exception {
        // given
        var nonExistentMemberId = 999L;
        var request = MemberEditRequest.builder()
                .role(Role.SEMESTER)
                .name("수정된이름")
                .phoneNumber("01087654321")
                .email("updated@example.com")
                .jobFamily(JobFamily.FE)
                .region(Region.SEOUL)
                .semesterName("1기")
                .build();

        doThrow(new MemberException(MemberErrorCode.NOT_FOUND_MEMBER))
                .when(memberManagementService).editMember(eq(nonExistentMemberId), any(MemberEditRequest.class));

        // expected
        mockMvc.perform(put("/admin/members/{memberId}", nonExistentMemberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpectAll(
                        jsonPath("$.code").value(MemberErrorCode.NOT_FOUND_MEMBER.getCode()),
                        jsonPath("$.messages").value(MemberErrorCode.NOT_FOUND_MEMBER.getMessage()),
                        jsonPath("$.status").value(MemberErrorCode.NOT_FOUND_MEMBER.getHttpStatus().name())
                )
                .andDo(print());

        verify(memberManagementService).editMember(eq(nonExistentMemberId), any(MemberEditRequest.class));
    }

    @Test
    void 회원_정보_수정_실패_유효하지_않은_데이터() throws Exception {
        // given
        var memberId = 1L;
        var request = MemberEditRequest.builder()
                .role(Role.SEMESTER)
                .name("") // 빈 이름
                .phoneNumber("invalid-phone") // 유효하지 않은 전화번호
                .email("invalid-email") // 유효하지 않은 이메일
                .jobFamily(JobFamily.FE)
                .semesterName("1기")
                .build();

        // expected
        mockMvc.perform(put("/admin/members/{memberId}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void 단일_회원_삭제_성공() throws Exception {
        // given
        var memberId = 1L;

        doNothing().when(memberManagementService).deleteMember(memberId);

        // expected
        mockMvc.perform(delete("/admin/members/{memberId}", memberId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        verify(memberManagementService).deleteMember(memberId);
    }

    @Test
    void 단일_회원_삭제_실패_존재하지_않는_회원() throws Exception {
        // given
        var nonExistentMemberId = 999L;

        doThrow(new MemberException(MemberErrorCode.NOT_FOUND_MEMBER))
                .when(memberManagementService).deleteMember(nonExistentMemberId);

        // expected
        mockMvc.perform(delete("/admin/members/{memberId}", nonExistentMemberId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpectAll(
                        jsonPath("$.code").value(MemberErrorCode.NOT_FOUND_MEMBER.getCode()),
                        jsonPath("$.messages").value(MemberErrorCode.NOT_FOUND_MEMBER.getMessage()),
                        jsonPath("$.status").value(MemberErrorCode.NOT_FOUND_MEMBER.getHttpStatus().name())
                )
                .andDo(print());

        verify(memberManagementService).deleteMember(nonExistentMemberId);
    }

    @Test
    void 다중_회원_삭제_성공() throws Exception {
        // given
        List<Long> memberIds = List.of(1L, 2L, 3L);
        MemberBulkDeleteRequest request = new MemberBulkDeleteRequest(memberIds);

        given(memberManagementService.deleteMembers(memberIds)).willReturn(3);

        // expected
        mockMvc.perform(delete("/admin/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(print());

        verify(memberManagementService).deleteMembers(memberIds);
    }

    @Test
    void 다중_회원_삭제_실패_빈_리스트() throws Exception {
        // given
        List<Long> emptyMemberIds = List.of();
        var request = new MemberBulkDeleteRequest(emptyMemberIds);

        // expected
        mockMvc.perform(delete("/admin/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 다중_회원_삭제_실패_null_리스트() throws Exception {
        // given
        var request = new MemberBulkDeleteRequest(null);

        // expected
        mockMvc.perform(delete("/admin/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 다중_회원_삭제_실패_일부_회원이_존재하지_않음() throws Exception {
        // given
        var memberIds = List.of(1L, 999L, 3L); // 999L은 존재하지 않는 회원
        var request = new MemberBulkDeleteRequest(memberIds);

        doThrow(new MemberException(MemberErrorCode.NOT_FOUND_MEMBER))
                .when(memberManagementService).deleteMembers(memberIds);

        // expected
        mockMvc.perform(delete("/admin/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpectAll(
                        jsonPath("$.code").value(MemberErrorCode.NOT_FOUND_MEMBER.getCode()),
                        jsonPath("$.messages").value(MemberErrorCode.NOT_FOUND_MEMBER.getMessage()),
                        jsonPath("$.status").value(MemberErrorCode.NOT_FOUND_MEMBER.getHttpStatus().name())
                )
                .andDo(print());

        verify(memberManagementService).deleteMembers(memberIds);
    }
}
