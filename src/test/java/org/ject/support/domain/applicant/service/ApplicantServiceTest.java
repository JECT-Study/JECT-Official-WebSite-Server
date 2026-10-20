package org.ject.support.domain.applicant.service;

import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.domain.applicant.dto.ApplicantDto.InitialProfileRequest;
import org.ject.support.domain.applicant.dto.ApplicantDto.RegisterRequest;
import org.ject.support.domain.applicant.dto.ApplicantDto.UpdatePinRequest;
import org.ject.support.domain.applicant.dto.ApplicantProfileResponse;
import org.ject.support.domain.applicant.dto.DeleteApplicantsRequest;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.applicant.exception.ApplicantErrorCode;
import org.ject.support.domain.applicant.exception.ApplicantException;
import org.ject.support.domain.applicant.repository.ApplicantRepository;
import org.ject.support.domain.member.CareerDetails;
import org.ject.support.domain.member.ExperiencePeriod;
import org.ject.support.domain.member.MemberStatus;
import org.ject.support.domain.member.Region;
import org.ject.support.domain.recruit.service.SemesterInquiryUsecase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

class ApplicantServiceTest extends UnitTestSupport {

    @InjectMocks
    private ApplicantService applicantService;

    @Mock
    private ApplicantRepository applicantRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Authentication authentication;

    @Mock
    private SemesterInquiryUsecase semesterInquiryUsecase;

    private final String TEST_NAME = "홍길동";
    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_PHONE_NUMBER = "01012345678";
    private final String TEST_PIN = "123456";
    private final String TEST_ENCODED_PIN = "encoded_pin";
    private final Long TEST_RECRUIT_ID = 22L;
    private final Long TEST_SEMESTER_ID = 5L;

    @BeforeEach
    void setUp() {
        // 기본 설정
    }

    @Test
    @DisplayName("지원자를 삭제한다")
    void 지원자를_삭제한다() {
        // given
        Long applicantId = 1L;
        Applicant applicant = Applicant.builder().id(applicantId).build();
        given(applicantRepository.findById(applicantId)).willReturn(Optional.of(applicant));

        // when
        applicantService.deleteApplicant(applicantId);

        // then
        verify(applicantRepository).delete(applicant);
    }

    @Test
    @DisplayName("존재하지 않는 지원자를 삭제하면 예외가 발생한다")
    void 존재하지_않는_지원자를_삭제하면_예외가_발생한다() {
        // given
        Long applicantId = 1L;
        given(applicantRepository.findById(applicantId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> applicantService.deleteApplicant(applicantId))
                .isInstanceOf(ApplicantException.class)
                .extracting(exception -> ((ApplicantException) exception).getErrorCode())
                .isEqualTo(ApplicantErrorCode.NOT_FOUND_APPLICANT);
        verify(applicantRepository, never()).delete(any(Applicant.class));
    }

    @Test
    @DisplayName("여러 지원자를 삭제한다")
    void 여러_지원자를_삭제한다() {
        // given
        Set<Long> applicantIds = Set.of(1L, 2L);
        List<Applicant> applicants = applicantIds.stream()
                .map(id -> Applicant.builder().id(id).build())
                .toList();
        given(applicantRepository.findAllById(applicantIds)).willReturn(applicants);

        // when
        applicantService.deleteApplicants(new DeleteApplicantsRequest(applicantIds));

        // then
        verify(applicantRepository).deleteAll(applicants);
    }

    @Test
    @DisplayName("다건 삭제 대상에 존재하지 않는 지원자가 포함되면 예외가 발생한다")
    void 다건_삭제_대상에_존재하지_않는_지원자가_포함되면_예외가_발생한다() {
        // given
        Set<Long> applicantIds = Set.of(1L, 2L);
        given(applicantRepository.findAllById(applicantIds))
                .willReturn(List.of(Applicant.builder().id(1L).build()));

        // when & then
        assertThatThrownBy(() -> applicantService.deleteApplicants(new DeleteApplicantsRequest(applicantIds)))
                .isInstanceOf(ApplicantException.class)
                .extracting(exception -> ((ApplicantException) exception).getErrorCode())
                .isEqualTo(ApplicantErrorCode.NOT_FOUND_APPLICANT);
        verify(applicantRepository, never()).deleteAll(any());
    }

    @Test
    void 임시_지원자_등록_성공() {
        // given
        RegisterRequest request = new RegisterRequest(TEST_PIN, TEST_RECRUIT_ID);
        Applicant applicant = Applicant.builder()
                .id(1L)
                .email(TEST_EMAIL)
                .pin(TEST_ENCODED_PIN)
                .status(MemberStatus.ACTIVE)
                .build();
        given(semesterInquiryUsecase.getSemesterIdByRecruitId(TEST_RECRUIT_ID))
                .willReturn(TEST_SEMESTER_ID);
        given(applicantRepository.findByEmailAndSemesterId(TEST_EMAIL, TEST_SEMESTER_ID))
                .willReturn(Optional.empty());
        given(passwordEncoder.encode(TEST_PIN)).willReturn(TEST_ENCODED_PIN);
        given(applicantRepository.save(any(Applicant.class))).willReturn(applicant);
        given(jwtTokenProvider.createAuthenticationByApplicant(any(Applicant.class))).willReturn(authentication);

        // when
        Authentication result = applicantService.registerTempApplicant(request, TEST_EMAIL);

        // then
        assertThat(result).isEqualTo(authentication);
        verify(applicantRepository).save(any(Applicant.class));
        verify(passwordEncoder).encode(TEST_PIN);
        verify(jwtTokenProvider).createAuthenticationByApplicant(any(Applicant.class));
    }

    @Test
    void 이미_존재하는_지원자인_경우_예외_발생() {
        // given
        RegisterRequest request = new RegisterRequest(TEST_PIN, TEST_RECRUIT_ID);
        Applicant existingApplicant = Applicant.builder()
                .email(TEST_EMAIL)
                .pin(TEST_ENCODED_PIN)
                .status(MemberStatus.ACTIVE)
                .build();

        given(semesterInquiryUsecase.getSemesterIdByRecruitId(TEST_RECRUIT_ID))
                .willReturn(TEST_SEMESTER_ID);
        given(applicantRepository.findByEmailAndSemesterId(TEST_EMAIL, TEST_SEMESTER_ID))
                .willReturn(Optional.of(existingApplicant));

        // when & then
        assertThatThrownBy(() -> applicantService.registerTempApplicant(request, TEST_EMAIL))
                .isInstanceOf(ApplicantException.class)
                .extracting(e -> ((ApplicantException) e).getErrorCode())
                .isEqualTo(ApplicantErrorCode.ALREADY_EXIST_APPLICANT);
    }

    @Test
    void 지원자_정보_업데이트_성공() {
        // given
        Long applicantId = 1L;
        InitialProfileRequest request = new InitialProfileRequest(TEST_NAME, TEST_PHONE_NUMBER);
        Applicant applicant = Applicant.builder()
                .id(applicantId)
                .email(TEST_EMAIL)
                .pin(TEST_ENCODED_PIN)
                .status(MemberStatus.ACTIVE)
                .build();

        given(applicantRepository.findById(applicantId)).willReturn(Optional.of(applicant));

        // when
        applicantService.registerInitialProfile(request, applicantId);

        // then
        assertThat(applicant.getName()).isEqualTo(TEST_NAME);
        assertThat(applicant.getPhoneNumber()).isEqualTo(TEST_PHONE_NUMBER);
        verify(applicantRepository).findById(applicantId);
    }

    @Test
    void 존재하지_않는_지원자_정보_업데이트_시_예외_발생() {
        // given
        Long applicantId = 1L;
        InitialProfileRequest request = new InitialProfileRequest(TEST_NAME, TEST_PHONE_NUMBER);

        given(applicantRepository.findById(applicantId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> applicantService.registerInitialProfile(request, applicantId))
                .isInstanceOf(ApplicantException.class)
                .extracting(e -> ((ApplicantException) e).getErrorCode())
                .isEqualTo(ApplicantErrorCode.NOT_FOUND_APPLICANT);
    }

    @Test
    void 핀번호_재설정_성공() {
        // given
        Long applicantId = 1L;
        String newPin = "654321";
        UpdatePinRequest request = new UpdatePinRequest(newPin);

        Applicant applicant = Applicant.builder()
                .id(applicantId)
                .email(TEST_EMAIL)
                .pin(TEST_ENCODED_PIN)
                .status(MemberStatus.ACTIVE)
                .build();

        String newEncodedPin = "new_encoded_pin";

        given(applicantRepository.findById(applicantId)).willReturn(Optional.of(applicant));
        given(passwordEncoder.encode(newPin)).willReturn(newEncodedPin);

        // when
        applicantService.updatePin(request, applicantId);

        // then
        assertThat(applicant.getPin()).isEqualTo(newEncodedPin);
        verify(applicantRepository).findById(applicantId);
        verify(passwordEncoder).encode(newPin);
    }

    @Test
    void 핀번호_재설정_실패_존재하지_않는_지원자() {
        // given
        Long applicantId = 1L;
        UpdatePinRequest request = new UpdatePinRequest("654321");

        given(applicantRepository.findById(applicantId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> applicantService.updatePin(request, applicantId))
                .isInstanceOf(ApplicantException.class)
                .extracting(e -> ((ApplicantException) e).getErrorCode())
                .isEqualTo(ApplicantErrorCode.NOT_FOUND_APPLICANT);
    }

    @Test
    void 존재하지않는_지원자ID로_지원자_프로필_정보를_조회할_경우_NOT_FOUND_APPLICANT_예외_발생() {
        // given
        Long applicantId = 1L;
        given(applicantRepository.findById(applicantId))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> applicantService.getApplicantProfile(applicantId))
                .isInstanceOf(ApplicantException.class)
                .extracting(e -> ((ApplicantException) e).getErrorCode())
                .isEqualTo(ApplicantErrorCode.NOT_FOUND_APPLICANT);
    }

    @Test
    void 지원자ID로_지원자_프로필_정보를_조회할_경우_지원자정보를_반환한다() {
        // given
        Long applicantId = 1L;
        String name = "프로필";
        String phoneNumber = "01012345678";
        CareerDetails careerDetails = CareerDetails.EMPLOYEE;
        Region region = Region.BUSAN;
        ExperiencePeriod experiencePeriod = ExperiencePeriod.FIVE_PLUS;
        List<String> interestedDomains = List.of("BACKEND", "FRONTEND");

        Applicant applicant = Applicant.builder()
                .id(applicantId)
                .name(name)
                .phoneNumber(phoneNumber)
                .careerDetails(careerDetails)
                .region(region)
                .experiencePeriod(experiencePeriod)
                .interestedDomains(interestedDomains)
                .build();

        given(applicantRepository.findById(applicantId))
                .willReturn(Optional.of(applicant));

        // when
        ApplicantProfileResponse result = applicantService.getApplicantProfile(applicantId);

        // then
        assertThat(result.id()).isEqualTo(applicantId);
        assertThat(result.name()).isEqualTo(name);
        assertThat(result.careerDetails()).isEqualTo(careerDetails);
        assertThat(result.region()).isEqualTo(region);
        assertThat(result.experiencePeriod()).isEqualTo(experiencePeriod);
        assertThat(result.interestedDomains()).isEqualTo(interestedDomains);
    }
}
