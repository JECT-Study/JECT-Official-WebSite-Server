package org.ject.support.domain.auth;


import static org.ject.support.domain.auth.AuthErrorCode.INVALID_AUTH_CODE;
import static org.ject.support.domain.auth.AuthErrorCode.NOT_FOUND_AUTH_CODE;

import lombok.RequiredArgsConstructor;
import org.ject.support.common.security.jwt.JwtTokenProvider;
import org.ject.support.domain.auth.AuthDto.VerifyAuthCodeOnlyResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 인증번호 검증만 수행하고 임시 토큰을 발급합니다.
     * 이메일 인증 -> 인증번호 입력 단계에서 호출됩니다.
     * 인증번호 검증이 성공하면 임시 토큰을 발급하여 반환합니다.
     * 이 토큰은 사용자 정보를 포함하지 않고, 인증번호 검증이 완료되었음을 증명하는 용도로만 사용됩니다.
     * 
     * @param email 인증할 이메일 주소
     * @param userInputCode 사용자가 입력한 인증번호
     * @return 임시 인증 토큰이 포함된 응답 객체
     */
    public VerifyAuthCodeOnlyResponse verifyEmailByAuthCodeOnly(String email, String userInputCode) {
        // 인증번호 검증
        verifyAuthCode(email, userInputCode);
        
        // 임시 토큰 발급 - 인증번호 검증이 완료되었음을 증명하는 토큰
        String verificationToken = jwtTokenProvider.createVerificationToken(email);
        
        // 임시 토큰 반환
        return new VerifyAuthCodeOnlyResponse(verificationToken);
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

        // 인증 성공
        redisTemplate.delete(email);
    }
}
