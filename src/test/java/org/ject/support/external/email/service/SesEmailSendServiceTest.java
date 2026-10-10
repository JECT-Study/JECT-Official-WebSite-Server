package org.ject.support.external.email.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Map;
import java.util.stream.Stream;
import org.ject.support.base.UnitTestSupport;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.external.email.domain.EmailTemplate;
import org.ject.support.external.email.exception.EmailErrorCode;
import org.ject.support.external.email.exception.EmailException;
import org.ject.support.external.infrastructure.SesRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.AccountSuspendedException;
import software.amazon.awssdk.services.sesv2.model.BadRequestException;
import software.amazon.awssdk.services.sesv2.model.LimitExceededException;
import software.amazon.awssdk.services.sesv2.model.MailFromDomainNotVerifiedException;
import software.amazon.awssdk.services.sesv2.model.MessageRejectedException;
import software.amazon.awssdk.services.sesv2.model.NotFoundException;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SendEmailResponse;
import software.amazon.awssdk.services.sesv2.model.SendingPausedException;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;
import software.amazon.awssdk.services.sesv2.model.TooManyRequestsException;

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
    @DisplayName("단건 본문 이메일의 결과를 확인할 수 없으면 불확실 예외가 발생한다")
    void 단건_본문_이메일의_결과를_확인할_수_없으면_불확실_예외가_발생한다() {
        given(sesV2Client.sendEmail(any(SendEmailRequest.class)))
                .willThrow(new RuntimeException("simple send fail"));

        assertThatThrownBy(() -> sesEmailSendService.sendEmail("user@recipient.com", "JECT 안내", "<h1>본문</h1>"))
                .isInstanceOf(EmailException.class)
                .extracting(e -> ((EmailException) e).getErrorCode().getCode())
                .isEqualTo("EMAIL_SEND_RESULT_UNKNOWN");
    }

    @ParameterizedTest
    @MethodSource("confirmedRejections")
    void SES가_명확히_거부한_요청은_확정_실패로_구분한다(SesV2Exception rejection) {
        // given
        given(sesV2Client.sendEmail(any(SendEmailRequest.class))).willThrow(rejection);

        // when, then
        assertThatThrownBy(() -> sesEmailSendService.sendEmail("user@recipient.com", "제목", "본문"))
                .isInstanceOf(EmailException.class)
                .extracting(e -> ((EmailException) e).getErrorCode())
                .isEqualTo(EmailErrorCode.EMAIL_SEND_FAILURE);
    }

    private static Stream<SesV2Exception> confirmedRejections() {
        return Stream.of(
                BadRequestException.builder().statusCode(400).build(),
                AccountSuspendedException.builder().statusCode(400).build(),
                LimitExceededException.builder().statusCode(400).build(),
                MailFromDomainNotVerifiedException.builder().statusCode(400).build(),
                MessageRejectedException.builder().statusCode(400).build(),
                NotFoundException.builder().statusCode(404).build(),
                SendingPausedException.builder().statusCode(400).build());
    }

    @Test
    void SES_호출_제한은_다른_확정_실패와_구분한다() {
        // given
        given(sesV2Client.sendEmail(any(SendEmailRequest.class)))
                .willThrow(TooManyRequestsException.builder().statusCode(429).build());

        // when, then
        assertThatThrownBy(() -> sesEmailSendService.sendEmail("user@recipient.com", "제목", "본문"))
                .isInstanceOf(EmailException.class)
                .extracting(e -> ((EmailException) e).getErrorCode())
                .isEqualTo(EmailErrorCode.TOO_MANY_EMAIL_REQUESTS);
    }

    @Test
    void 외부_호출_전_처리_실패는_발송하지_않은_확정_실패로_구분한다() {
        // given
        doThrow(new RuntimeException("local wait interrupted")).when(rateLimiter).consume(1);

        // when, then
        assertThatThrownBy(() -> sesEmailSendService.sendEmail("user@recipient.com", "제목", "본문"))
                .isInstanceOf(EmailException.class)
                .extracting(e -> ((EmailException) e).getErrorCode())
                .isEqualTo(EmailErrorCode.EMAIL_SEND_FAILURE);
        verifyNoInteractions(sesV2Client);
    }

    @ParameterizedTest
    @MethodSource("uncertainOutcomes")
    void 네트워크_오류와_문서화되지_않은_응답은_발송_여부를_확정하지_않는다(RuntimeException exception) {
        // given
        given(sesV2Client.sendEmail(any(SendEmailRequest.class))).willThrow(exception);

        // when, then
        assertThatThrownBy(() -> sesEmailSendService.sendEmail("user@recipient.com", "제목", "본문"))
                .isInstanceOf(EmailException.class)
                .extracting(e -> ((EmailException) e).getErrorCode())
                .isEqualTo(EmailErrorCode.EMAIL_SEND_RESULT_UNKNOWN);
    }

    private static Stream<RuntimeException> uncertainOutcomes() {
        return Stream.of(
                SdkClientException.create("network response lost"),
                SesV2Exception.builder().statusCode(500).build(),
                SesV2Exception.builder().statusCode(403).build(),
                MessageRejectedException.builder().statusCode(500).build(),
                TooManyRequestsException.builder().statusCode(500).build());
    }
}
