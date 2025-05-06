package org.ject.support.external.email.service;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.external.email.domain.EmailTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.Body;
import software.amazon.awssdk.services.sesv2.model.BulkEmailContent;
import software.amazon.awssdk.services.sesv2.model.BulkEmailEntry;
import software.amazon.awssdk.services.sesv2.model.Content;
import software.amazon.awssdk.services.sesv2.model.Destination;
import software.amazon.awssdk.services.sesv2.model.EmailContent;
import software.amazon.awssdk.services.sesv2.model.Message;
import software.amazon.awssdk.services.sesv2.model.SendBulkEmailRequest;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.Template;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailSendService {

    private final SesV2Client sesV2Client;
    private final Map2JsonSerializer map2JsonSerializer;

    @Value("${aws.ses.from-email-address}")
    private String from;

    /**
     * 단건 email 전송 (template)
     */
    public void sendTemplatedEmail(String to, EmailTemplate emailTemplate, Map<String, String> parameter) {
        // 수신자(destination) 설정
        Destination destination = getDestination(to);

        // 사용할 템플릿 선택 및 변수 매핑
        Template template = getTemplate(emailTemplate.getTemplateName(), parameter);

        // 이메일 콘텐츠 설정
        EmailContent emailContent = EmailContent.builder()
                .template(template)
                .build();

        // 이메일 발송 요청 객체 생성
        SendEmailRequest emailRequest = getSendEmailRequest(destination, emailContent);

        // AWS SES를 통해 이메일 발송 요청
        sesV2Client.sendEmail(emailRequest);
    }

    /**
     * 단건 email 발송 (plain text)
     */
    public void sendPlainTextEmail(String to, String subject, String content) {
        // 수신자(destination) 설정
        Destination destination = getDestination(to);

        // 제목, 본문으로 Message 객체 생성
        Message message = Message.builder()
                .subject(getContent(subject))
                .body(Body.builder().text(getContent(content)).build())
                .build();

        // 이메일 콘텐츠 설정
        EmailContent emailContent = EmailContent.builder()
                .simple(message)
                .build();

        // 이메일 요청 객체 생성
        SendEmailRequest emailRequest = getSendEmailRequest(destination, emailContent);

        // AWS SES를 통해 이메일 발송 요청
        sesV2Client.sendEmail(emailRequest);
    }

    /**
     * 대량 email 발송 (template)
     */
    public void sendBulkTemplatedEmail(List<String> toList, EmailTemplate emailTemplate, Map<String, String> parameter) {
        // 사용할 템플릿 선택 및 변수 매핑
        Template template = getTemplate(emailTemplate.getTemplateName(), parameter);

        // 50개씩 나눠서 이메일 발송
        Lists.partition(toList, 50).forEach(batch -> {
            List<BulkEmailEntry> entries = batch.stream()
                    .map(this::getBulkEmailEntry)
                    .toList();

            // 이메일 발송 요청 객체 생성
            SendBulkEmailRequest sendBulkEmailRequest = SendBulkEmailRequest.builder()
                    .bulkEmailEntries(entries)
                    .defaultContent(BulkEmailContent.builder().template(template).build())
                    .fromEmailAddress(from)
                    .build();

            // 이메일 발송
            sesV2Client.sendBulkEmail(sendBulkEmailRequest);
        });
    }

    private Destination getDestination(String to) {
        return Destination.builder()
                .toAddresses(to)
                .build();
    }

    private Template getTemplate(String templateName, Map<String, String> parameter) {
        return Template.builder()
                .templateName(templateName)
                .templateData(map2JsonSerializer.serializeAsString(parameter))
                .build();
    }

    private SendEmailRequest getSendEmailRequest(Destination destination, EmailContent emailContent) {
        return SendEmailRequest.builder()
                .destination(destination)
                .content(emailContent)
                .fromEmailAddress(from)
                .build();
    }

    private Content getContent(String subject) {
        return Content.builder().data(subject).build();
    }

    private BulkEmailEntry getBulkEmailEntry(String to) {
        return BulkEmailEntry.builder()
                .destination(getDestination(to))
                .build();
    }
}
