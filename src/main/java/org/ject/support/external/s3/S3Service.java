package org.ject.support.external.s3;

import com.google.common.net.MediaType;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.util.PeriodAccessible;
import org.ject.support.domain.file.dto.Constants;
import org.ject.support.domain.file.dto.UploadFileRequest;
import org.ject.support.domain.file.dto.UploadFileResponse;
import org.ject.support.domain.file.exception.FileErrorCode;
import org.ject.support.domain.file.exception.FileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private static final int EXPIRE_MINUTES = 10;

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.cloudfront.domain}")
    private String cloudfrontDomain;

    /**
     * 지원자가 첨부한 포트폴리오 파일 이름과 해당 지원자의 식별자를 토대로 Pre-signed URL 생성
     */
    @PeriodAccessible(permitAllJob = true)
    public List<UploadFileResponse> uploadPortfolios(Long memberId, List<UploadFileRequest> requests) {
        validatePortfolioExtension(requests);
        validatePortfolioSize(requests);
        return createPresignedUrls(memberId, requests);
    }

    /**
     * CORE 이상의 권한을 가진 사용자가 첨부한 파일 이름과 해당 사용자의 식별자를 토대로 Pre-signed URL 생성
     */
    public List<UploadFileResponse> uploadContents(Long memberId, List<UploadFileRequest> requests) {
        return createPresignedUrls(memberId, requests);
    }

    private void validatePortfolioExtension(List<UploadFileRequest> requests) {
        if (requests.stream().anyMatch(request -> !isTypePdf(request.contentType()))) {
            throw new FileException(FileErrorCode.INVALID_EXTENSION);
        }
    }

    private boolean isTypePdf(String contentType) {
        return MediaType.parse(contentType).is(MediaType.PDF);
    }

    private void validatePortfolioSize(List<UploadFileRequest> requests) {
        long totalSize = requests.stream()
                .mapToLong(UploadFileRequest::contentLength)
                .sum();
        if (totalSize > Constants.PORTFOLIO_MAX_SIZE) {
            throw new FileException(FileErrorCode.EXCEEDED_PORTFOLIO_MAX_SIZE);
        }
    }

    private List<UploadFileResponse> createPresignedUrls(Long memberId, List<UploadFileRequest> requests) {
        return requests.stream()
                .map(request -> {
                    String keyName = getKeyName(memberId, request.name());
                    PutObjectPresignRequest presignRequest =
                            getPutObjectPresignRequest(keyName, request.contentType(), request.contentLength());
                    PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
                    return getUploadFileResponse(cloudfrontDomain + keyName, presignedRequest);
                })
                .toList();
    }

    private String getKeyName(Long memberId, String fileName) {
        String uniqueFileName = String.format("%s_%s", fileName, UUID.randomUUID());
        return String.format("%s/%s", memberId, uniqueFileName);
    }

    private PutObjectPresignRequest getPutObjectPresignRequest(String keyName, String contentType, long contentLength) {
        return PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(EXPIRE_MINUTES))
                .putObjectRequest(PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(keyName)
                        .contentType(contentType)
                        .contentLength(contentLength)
                        .build())
                .build();
    }

    private UploadFileResponse getUploadFileResponse(String cdnUrl, PresignedPutObjectRequest presignedRequest) {
        return UploadFileResponse.builder()
                .cdnUrl(cdnUrl)
                .presignedUrl(presignedRequest.url().toExternalForm())
                .expiration(LocalDateTime.ofInstant(presignedRequest.expiration(), ZoneId.systemDefault()))
                .build();
    }
}
