package org.ject.support.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.ject.support.common.springdoc.ApiErrorResponse;
import org.ject.support.common.springdoc.ApiErrorResponses;
import org.ject.support.domain.auth.dto.AuthDto;
import org.ject.support.domain.auth.exception.AuthErrorCode;
import org.ject.support.domain.member.exception.MemberErrorCode;
import org.ject.support.external.email.domain.EmailTemplate;
import org.ject.support.external.email.exception.EmailErrorCode;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Auth", description = "인증 API")
public interface AuthApiSpec {

    @Operation(
            summary = "인증번호 검증",
            description = "인증번호 검증만 수행하고 임시 토큰을 발급합니다."
    )
    @Parameters({
            @Parameter(
                    name = "template", description = "이메일 템플릿",
                    schema = @Schema(allowableValues = {"AUTH_CODE", "PIN_RESET"}),
                    examples = {
                            @ExampleObject(name = "AUTH_CODE", summary = "이메일 인증 코드 안내", value = "AUTH_CODE"),
                            @ExampleObject(name = "PIN_RESET", summary = "PIN 재설정 인증 코드 안내", value = "PIN_RESET")
                    },
                    required = true)
    })
    @ApiResponse(
            headers = {
                    @Header(name = "refreshToken", description = "프레시레시 토큰"),
                    @Header(name = "accessToken", description = "액세스 토큰"),
                    @Header(name = "verificationToken", description = "검증 토큰")
            }
    )
    @ApiErrorResponses(responses = {
            @ApiErrorResponse(value = AuthErrorCode.class, code = 400, name = "INVALID_AUTH_CODE"),
            @ApiErrorResponse(value = AuthErrorCode.class, code = 404, name = "NOT_FOUND_AUTH_CODE"),
            @ApiErrorResponse(value = EmailErrorCode.class, code = 400, name = "INVALID_EMAIL_TEMPLATE")
    })
    boolean verifyAuthCode(@RequestBody AuthDto.VerifyAuthCodeRequest verifyAuthCodeRequest,
                           HttpServletRequest request, HttpServletResponse response,
                           @RequestParam EmailTemplate template);

    @Operation(
            summary = "리프레시 토큰을 이용한 액세스 토큰 재발급",
            description = "리프레시 토큰이 유효한 경우 새로운 액세스 토큰을 발급합니다."
    )
    @ApiErrorResponses(responses = {
            @ApiErrorResponse(value = AuthErrorCode.class, code = 400, name = "INVALID_REFRESH_TOKEN"),
            @ApiErrorResponse(value = AuthErrorCode.class, code = 400, name = "EXPIRED_REFRESH_TOKEN"),
            @ApiErrorResponse(value = AuthErrorCode.class, code = 400, name = "INVALID_REFRESH_TOKEN")
    })
    boolean refreshToken(HttpServletRequest request, HttpServletResponse response);

    @Operation(
            summary = "PIN 로그인",
            description = "이메일과 PIN 번호로 로그인하고 액세스 토큰과 리프레시 토큰을 발급합니다.")
    @ApiErrorResponses(responses = {
            @ApiErrorResponse(value = MemberErrorCode.class, code = 404, name = "NOT_FOUND_MEMBER"),
            @ApiErrorResponse(value = AuthErrorCode.class, code = 400, name = "INVALID_CREDENTIALS")
    })
    boolean loginWithPin(@RequestBody @Valid AuthDto.PinLoginRequest request,
                         HttpServletRequest httpRequest, HttpServletResponse response);

    @Operation(
            summary = "이메일 가입 여부 확인",
            description = "입력한 이메일이 이미 가입된 사용자인지 여부를 확인합니다.")
    boolean isExistMember(@RequestParam String email);
}
