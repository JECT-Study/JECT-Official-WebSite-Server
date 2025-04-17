package org.ject.support.external.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.external.email.event.EmailSendEvent;
import org.ject.support.external.email.event.EmailSendEventListener;
import org.ject.support.testconfig.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@IntegrationTest
class EmailSendServiceTest {
    @Autowired
    private EmailSendService emailSendService;
    
    @Mock
    private ApplicationEventPublisher eventPublisher;
    
    @Mock
    private EmailSendEventListener emailSendEventListener;
    
    private final AtomicInteger eventPublishCounter = new AtomicInteger(0);
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(emailSendService, "eventPublisher", eventPublisher);
        
        // 이벤트 발행 카운트를 증가시키는 모킹 설정
        doAnswer(invocation -> {
            eventPublishCounter.incrementAndGet();
            EmailSendEvent event = invocation.getArgument(0);
            emailSendEventListener.handleEmailSendEvent(event);
            return null;
        }).when(eventPublisher).publishEvent(any(EmailSendEvent.class));
    }

    @Test
    @DisplayName("단일 이메일 전송 시 이벤트 발행 확인")
    void send_single_email_publishes_event() {
        // given
        String testEmail = "test@example.com";
        Map<String, String> params = new HashMap<>();
        params.put("name", "테스트사용자");
        params.put("content", "테스트 내용입니다.");
        
        eventPublishCounter.set(0);
        
        // 이벤트 캡처를 위한 ArgumentCaptor 설정
        ArgumentCaptor<EmailSendEvent> eventCaptor = ArgumentCaptor.forClass(EmailSendEvent.class);
        
        // when
        emailSendService.sendEmail(testEmail, EmailTemplate.AUTH_CODE, params);
        
        // then
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        EmailSendEvent capturedEvent = eventCaptor.getValue();
        
        // 이벤트가 발행되었는지 확인
        assertThat(eventPublishCounter.get()).isEqualTo(1);
        // 이벤트에 포함된 preparator가 존재하는지 확인
        assertThat(capturedEvent.preparators()).isNotNull();
        assertThat(capturedEvent.preparators().length).isEqualTo(1);
    }
    
    @Test
    @DisplayName("실제 이메일 전송 테스트 (필요시 주석 해제)")
    void send_email() {
        // StopWatch stopWatch = new StopWatch();
        // stopWatch.start();
        // TestMailParameter parameter = new TestMailParameter("ject", "신예찬");
        // emailSendService.sendEmail("****@gmail.com", EmailTemplate.ACCEPTANCE, Json2MapSerializer.serializeAsMap(parameter));
        // stopWatch.stop();
        // System.out.println(stopWatch.getTotalTimeSeconds());
    }

    @Getter
    @AllArgsConstructor
    static class TestMailParameter {
        private final String to;
        private final String value;
    }

    private Thread[] getAllThreads() {
        return Thread.getAllStackTraces().keySet().toArray(new Thread[0]);
    }
}
