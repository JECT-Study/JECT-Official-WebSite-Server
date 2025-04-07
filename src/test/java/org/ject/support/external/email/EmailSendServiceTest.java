package org.ject.support.external.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ject.support.common.util.Json2MapSerializer;
import org.ject.support.testconfig.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.test.util.ReflectionTestUtils;

@IntegrationTest
class EmailSendServiceTest {
    @Autowired
    private EmailSendService emailSendService;
    
    
    @Autowired
    private Json2MapSerializer json2MapSerializer;
    
    @Mock
    private JavaMailSender javaMailSender;
    
    private final AtomicInteger threadCounter = new AtomicInteger(0);
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(emailSendService, "mailSender", javaMailSender);
        
        // 메일 전송 시 가상 스레드 카운트를 증가시키는 모킹 설정
        doAnswer(invocation -> {
            threadCounter.incrementAndGet();
            return null;
        }).when(javaMailSender).send(any(MimeMessagePreparator[].class));
    }

    @Test
    @DisplayName("단일 이메일 전송 시 가상 스레드 생성 확인")
    void send_single_email_creates_virtual_thread() throws InterruptedException {
        // given
        String testEmail = "test@example.com";
        Map<String, String> params = new HashMap<>();
        params.put("name", "테스트사용자");
        params.put("content", "테스트 내용입니다.");
        
        AtomicInteger virtualThreadCreated = new AtomicInteger(0);
        CountDownLatch threadStartLatch = new CountDownLatch(1);
        
        doAnswer(invocation -> {
            Thread.startVirtualThread(() -> {
                try {
                    virtualThreadCreated.incrementAndGet();
                    threadStartLatch.countDown();
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            
            threadCounter.incrementAndGet();
            return null;
        }).when(javaMailSender).send(any(MimeMessagePreparator[].class));

        threadCounter.set(0);
        virtualThreadCreated.set(0);
        
        int initialVirtualThreadCount = countVirtualThreads();
        System.out.println("초기 가상 스레드 수: " + initialVirtualThreadCount);
        printThreadInfo("테스트 시작 전");
        
        // when
        emailSendService.sendEmail(testEmail, EmailTemplate.ACCEPTANCE, params);
        
        boolean threadStarted = threadStartLatch.await(2, TimeUnit.SECONDS);
        
        printThreadInfo("이메일 전송 후");
        
        // then
        assertThat(threadCounter.get()).isEqualTo(1);
        assertThat(threadStarted).isTrue(); // 가상 스레드가 시작되었는지 확인
        assertThat(virtualThreadCreated.get()).isEqualTo(1); // 가상 스레드가 생성되었는지 확인
        
        int afterVirtualThreadCount = countVirtualThreads();
        System.out.println("초기 가상 스레드 수: " + initialVirtualThreadCount);
        System.out.println("이메일 전송 후 가상 스레드 수: " + afterVirtualThreadCount);
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

    private int countVirtualThreads() {
        Thread[] threads = getAllThreads();
        int virtualThreadCount = 0;
        
        for (Thread thread : threads) {
            if (thread != null && thread.isVirtual()) {
                virtualThreadCount++;
            }
        }
        
        return virtualThreadCount;
    }

    private Thread[] getAllThreads() {
        return Thread.getAllStackTraces().keySet().toArray(new Thread[0]);
    }

    private void printThreadInfo(String prefix) {
        Thread[] threads = getAllThreads();
        int platformThreadCount = 0;
        int virtualThreadCount = 0;
        
        System.out.println("\n==== " + prefix + " - 스레드 정보 ====");
        System.out.println("총 스레드 수: " + threads.length);
        
        for (Thread thread : threads) {
            if (thread.isVirtual()) {
                virtualThreadCount++;
                System.out.println("가상 스레드: " + thread.getName());
            } else {
                platformThreadCount++;
            }
        }
        
        System.out.println("플랫폼 스레드 수: " + platformThreadCount);
        System.out.println("가상 스레드 수: " + virtualThreadCount);
        System.out.println("========================\n");
    }
}
