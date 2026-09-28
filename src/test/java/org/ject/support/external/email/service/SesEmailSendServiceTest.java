package org.ject.support.external.email.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Map;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.external.email.domain.EmailTemplate;
import org.ject.support.external.email.exception.EmailErrorCode;
import org.ject.support.external.email.exception.EmailException;
import org.ject.support.external.infrastructure.SesRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SendEmailResponse;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkServiceException;

class SesEmailSendServiceTest extends UnitTestSupport {

    @InjectMocks
    private SesEmailSendService sesEmailSendService;

    @Mock
    private Map2JsonSerializer map2JsonSerializer;

    @Mock
    private SesV2Client sesV2Client;

    @Mock
    private SesRateLimiter rateLimiter;

    private static final String MOCK_FROM_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(sesEmailSendService, "from", MOCK_FROM_EMAIL);
    }

    @Test
    @DisplayName("이메일 전송에 실패할 경우 EMAIL_SEND_FAILURE 예외가 발생한다")
    void 이메일_전송에_실패할_경우_EMAIL_SEND_FAILURE_예외_발생() {
        // given
        String to = "user@recipient.com";
        String subject = "Test Subject";
        String htmlBody = "<h1>Test Body</h1>";

        SendEmailResponse mockResponse = SendEmailResponse.builder()
                .messageId("mock-message-id-123")
                .build();
        given(map2JsonSerializer.serializeAsString(anyMap())).willReturn("{\"key\":\"value\"}");
        given(sesV2Client.sendEmail(any(SendEmailRequest.class)))
                .willThrow(new RuntimeException("Simulated SES send failure"));

        // when, then
        assertThatThrownBy(() -> sesEmailSendService.sendTemplatedEmail(                EmailTemplate.AUTH_CODE,
                        to,
                        Map.of("subject", subject, "htmlBody", htmlBody)
                ))
                .isInstanceOf(EmailException.class)
                .extracting(e -> ((EmailException) e).getErrorCode())
                .isEqualTo(EmailErrorCode.EMAIL_SEND_FAILURE);
    }

    @Test
    @DisplayName("이메일 전송 성공 시 SES Client를 호출한다")
    void 이메일_전송_성공_시_SES_Client_호출_검증() {
        // given
        String to = "user@recipient.com";
        String subject = "Test Subject";
        String htmlBody = "<h1>Test Body</h1>";

        SendEmailResponse mockResponse = SendEmailResponse.builder()
                .messageId("mock-message-id-123")
                .build();
        given(map2JsonSerializer.serializeAsString(anyMap())).willReturn("{\"key\":\"value\"}");
        given(sesV2Client.sendEmail(any(SendEmailRequest.class))).willReturn(mockResponse);

        // when
        sesEmailSendService.sendTemplatedEmail(
                EmailTemplate.AUTH_CODE,
                to,
                Map.of("subject", subject, "htmlBody", htmlBody)
        );

        // then
        verify(map2JsonSerializer).serializeAsString(anyMap());
        verify(sesV2Client).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    @DisplayName("단건 본문 이메일 전송 성공 시 SES Client를 호출한다")
    void 단건_본문_이메일_전송_성공_시_SES_Client_호출_검증() {
        given(sesV2Client.sendEmail(any(SendEmailRequest.class)))
                .willReturn(SendEmailResponse.builder().messageId("simple-1").build());

        sesEmailSendService.sendEmail("user@recipient.com", "JECT 안내", "<h1>본문</h1>");

        verify(sesV2Client).sendEmail(any(SendEmailRequest.class));
        verify(rateLimiter).consume(1);
    }

    @Test
    @DisplayName("단건 본문 이메일 전송 실패 시 EMAIL_SEND_FAILURE 예외가 발생한다")
    void 단건_본문_이메일_전송_실패_시_EMAIL_SEND_FAILURE_예외_발생() {
        given(sesV2Client.sendEmail(any(SendEmailRequest.class)))
                .willThrow(new RuntimeException("simple send fail"));

        assertThatThrownBy(() -> sesEmailSendService.sendEmail("user@recipient.com", "JECT 안내", "<h1>본문</h1>"))
                .isInstanceOf(EmailException.class)
                .extracting(e -> ((EmailException) e).getErrorCode())
                .isEqualTo(EmailErrorCode.EMAIL_SEND_FAILURE);
    }

    @Test
    @DisplayName("SES client 오류는 일시적 전송 오류로 분류한다")
    void SES_client_오류는_일시적_전송_오류로_분류한다() {
        given(sesV2Client.sendEmail(any(SendEmailRequest.class)))
                .willThrow(SdkClientException.create("network failure"));

        assertThatThrownBy(() -> sesEmailSendService.sendEmail(
                "user@recipient.com", "JECT 안내", "<h1>본문</h1>"))
                .isInstanceOf(EmailException.class)
                .extracting(e -> ((EmailException) e).getErrorCode())
                .isEqualTo(EmailErrorCode.EMAIL_TRANSIENT_FAILURE);
    }

    @Test
    @DisplayName("SES 5xx 오류는 일시적 전송 오류로 분류한다")
    void SES_5xx_오류는_일시적_전송_오류로_분류한다() {
        given(sesV2Client.sendEmail(any(SendEmailRequest.class)))
                .willThrow(SdkServiceException.builder().statusCode(503).build());

        assertThatThrownBy(() -> sesEmailSendService.sendEmail(
                "user@recipient.com", "JECT 안내", "<h1>본문</h1>"))
                .isInstanceOf(EmailException.class)
                .extracting(e -> ((EmailException) e).getErrorCode())
                .isEqualTo(EmailErrorCode.EMAIL_TRANSIENT_FAILURE);
    }

    @Test
    @DisplayName("SES 4xx 오류는 재시도하지 않는 전송 오류로 분류한다")
    void SES_4xx_오류는_재시도하지_않는_전송_오류로_분류한다() {
        given(sesV2Client.sendEmail(any(SendEmailRequest.class)))
                .willThrow(SdkServiceException.builder().statusCode(400).build());

        assertThatThrownBy(() -> sesEmailSendService.sendEmail(
                "user@recipient.com", "JECT 안내", "<h1>본문</h1>"))
                .isInstanceOf(EmailException.class)
                .extracting(e -> ((EmailException) e).getErrorCode())
                .isEqualTo(EmailErrorCode.EMAIL_SEND_FAILURE);
    }
}
