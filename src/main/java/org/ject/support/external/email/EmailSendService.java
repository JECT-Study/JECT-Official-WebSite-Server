package org.ject.support.external.email;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.ject.support.common.util.Json2MapSerializer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailSendService {
    private final JavaMailSender mailSender;
    private final MessageGenerator messageGenerator;
    private final Json2MapSerializer json2MapSerializer;
    private final RedisTemplate<String, String> redisTemplate;
    private final Random random = new Random();

    // 인증 번호 만료 시간 (초)
    private static final long EXPIRE_TIME = 300;
    public static final int AUTH_CODE_LENGTH = 6;

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
        try {
            mailSender.send(preparators);
        } catch (org.springframework.mail.MailException e) {
            throw new MailException(MailErrorCode.MAIL_SEND_FAILURE);
        }
    }

    public void sendAuthCodeEmail(String email) {
        // 랜덤 6자리 숫자
        String authCode = createAuthCode();

        // Redis에 저장 (key: "email:이메일", value: 인증번호)
        redisTemplate.opsForValue().set(email, authCode, Duration.ofSeconds(EXPIRE_TIME));
        String storedCode = redisTemplate.opsForValue().get(email);
        System.out.println("storedCode = " + storedCode); // TODO: 개발 완료 시 제거
        // TODO: Map.of() 에서 email을 실제 이름으로 변경
        sendEmail(email, EmailTemplate.CERTIFICATE, Map.of("to", email, "value", authCode));

        // TODO: 인증 번호 전송 횟수 제한 로직 추가
    }

    private String createAuthCode() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < AUTH_CODE_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }

        return sb.toString();
    }

    public record MailSendRequest(
            String to,
            EmailTemplate template,
            Map<String,String> parameter
    ) {
    }
}
