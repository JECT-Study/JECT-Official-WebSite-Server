package org.ject.support.external.email.service;

import static org.ject.support.external.email.exception.EmailErrorCode.EMAIL_SEND_FAILURE;
import static org.ject.support.external.email.exception.EmailErrorCode.EMAIL_SEND_RESULT_UNKNOWN;
import static org.ject.support.external.email.exception.EmailErrorCode.TOO_MANY_EMAIL_REQUESTS;

import com.google.common.collect.Lists;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.external.email.domain.EmailTemplate;
import org.ject.support.external.email.exception.EmailException;
import org.ject.support.external.infrastructure.SesRateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.AccountSuspendedException;
import software.amazon.awssdk.services.sesv2.model.BadRequestException;
import software.amazon.awssdk.services.sesv2.model.Body;
import software.amazon.awssdk.services.sesv2.model.BulkEmailContent;
import software.amazon.awssdk.services.sesv2.model.BulkEmailEntry;
import software.amazon.awssdk.services.sesv2.model.Content;
import software.amazon.awssdk.services.sesv2.model.Destination;
import software.amazon.awssdk.services.sesv2.model.EmailContent;
import software.amazon.awssdk.services.sesv2.model.LimitExceededException;
import software.amazon.awssdk.services.sesv2.model.MailFromDomainNotVerifiedException;
import software.amazon.awssdk.services.sesv2.model.Message;
import software.amazon.awssdk.services.sesv2.model.MessageRejectedException;
import software.amazon.awssdk.services.sesv2.model.MessageTag;
import software.amazon.awssdk.services.sesv2.model.NotFoundException;
import software.amazon.awssdk.services.sesv2.model.SendBulkEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SendingPausedException;
import software.amazon.awssdk.services.sesv2.model.SesV2Exception;
import software.amazon.awssdk.services.sesv2.model.Template;
import software.amazon.awssdk.services.sesv2.model.TooManyRequestsException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SesEmailSendService implements EmailSendService {

    private static final String GROUP_CODE_TAG_NAME = "group_code";
    private static final Duration SEND_PERMIT_MAX_WAIT = Duration.ofSeconds(10);

    private final Map2JsonSerializer map2JsonSerializer;
    private final SesV2Client sesV2Client;
    private final SesRateLimiter rateLimiter;

    @Value("${aws.ses.from-email-address}")
    private String from;

    @Override
    public void sendTemplatedEmail(EmailTemplate sendGroupCode, String toEmail, Map<String, String> params) {
        // 이메일 콘텐츠 구성
        EmailContent emailContent = EmailContent.builder()
                .template(getTemplate(sendGroupCode.getTemplateName(), params))
                .build();

        // 이메일 그룹 식별용 태그 설정
        MessageTag messageTag = getMessageTag(sendGroupCode.getTag());

        // 단건 이메일 요청 생성
        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .destination(getDestination(toEmail))
                .content(emailContent)
                .fromEmailAddress(from)
                .emailTags(messageTag)
                .build();

        try {
            sesV2Client.sendEmail(emailRequest);
        } catch (Exception e) {
            log.error("이메일 전송 실패 sendGroupCode={}", sendGroupCode.getTemplateName(), e);
            throw new EmailException(EMAIL_SEND_FAILURE);
        }
    }

    @Override
    public void sendBulkTemplatedEmail(EmailTemplate sendGroupCode, List<String> toList, Map<String, String> params) {
        // 이메일 콘텐츠 구성
        BulkEmailContent content = BulkEmailContent.builder()
                .template(getTemplate(sendGroupCode.getTemplateName(), params))
                .build();

        // 이메일 그룹 식별용 태그 설정
        MessageTag messageTag = getMessageTag(sendGroupCode.getTag());

        // 수신자 리스트를 초당 전송량만큼 분할하여 전송
        Lists.partition(toList, rateLimiter.getRateLimitPerSecond())
                .forEach(chunk -> {
                    rateLimiter.consume(chunk.size());

                    List<BulkEmailEntry> entries = chunk.stream()
                            .map(to -> BulkEmailEntry.builder()
                                    .destination(getDestination(to))
                                    .build())
                            .toList();

                    SendBulkEmailRequest sendBulkEmailRequest = SendBulkEmailRequest.builder()
                            .bulkEmailEntries(entries)
                            .defaultContent(content)
                            .fromEmailAddress(from)
                            .defaultEmailTags(messageTag)
                            .build();

                    sesV2Client.sendBulkEmail(sendBulkEmailRequest);
                });
    }

    @Override
    public void sendEmail(String to, String subject, String htmlBody) {
        EmailContent emailContent = EmailContent.builder()
                .simple(Message.builder()
                        .subject(Content.builder().data(subject).charset("UTF-8").build())
                        .body(Body.builder()
                                .html(Content.builder().data(htmlBody).charset("UTF-8").build())
                                .build())
                        .build())
                .build();

        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .destination(getDestination(to))
                .content(emailContent)
                .fromEmailAddress(from)
                .build();

        try {
            rateLimiter.consume(1, SEND_PERMIT_MAX_WAIT);
        } catch (RuntimeException exception) {
            // SES 호출 전 실패는 발송하지 않았다는 근거가 있어 확정 실패로 처리한다.
            log.error("단건 이메일 호출 전 실패 errorType={}", exception.getClass().getSimpleName());
            throw new EmailException(EMAIL_SEND_FAILURE);
        }

        try {
            sesV2Client.sendEmail(emailRequest);
        } catch (Exception e) {
            log.error("단건 이메일 호출 오류 errorType={}", e.getClass().getSimpleName());
            if (e instanceof TooManyRequestsException limited && limited.statusCode() == 429) {
                throw new EmailException(TOO_MANY_EMAIL_REQUESTS);
            }
            if (e instanceof SesV2Exception sesException && isConfirmedRejection(sesException)) {
                throw new EmailException(EMAIL_SEND_FAILURE);
            }
            throw new EmailException(EMAIL_SEND_RESULT_UNKNOWN);
        }
    }

    private boolean isConfirmedRejection(SesV2Exception exception) {
        // 문서화된 거부 응답만 확정 실패로 분류하고 나머지는 보수적으로 격리한다.
        return (exception.statusCode() == 404 && exception instanceof NotFoundException)
                || (exception.statusCode() == 400
                && (exception instanceof BadRequestException
                || exception instanceof AccountSuspendedException
                || exception instanceof LimitExceededException
                || exception instanceof MailFromDomainNotVerifiedException
                || exception instanceof MessageRejectedException
                || exception instanceof SendingPausedException));
    }

    private Template getTemplate(String templateName, Map<String, String> parameter) {
        return Template.builder()
                .templateName(templateName)
                .templateData(map2JsonSerializer.serializeAsString(parameter))
                .build();
    }

    private Destination getDestination(String to) {
        return Destination.builder()
                .toAddresses(to)
                .build();
    }

    private MessageTag getMessageTag(String groupCode) {
        return MessageTag.builder()
                .name(GROUP_CODE_TAG_NAME)
                .value(String.format("%s_%s",
                        groupCode, LocalDate.now().format(DateTimeFormatter.ofPattern("yy.MM.dd"))))
                .build();
    }
}
