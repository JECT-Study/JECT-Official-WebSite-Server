package org.ject.support.external.email.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ject.support.external.email.MailErrorCode;
import org.ject.support.external.email.MailSendException;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSendEventListener {
    
    private final JavaMailSender mailSender;
    
    @Async
    @EventListener
    public void handleEmailSendEvent(EmailSendEvent event) {
        try {
            mailSender.send(event.preparators());
            log.info("이메일 전송 성공");
        } catch (Exception e) {
            log.error("이메일 전송 실패: {}", e.getMessage(), e);
            throw new MailSendException(MailErrorCode.MAIL_SEND_FAILURE);
        }
    }
}
