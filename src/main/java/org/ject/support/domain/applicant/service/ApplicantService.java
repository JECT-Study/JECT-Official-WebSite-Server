package org.ject.support.domain.applicant.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.domain.applicant.dto.ApplicantDto;
import org.ject.support.domain.applicant.dto.ApplicantDto.RegisterRequest;
import org.ject.support.domain.applicant.dto.ApplicantDto.UpdatePinRequest;
import org.ject.support.domain.applicant.dto.ApplicantProfileResponse;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.applicant.exception.ApplicantErrorCode;
import org.ject.support.domain.applicant.exception.ApplicantException;
import org.ject.support.domain.applicant.repository.ApplicantRepository;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.exception.SemesterErrorCode;
import org.ject.support.domain.recruit.exception.SemesterException;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.ject.support.domain.applicant.exception.ApplicantErrorCode.ALREADY_EXIST_APPLICANT;

@Service
@RequiredArgsConstructor
public class ApplicantService {

    private final ApplicantRepository applicantRepository;
    private final SemesterRepository semesterRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    /**
     * 인증된 사용자의 PIN 번호를 설정하고 지원자를 생성합니다.
     * 인증번호 검증 -> PIN 번호 설정 -> 지원자 생성 단계에서 호출됩니다.
     * 인증 토큰을 통해 검증된 이메일로 지원자를 조회하고, PIN 번호를 암호화하여 저장합니다.
     */
    @Transactional
    public Authentication registerTempApplicant(RegisterRequest registerRequest, String email) {
        // 이메일로 지원자 조회
        /**
         * Todo: 이메일을 단건으로 구분할 수 있는 정책 설정
         * 기존 Member는 email에 유니크 제약조건 존재, applicant는 이력 의미도 포함하기 때문에 유니크 제약 X
         * 여러 기수에 지원한 경우 현재 로직대로면 복수 행 응답 가능
         * 지원데이터는 모집 공고 당 1개만 존재할 수 있음 -> 이메일 조회를 이메일+공고로 조회
         * applicant에 recruit_id 필요 (값 참조)
         */
        Applicant applicant = applicantRepository.findByEmail(email)
                .orElse(null);

        if (applicant == null) {
            // 새로운 지원자 생성
            applicant = createTempApplicantWithPin(registerRequest, email);
        } else {
            // 기존 지원자가 있는 경우 PIN 번호만 업데이트
            throw new ApplicantException(ALREADY_EXIST_APPLICANT);
        }

        // 인증 및 토큰 발급
        return jwtTokenProvider.createAuthenticationByApplicant(applicant);
    }

    /**
     * PIN 번호가 설정된 지원자를 생성합니다.
     * PIN 번호는 암호화하여 저장합니다.
     */
    private Applicant createTempApplicantWithPin(RegisterRequest registerRequest, String email) {
        String encodedPin = passwordEncoder.encode(registerRequest.pin());

        Semester semester = semesterRepository.findRecruitingSemester()
                .orElseThrow(() -> new SemesterException(SemesterErrorCode.NOT_FOUND_RECRUITING_SEMESTER));

        Applicant applicant = registerRequest.toEntity(semester.getId(), email, encodedPin);

        return applicantRepository.save(applicant);
    }

    /**
     * 지원자의 최초 프로필 정보(이름, 전화번호) 등록
     * 지원자(ROLE_APPLY)가 최초로 이름과 전화번호를 등록할 때 호출됩니다.
     */
    @Transactional
    public void registerInitialProfile(ApplicantDto.InitialProfileRequest request, Long applicantId) {
        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new ApplicantException(ApplicantErrorCode.NOT_FOUND_APPLICANT));

        applicant.updateNameAndPhoneNumber(request.name(), request.phoneNumber());
    }

    @Transactional
    public void updatePin(UpdatePinRequest request, Long applicantId) {
        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new ApplicantException(ApplicantErrorCode.NOT_FOUND_APPLICANT));

        String encodedPin = passwordEncoder.encode(request.pin());
        applicant.updatePin(encodedPin);
    }

    @Transactional(readOnly = true)
    public boolean checkIsInitialed(Long applicantId) {
        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new ApplicantException(ApplicantErrorCode.NOT_FOUND_APPLICANT));

        return applicant.isInitialed();
    }

    @Transactional(readOnly = true)
    public ApplicantProfileResponse getApplicantProfile(Long applicantId) {
        Applicant applicant = applicantRepository.findById(applicantId)
                .orElseThrow(() -> new ApplicantException(ApplicantErrorCode.NOT_FOUND_APPLICANT));
        return ApplicantProfileResponse.of(applicant);
    }
}
