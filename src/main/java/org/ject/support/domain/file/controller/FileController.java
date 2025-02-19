package org.ject.support.domain.file.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.ject.support.domain.file.dto.CreatePresignedUrlResponse;
import org.ject.support.external.s3.S3Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class FileController {

    private final S3Service s3Service;

    // TODO 인증 어노테이션 추가
    @PostMapping("/presigned-url")
    public CreatePresignedUrlResponse createPresignedUrl(final Long memberId,
                                                         @RequestBody final String fileName) {
        return s3Service.createPresignedUrl(memberId, fileName);
    }

    // TODO 인증 어노테이션 추가
    @PostMapping("/presigned-urls")
    public List<CreatePresignedUrlResponse> createPresignedUrls(final Long memberId,
                                                                @RequestBody final List<String> fileNames) {
        return s3Service.createPresignedUrls(memberId, fileNames);
    }
}
