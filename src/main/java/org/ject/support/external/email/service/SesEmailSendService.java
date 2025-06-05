package org.ject.support.external.email.service;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.external.email.domain.EmailSendGroup;
import org.ject.support.external.email.exception.EmailErrorCode;
import org.ject.support.external.email.exception.EmailException;
import org.ject.support.external.email.repository.EmailSendGroupRepository;
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

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SesEmailSendService implements EmailSendService {

    private static final int MAX_BULK_EMAIL_RECIPIENTS = 50;
    private static final String GROUP_CODE_TAG_NAME = "group_code";

    private final SesV2Client sesV2Client;
    private final Map2JsonSerializer map2JsonSerializer;
    private final EmailSendGroupRepository emailSendGroupRepository;

    @Value("${aws.ses.from-email-address}")
    private String from;

    @Override
    public void sendTemplatedEmail(String sendGroupCode, String to, Map<String, String> params) {
        // 이메일 전송 그룹 조회
        EmailSendGroup sendGroup = getSendGroup(sendGroupCode);

        // 사용할 템플릿 선택 및 변수 매핑
        Template template = getTemplate(sendGroup.getTemplateName(), params);

        // 수신자(destination) 설정
        Destination destination = getDestination(to);

        // 이메일 콘텐츠 설정
        EmailContent emailContent = EmailContent.builder()
                .template(template)
                .build();

        // 이메일 발송 요청 객체 생성
        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .destination(destination)
                .content(emailContent)
                .fromEmailAddress(from)
                .emailTags(getMessageTag(sendGroup.getCode()))
                .build();

        // 이메일 발송
        sesV2Client.sendEmail(emailRequest);
    }

    @Override
    public void sendBulkTemplatedEmail(String sendGroupCode, List<String> toList, Map<String, String> params) {
        // 이메일 전송 그룹 조회
        EmailSendGroup sendGroup = getSendGroup(sendGroupCode);

        // 사용할 템플릿 선택 및 변수 매핑
        Template template = getTemplate(sendGroup.getTemplateName(), params);

        // 전송 가능한 수신자 수로 나눈 후 이메일 발송
        Lists.partition(toList, MAX_BULK_EMAIL_RECIPIENTS).forEach(batch -> {
            List<BulkEmailEntry> entries = batch.stream()
                    .map(to -> BulkEmailEntry.builder()
                            .destination(getDestination(to))
                            .build())
                    .toList();

            // 이메일 발송 요청 객체 생성
            SendBulkEmailRequest sendBulkEmailRequest = SendBulkEmailRequest.builder()
                    .bulkEmailEntries(entries)
                    .defaultContent(BulkEmailContent.builder()
                            .template(template)
                            .build())
                    .fromEmailAddress(from)
                    .defaultEmailTags(getMessageTag(sendGroup.getCode()))
                    .build();

            // 이메일 발송
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
                .value(groupCode)
                .build();
    }
}
