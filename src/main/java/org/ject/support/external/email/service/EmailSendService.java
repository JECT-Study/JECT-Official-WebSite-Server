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
        Destination destination = Destination.builder()
                .toAddresses(to)
                .build();

        Template template = Template.builder()
                .templateName(emailTemplate.getTemplateName())
                .templateData(map2JsonSerializer.serializeAsString(parameter))
                .build();

        EmailContent emailContent = EmailContent.builder()
                .template(template)
                .build();

        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .destination(destination)
                .content(emailContent)
                .fromEmailAddress(from)
                .build();

        sesV2Client.sendEmail(emailRequest);
    }
}
