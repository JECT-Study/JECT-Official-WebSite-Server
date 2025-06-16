package org.ject.support.external.email.service;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.external.email.domain.EmailSendGroup;
import org.ject.support.external.email.exception.EmailErrorCode;
import org.ject.support.external.email.exception.EmailException;
import org.ject.support.external.email.repository.EmailSendGroupRepository;
import org.ject.support.external.infrastructure.SesRateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.BulkEmailContent;
import software.amazon.awssdk.services.sesv2.model.BulkEmailEntry;
import software.amazon.awssdk.services.sesv2.model.Destination;
import software.amazon.awssdk.services.sesv2.model.EmailContent;
import software.amazon.awssdk.services.sesv2.model.MessageTag;
import software.amazon.awssdk.services.sesv2.model.SendBulkEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.Template;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SesEmailSendService implements EmailSendService {

    private static final String GROUP_CODE_TAG_NAME = "group_code";

    private final Map2JsonSerializer map2JsonSerializer;
    private final EmailSendGroupRepository emailSendGroupRepository;
    private final SesV2Client sesV2Client;
    private final SesRateLimiter rateLimiter;

    @Value("${aws.ses.from-email-address}")
    private String from;

    @Override
    public void sendTemplatedEmail(String sendGroupCode, String to, Map<String, String> params) {
        // 전송 그룹 정보 조회
        EmailSendGroup sendGroup = getSendGroup(sendGroupCode);

        // 이메일 콘텐츠 구성
        EmailContent emailContent = EmailContent.builder()
                .template(getTemplate(sendGroup.getTemplateName(), params))
                .build();

        // 이메일 그룹 식별용 태그 설정
        MessageTag messageTag = getMessageTag(sendGroup.getCode());

        // 단건 이메일 요청 생성
        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .destination(getDestination(to))
                .content(emailContent)
                .fromEmailAddress(from)
                .emailTags(messageTag)
                .build();

        // 이메일 전송
        sesV2Client.sendEmail(emailRequest);
    }

    @Override
    public void sendBulkTemplatedEmail(String sendGroupCode, List<String> toList, Map<String, String> params) {
        // 전송 그룹 정보 조회
        EmailSendGroup sendGroup = getSendGroup(sendGroupCode);

        // 이메일 콘텐츠 구성
        BulkEmailContent content = BulkEmailContent.builder()
                .template(getTemplate(sendGroup.getTemplateName(), params))
                .build();

        // 이메일 그룹 식별용 태그 설정
        MessageTag messageTag = getMessageTag(sendGroup.getCode());

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

    private EmailSendGroup getSendGroup(String sendGroupCode) {
        return emailSendGroupRepository.findByCode(sendGroupCode)
                .orElseThrow(() -> new EmailException(EmailErrorCode.NOT_FOUND_SEND_GROUP));
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
