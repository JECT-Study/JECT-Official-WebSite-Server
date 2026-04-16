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
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SendEmailResponse;

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
    void 단건_본문_이메일_전송_성공_시_SES_Client_호출_검증() {
        given(sesV2Client.sendEmail(any(SendEmailRequest.class)))
                .willReturn(SendEmailResponse.builder().messageId("simple-1").build());

        sesEmailSendService.sendEmail("user@recipient.com", "JECT 안내", "<h1>본문</h1>");

        verify(sesV2Client).sendEmail(any(SendEmailRequest.class));
    }

    @Test
    void 단건_본문_이메일_전송_실패_시_EMAIL_SEND_FAILURE_예외_발생() {
        given(sesV2Client.sendEmail(any(SendEmailRequest.class)))
                .willThrow(new RuntimeException("simple send fail"));

        assertThatThrownBy(() -> sesEmailSendService.sendEmail("user@recipient.com", "JECT 안내", "<h1>본문</h1>"))
                .isInstanceOf(EmailException.class)
                .extracting(e -> ((EmailException) e).getErrorCode())
                .isEqualTo(EmailErrorCode.EMAIL_SEND_FAILURE);
    }
}
