package org.ject.support.domain.file.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ject.support.common.security.AuthPrincipal;
import org.ject.support.common.springdoc.ApiErrorResponse;
import org.ject.support.common.springdoc.ApiErrorResponses;
import org.ject.support.domain.file.dto.UploadFileRequest;
import org.ject.support.domain.file.dto.UploadFileResponse;
import org.ject.support.domain.file.exception.FileErrorCode;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "File", description = "파일 API")
public interface FileApiSpec {

    @Operation(
            summary = "콘텐츠 업로드",
            description = "프로젝트 썸네일, 소개서 등 콘텐츠를 업로드합니다."
    )
    @ApiErrorResponses(responses = {
            @ApiErrorResponse(value = FileErrorCode.class, code = 400, name = "INVALID_EXTENSION"),
            @ApiErrorResponse(value = FileErrorCode.class, code = 400, name = "EXCEEDED_PORTFOLIO_MAX_SIZE")
    })
    List<UploadFileResponse> uploadContents(@AuthPrincipal final Long memberId,
                                            @RequestBody final List<UploadFileRequest> requests);

    @Operation(
            summary = "포트폴리오 업로드",
            description = "지원자의 포트폴리오를 업로드합니다."
    )
    @ApiErrorResponses(responses = {
            @ApiErrorResponse(value = FileErrorCode.class, code = 400, name = "INVALID_EXTENSION"),
            @ApiErrorResponse(value = FileErrorCode.class, code = 400, name = "EXCEEDED_PORTFOLIO_MAX_SIZE")
    })
    List<UploadFileResponse> uploadPortfolios(@AuthPrincipal final Long memberId,
                                              @RequestBody final List<UploadFileRequest> requests);
}
