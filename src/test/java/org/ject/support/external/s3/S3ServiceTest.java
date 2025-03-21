package org.ject.support.external.s3;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.ject.support.domain.file.dto.UploadFileRequest;
import org.ject.support.domain.file.dto.UploadFileResponse;
import org.ject.support.domain.file.exception.FileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    private static final String CDN_DOMAIN = "https://test-ject-cdn.net";
    private static final int EXPIRE_MINUTES = 10;

    @Mock
    PresignedPutObjectRequest presignedPutObjectRequest;

    Instant expirationTime;
    Long memberId;
    List<UploadFileRequest> requests;
    List<String> expectedPortfolioUploadUrls;

    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        requests = List.of(
                new UploadFileRequest("portfolio1.pdf", "application/pdf", 12345),
                new UploadFileRequest("portfolio2.pdf", "application/pdf", 12345));
        expirationTime = Instant.now().plusSeconds(60 * EXPIRE_MINUTES);
        memberId = 1L;
        expectedPortfolioUploadUrls =
                List.of(createExpectedUrl(memberId, "portfolio1.pdf"), createExpectedUrl(memberId, "portfolio2.pdf"));
    }

    @Test
    @DisplayName("포트폴리오 업로드를 위한 pre-signed url 생성")
    void upload_portfolio() throws MalformedURLException {
        // given
        when(presignedPutObjectRequest.url()).thenReturn(URI.create(expectedPortfolioUploadUrls.get(0)).toURL());
        when(presignedPutObjectRequest.expiration()).thenReturn(expirationTime);
        when(s3Presigner.presignPutObject((PutObjectPresignRequest) any())).thenReturn(presignedPutObjectRequest);

        // when
        List<UploadFileResponse> result = s3Service.uploadPortfolios(1L, requests);

        // then
        assertThat(result).hasSize(2);

        UploadFileResponse firstResponse = result.get(0);
        assertThat(firstResponse.cdnUrl()).contains(requests.get(0).name());
        assertThat(firstResponse.presignedUrl()).isEqualTo(expectedPortfolioUploadUrls.get(0));
        assertThat(firstResponse.expiration())
                .isEqualTo(LocalDateTime.ofInstant(expirationTime, ZoneId.systemDefault()));

        UploadFileResponse secondResponse = result.get(1);
        assertThat(secondResponse.cdnUrl()).contains(requests.get(1).name());
    }

    @Test
    @DisplayName("객체 키 생성")
    void create_key_name() throws MalformedURLException {
        // given
        when(presignedPutObjectRequest.url()).thenReturn(URI.create(expectedPortfolioUploadUrls.get(0)).toURL());
        when(presignedPutObjectRequest.expiration()).thenReturn(expirationTime);
        when(s3Presigner.presignPutObject((PutObjectPresignRequest) any())).thenReturn(presignedPutObjectRequest);

        // when
        List<UploadFileResponse> result =
                s3Service.uploadPortfolios(memberId, requests);

        // then
        assertThat(result).hasSize(2);

        for (int i = 0; i < 2; i++) {
            UploadFileResponse firstResponse = result.get(i);
            assertThat(firstResponse.cdnUrl()).contains(memberId.toString());
            assertThat(firstResponse.cdnUrl()).contains("_");
        }
    }

    @Test
    @DisplayName("유효하지 않은 확장자로 인한 포트폴리오 업로드 실패")
    void upload_portfolio_fail() {
        // given
        List<UploadFileRequest> invalidRequests = List.of(new UploadFileRequest("test.png", "image/png", 12345));

        // when, then
        assertThatThrownBy(() -> s3Service.uploadPortfolios(memberId, invalidRequests))
                .isInstanceOf(FileException.class);
    }

    private String createExpectedUrl(Long memberId, String fileName) {
        return String.format("%s/%d/%s", CDN_DOMAIN, memberId, fileName);
    }
}