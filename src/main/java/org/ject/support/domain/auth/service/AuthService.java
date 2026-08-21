package org.ject.support.domain.auth.service;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.domain.applicant.entity.Applicant;
import org.ject.support.domain.applicant.exception.ApplicantException;
import org.ject.support.domain.applicant.repository.ApplicantRepository;
import org.ject.support.domain.auth.dto.AuthVerificationResult;
import org.ject.support.domain.auth.exception.AuthException;
import org.ject.support.domain.recruit.service.SemesterInquiryUsecase;
import org.ject.support.external.email.domain.EmailTemplate;
import org.ject.support.external.email.exception.EmailErrorCode;
import org.ject.support.external.email.exception.EmailException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.ject.support.domain.auth.exception.AuthErrorCode.EXPIRED_REFRESH_TOKEN;
import static org.ject.support.domain.auth.exception.AuthErrorCode.INVALID_AUTH_CODE;
import static org.ject.support.domain.auth.exception.AuthErrorCode.INVALID_CREDENTIALS;
import static org.ject.support.domain.auth.exception.AuthErrorCode.INVALID_REFRESH_TOKEN;
import static org.ject.support.domain.auth.exception.AuthErrorCode.NOT_FOUND_AUTH_CODE;
import static org.ject.support.domain.applicant.exception.ApplicantErrorCode.NOT_FOUND_APPLICANT;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    private final ApplicantRepository applicantRepository;
    private final PasswordEncoder passwordEncoder;
    private final SemesterInquiryUsecase semesterInquiryUsecase;

    /**
     * 이메일 템플릿 타입에 따라 인증번호를 검증하고 적절한 결과를 반환합니다.
     *
     * @param email 인증할 이메일 주소
     * @param authCode 사용자가 입력한 인증번호
     * @param template 이메일 템플릿 타입
     * @return 템플릿 타입에 따른 결과 객체 (Authentication 또는 String)
     */
    @Transactional
    public AuthVerificationResult verifyAuthCodeByTemplate(
            String email, String authCode, Long recruitId, EmailTemplate template) {
        if (template == EmailTemplate.AUTH_CODE) {
            // 인증번호 검증 후 이메일 반환
            verifyAuthCode(email, authCode);
            deleteAuthCode(email);
            return new AuthVerificationResult(email);
        } else if (template == EmailTemplate.PIN_RESET) {
            // 인증번호 검증 후 Authentication 객체 반환
            Authentication authentication = verifyEmailByAuthCodeOnly(email, authCode, recruitId);
            return new AuthVerificationResult(authentication);
        }

        throw new EmailException(EmailErrorCode.INVALID_EMAIL_TEMPLATE);
    }
    /**
     * 이메일 인증번호를 검증하고 인증된 사용자의 Authentication 객체를 반환합니다.
     *
     * @param email 인증할 이메일 주소
     * @param userInputCode 사용자가 입력한 인증번호
     * @return 인증된 사용자의 Authentication 객체
     */

    private Authentication verifyEmailByAuthCodeOnly(String email, String userInputCode, Long recruitId) {
        // 인증번호 검증
        verifyAuthCode(email, userInputCode);
        Applicant applicant = findApplicant(email, recruitId);

        Authentication authentication = jwtTokenProvider.createAuthenticationByApplicant(applicant);
        deleteAuthCode(email);

        return authentication;
    }
    
    /**
     * PIN 번호를 사용하여 로그인합니다.
     * 이메일과 PIN 번호를 검증하고, 성공 시 액세스 토큰과 리프레시 토큰을 발급합니다.
     * 
     * @param email 사용자 이메일
     * @param pin 사용자 PIN 번호
     */
    @Transactional
    public Authentication loginWithPin(String email, String pin, Long recruitId) {
        // 이메일로 회원 조회
        Applicant applicant = findApplicant(email, recruitId);
        
        // PIN 번호 검증
        if (!passwordEncoder.matches(pin, applicant.getPin())) {
            throw new AuthException(INVALID_CREDENTIALS);
        }
        
        // 인증 및 토큰 발급
        return jwtTokenProvider.createAuthenticationByApplicant(applicant);
    }

    private Applicant findApplicant(String email, Long recruitId) {
        Long semesterId = semesterInquiryUsecase.getSemesterIdByRecruitId(recruitId);
        return applicantRepository.findByEmailAndSemesterId(email, semesterId)
                .orElseThrow(() -> new ApplicantException(NOT_FOUND_APPLICANT));
    }

    private void verifyAuthCode(String email, String userInputCode) {
        // redis에서 키 값으로 인증 번호 조회
        String redisCode = redisTemplate.opsForValue().get(email);

        // Redis에서 코드가 없는 경우
        if (redisCode == null) {
            throw new AuthException(NOT_FOUND_AUTH_CODE);
        }

        // 코드 불일치
        if (!userInputCode.equals(redisCode)) {
            throw new AuthException(INVALID_AUTH_CODE);
        }
    }

    private void deleteAuthCode(String email) {
        redisTemplate.delete(email);
    }

    /**
     * 리프레시 토큰을 검증하고 새로운 액세스 토큰을 발급합니다.
     *
     * @param refreshToken 리프레시 토큰
     * @return 새로 발급된 액세스 토큰이 포함된 응답 객체
     * @throws AuthException 리프레시 토큰이 유효하지 않거나 만료된 경우
     */
    @Transactional
    public Long refreshAccessToken(String refreshToken) {
        try {
            // 리프레시 토큰 유효성 검증
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                throw new AuthException(INVALID_REFRESH_TOKEN);
            }

            return jwtTokenProvider.extractApplicantId(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new AuthException(EXPIRED_REFRESH_TOKEN);
        } catch (JwtException e) {
            throw new AuthException(INVALID_REFRESH_TOKEN);
        }
    }

    public boolean isExistMember(String email, Long recruitId) {
        Long semesterId = semesterInquiryUsecase.getSemesterIdByRecruitId(recruitId);
        return applicantRepository.existsByEmailAndSemesterId(email, semesterId);
    }
}
