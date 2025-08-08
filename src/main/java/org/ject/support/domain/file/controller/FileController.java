package org.ject.support.domain.file.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.security.AuthPrincipal;
import org.ject.support.domain.file.dto.UploadFileRequest;
import org.ject.support.domain.file.dto.UploadFileResponse;
import org.ject.support.external.s3.S3Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class FileController implements FileApi {

    private final S3Service s3Service;

    @Override
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/contents")
    public List<UploadFileResponse> uploadContents(@AuthPrincipal final Long memberId,
                                                   @RequestBody final List<UploadFileRequest> requests) {
        return s3Service.uploadContents(memberId, requests);
    }

    @Override
    @PreAuthorize("hasRole('ROLE_TEMP')")
    @PostMapping("/portfolios")
    public List<UploadFileResponse> uploadPortfolios(@AuthPrincipal final Long memberId,
                                                     @RequestBody final List<UploadFileRequest> requests) {
        return s3Service.uploadPortfolios(memberId, requests);
    }
}
