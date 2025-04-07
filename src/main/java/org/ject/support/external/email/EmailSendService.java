package org.ject.support.external.email;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ject.support.common.util.Json2MapSerializer;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSendService {
    private final JavaMailSender mailSender;
    private final MessageGenerator messageGenerator;
    private final Json2MapSerializer json2MapSerializer;

    /**
     * 단건 email 전송
     */
    public void sendEmail(String to, EmailTemplate template, Map<String,String> parameter) {
        MimeMessagePreparator preparator = messageGenerator.generateMessagePreparator(to, template, parameter);
        sendMail(preparator);
    }

    /**
     * 단체 email 전송
     */
    public void sendEmailBulk(List<MailSendRequest> requests) {
        MimeMessagePreparator[] preparators = requests.stream()
                .map(request -> messageGenerator.generateMessagePreparator(request.to, request.template, json2MapSerializer.serializeAsMap(request.parameter)))
                .toArray(MimeMessagePreparator[]::new);
        sendMail(preparators);
    }

    private void sendMail(final MimeMessagePreparator ...preparators) {
        Thread thread = Thread.startVirtualThread(() -> mailSender.send(preparators));
        thread.setUncaughtExceptionHandler((t, e) -> {
            log.error("가상 스레드에서 메일 전송 실패 {}: {}", t.getName(), e.getMessage(), e);
            throw new MailSendException(MailErrorCode.MAIL_SEND_FAILURE);
        });
    }



    public record MailSendRequest(
            String to,
            EmailTemplate template,
            Map<String,String> parameter
    ) {
    }
}
