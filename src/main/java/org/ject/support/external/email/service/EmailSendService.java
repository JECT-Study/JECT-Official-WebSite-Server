package org.ject.support.external.email.service;

import lombok.RequiredArgsConstructor;
import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.external.email.domain.EmailTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.Destination;
import software.amazon.awssdk.services.sesv2.model.EmailContent;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;
import software.amazon.awssdk.services.sesv2.model.Template;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailSendService {

    private final SesV2Client sesV2Client;
    private final Map2JsonSerializer map2JsonSerializer;

    @Value("${aws.ses.from-email-address}")
    private String from;

    /**
     * 단건 email 전송
     */
    public void sendEmail(String to, EmailTemplate emailTemplate, Map<String, String> parameter) {
        // 수신자(destination) 설정
        Destination destination = Destination.builder()
                .toAddresses(to)
                .build();

        // 사용할 템플릿 선택 및 변수 매핑
        Template template = Template.builder()
                .templateName(emailTemplate.getTemplateName())
                .templateData(map2JsonSerializer.serializeAsString(parameter))
                .build();

        // 이메일 콘텐츠 설정
        EmailContent emailContent = EmailContent.builder()
                .template(template)
                .build();

        // 이메일 요청 객체 생성
        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .destination(destination)
                .content(emailContent)
                .fromEmailAddress(from)
                .build();

        // AWS SES를 통해 이메일 발송 요청
        sesV2Client.sendEmail(emailRequest);
    }
}
