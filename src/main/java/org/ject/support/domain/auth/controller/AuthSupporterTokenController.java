package org.ject.support.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.ject.support.domain.auth.dto.AuthDto;
import org.ject.support.domain.auth.service.AuthSupporterTokenService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Tag(name = "Supporter", description = "서포터 API")
public class AuthSupporterTokenController {

    private final AuthSupporterTokenService authSupporterTokenService;

    @Operation(
            summary = "서포터 토큰 발급",
            description = "서포터 전용 토큰을 발급합니다."
    )
    @PostMapping("/supporter/token")
    @PreAuthorize("permitAll()")
    public void generateSupporterToken(@RequestBody @Valid AuthDto.PinLoginRequest request) {
        authSupporterTokenService.issueReadOnlyToken(request.email(), request.pin());
    }
}
