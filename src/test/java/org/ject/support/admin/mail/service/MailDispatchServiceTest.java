package org.ject.support.admin.mail.service;

import org.ject.support.common.util.Map2JsonSerializer;
import org.ject.support.common.util.String2MapSerializer;
import org.ject.support.admin.mail.domain.MailDispatchJob;
import org.ject.support.admin.mail.domain.MailDispatchJobRepository;
import org.ject.support.admin.mail.domain.MailDispatchJobStatus;
import org.ject.support.admin.mail.domain.MailDispatchTarget;
import org.ject.support.admin.mail.domain.MailDispatchTargetRepository;
import org.ject.support.admin.mail.domain.MailDispatchTargetStatus;
import org.ject.support.admin.mail.domain.MailDispatchTargetStatus;
import org.ject.support.admin.mail.domain.MailScenario;
import org.ject.support.admin.mail.domain.MailScenarioCategory;
import org.ject.support.admin.mail.domain.MailScenarioType;
import org.ject.support.admin.mail.domain.MailScenarioRepository;
import org.ject.support.admin.mail.domain.VariableInputType;
import org.ject.support.admin.mail.domain.MailScenarioVariable;
import org.ject.support.admin.mail.dto.MailDispatchExecuteResponse;
import org.ject.support.admin.mail.dto.MailDispatchFailedTargetResponse;
import org.ject.support.admin.mail.dto.MailDispatchHistoryResponse;
import org.ject.support.admin.mail.dto.MailDispatchRequest;
import org.ject.support.admin.mail.dto.MailDispatchResponse;
import org.ject.support.admin.mail.dto.MailDispatchDetailResponse;
import org.ject.support.admin.mail.dto.MailPreviewRequest;
import org.ject.support.admin.mail.dto.MailPreviewResponse;
import org.ject.support.admin.mail.dto.MailTestSendRequest;
import org.ject.support.admin.mail.dto.MailTestSendResponse;
import org.ject.support.admin.mail.exception.MailErrorCode;
import org.ject.support.admin.mail.exception.MailException;
import org.ject.support.domain.member.entity.Member;
import org.ject.support.domain.member.repository.MemberRepository;
import org.ject.support.domain.recruit.domain.Semester;
import org.ject.support.domain.recruit.repository.SemesterRepository;
import org.ject.support.external.email.service.EmailSendService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class MailDispatchServiceTest {

    @InjectMocks
    private MailDispatchService mailDispatchService;

    @Mock
    private MailScenarioRepository mailScenarioRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SemesterRepository semesterRepository;

    @Mock
    private MailDispatchJobRepository mailDispatchJobRepository;

    @Mock
    private MailDispatchTargetRepository mailDispatchTargetRepository;

    @Mock
    private MailTemplateEngine mailTemplateEngine;

    @Mock
    private MailTemplateValidator mailTemplateValidator;

    @Mock
    private Map2JsonSerializer map2JsonSerializer;

    @Mock
    private String2MapSerializer string2MapSerializer;

    @Mock
    private EmailSendService emailSendService;

    @Mock
    private MailDispatchAsyncExecutor mailDispatchAsyncExecutor;

    @Test
    @DisplayName("preview는 공통/개인 변수를 병합해 제목과 본문을 렌더링한다")
    void preview() {
        MailScenario scenario = MailScenario.builder()
                .name("불합격")
                .category(MailScenarioCategory.CLUB_MEMBER)
                .type(MailScenarioType.REJECT)
                .scenarioCode("MEMBER_REJECT_NOTICE")
                .subjectTemplate("[JECT] ${RECRUIT_NAME} 결과")
                .bodyTemplate("안녕하세요 ${name}님, ${semester}")
                .active(true)
                .customVariables(Set.of(MailScenarioVariable.builder().key("RECRUIT_NAME").label("모집명").inputType(VariableInputType.TEXT).required(true).build()))
                .build();
        ReflectionTestUtils.setField(scenario, "id", 1L);

        Member receiver = Member.builder()
                .id(10L)
                .email("member@ject.kr")
                .name("젝트")
                .semesterId(5L)
                .build();

        Semester semester = Semester.builder()
                .id(5L)
                .name("5기")
                .isRecruiting(true)
                .build();

        MailPreviewRequest request = new MailPreviewRequest(
                1L,
                10L,
                Map.of("RECRUIT_NAME", "메이커스")
        );

        given(mailScenarioRepository.findById(1L)).willReturn(Optional.of(scenario));
        given(memberRepository.findById(10L)).willReturn(Optional.of(receiver));
        given(semesterRepository.findAllById(Set.of(5L))).willReturn(List.of(semester));
        given(mailTemplateEngine.render(eq(scenario.getSubjectTemplate()), any())).willReturn("[JECT] 메이커스 결과");
        given(mailTemplateEngine.render(eq(scenario.getBodyTemplate()), any())).willReturn("안녕하세요 젝트님, 5기");

        MailPreviewResponse response = mailDispatchService.preview(request);

        assertThat(response.mailScenarioId()).isEqualTo(1L);
        assertThat(response.receiverId()).isEqualTo(10L);
        assertThat(response.receiverEmail()).isEqualTo("member@ject.kr");
        assertThat(response.subject()).isEqualTo("[JECT] 메이커스 결과");
        assertThat(response.body()).isEqualTo("안녕하세요 젝트님, 5기");
    }

    @Test
    @DisplayName("비활성 시나리오는 preview할 수 없다")
    void preview_InactiveScenario() {
        MailScenario inactiveScenario = MailScenario.builder()
                .name("비활성")
                .category(MailScenarioCategory.GENERAL)
                .type(MailScenarioType.ETC)
                .scenarioCode("INACTIVE")
                .subjectTemplate("subject")
                .bodyTemplate("body")
                .active(false)
                .customVariables(Set.of())
                .build();
        ReflectionTestUtils.setField(inactiveScenario, "id", 1L);

        given(mailScenarioRepository.findById(1L)).willReturn(Optional.of(inactiveScenario));

        assertThatThrownBy(() -> mailDispatchService.preview(new MailPreviewRequest(1L, 1L, Map.of())))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.INACTIVE_SCENARIO);
    }

    @Test
    @DisplayName("테스트 메일 발송은 단건 렌더링 후 지정 이메일로 전송한다")
    void sendTestMail() {
        MailScenario scenario = MailScenario.builder()
                .name("불합격")
                .category(MailScenarioCategory.CLUB_MEMBER)
                .type(MailScenarioType.REJECT)
                .scenarioCode("MEMBER_REJECT_NOTICE")
                .subjectTemplate("[JECT] ${RECRUIT_NAME} 결과")
                .bodyTemplate("안녕하세요 ${name}님, ${semester}")
                .active(true)
                .customVariables(Set.of(MailScenarioVariable.builder().key("RECRUIT_NAME").label("모집명").inputType(VariableInputType.TEXT).required(true).build()))
                .build();
        ReflectionTestUtils.setField(scenario, "id", 1L);

        Member receiver = Member.builder()
                .id(10L)
                .email("member@ject.kr")
                .name("젝트")
                .semesterId(5L)
                .build();

        Semester semester = Semester.builder()
                .id(5L)
                .name("5기")
                .isRecruiting(true)
                .build();

        MailTestSendRequest request = new MailTestSendRequest(
                1L,
                10L,
                "test@ject.kr",
                Map.of("RECRUIT_NAME", "메이커스")
        );

        given(mailScenarioRepository.findById(1L)).willReturn(Optional.of(scenario));
        given(memberRepository.findById(10L)).willReturn(Optional.of(receiver));
        given(semesterRepository.findAllById(Set.of(5L))).willReturn(List.of(semester));
        given(mailTemplateEngine.render(eq(scenario.getSubjectTemplate()), any())).willReturn("[JECT] 메이커스 결과");
        given(mailTemplateEngine.render(eq(scenario.getBodyTemplate()), any())).willReturn("안녕하세요 젝트님, 5기");

        MailTestSendResponse response = mailDispatchService.sendTestMail(request);

        assertThat(response.mailScenarioId()).isEqualTo(1L);
        assertThat(response.receiverId()).isEqualTo(10L);
        assertThat(response.toEmail()).isEqualTo("test@ject.kr");
        assertThat(response.subject()).isEqualTo("[JECT] 메이커스 결과");
        assertThat(response.status()).isEqualTo("SENT");
        then(emailSendService).should().sendEmail("test@ject.kr", "[JECT] 메이커스 결과", "안녕하세요 젝트님, 5기");
    }

    @Test
    @DisplayName("테스트 메일 발송 실패 시 TEST_MAIL_SEND_FAILURE 예외를 반환한다")
    void sendTestMail_Fail() {
        MailScenario scenario = MailScenario.builder()
                .name("불합격")
                .category(MailScenarioCategory.CLUB_MEMBER)
                .type(MailScenarioType.REJECT)
                .scenarioCode("MEMBER_REJECT_NOTICE")
                .subjectTemplate("[JECT] ${RECRUIT_NAME} 결과")
                .bodyTemplate("안녕하세요 ${name}님")
                .active(true)
                .customVariables(Set.of(MailScenarioVariable.builder().key("RECRUIT_NAME").label("모집명").inputType(VariableInputType.TEXT).required(true).build()))
                .build();
        ReflectionTestUtils.setField(scenario, "id", 1L);

        Member receiver = Member.builder()
                .id(10L)
                .email("member@ject.kr")
                .name("젝트")
                .semesterId(5L)
                .build();

        Semester semester = Semester.builder()
                .id(5L)
                .name("5기")
                .isRecruiting(true)
                .build();

        MailTestSendRequest request = new MailTestSendRequest(
                1L,
                10L,
                "test@ject.kr",
                Map.of("RECRUIT_NAME", "메이커스")
        );

        given(mailScenarioRepository.findById(1L)).willReturn(Optional.of(scenario));
        given(memberRepository.findById(10L)).willReturn(Optional.of(receiver));
        given(semesterRepository.findAllById(Set.of(5L))).willReturn(List.of(semester));
        given(mailTemplateEngine.render(eq(scenario.getSubjectTemplate()), any())).willReturn("[JECT] 메이커스 결과");
        given(mailTemplateEngine.render(eq(scenario.getBodyTemplate()), any())).willReturn("안녕하세요 젝트님");
        doThrow(new RuntimeException("ses fail"))
                .when(emailSendService).sendEmail(eq("test@ject.kr"), anyString(), anyString());

        assertThatThrownBy(() -> mailDispatchService.sendTestMail(request))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.TEST_MAIL_SEND_FAILURE);
    }

    @Test
    @DisplayName("dispatch는 작업/수신자 타겟을 생성하고 REQUESTED 상태를 반환한다")
    void dispatch() {
        MailScenario scenario = MailScenario.builder()
                .name("불합격")
                .category(MailScenarioCategory.CLUB_MEMBER)
                .type(MailScenarioType.REJECT)
                .scenarioCode("MEMBER_REJECT_NOTICE")
                .subjectTemplate("[JECT] ${RECRUIT_NAME}")
                .bodyTemplate("안녕하세요 ${name}님")
                .active(true)
                .customVariables(Set.of(MailScenarioVariable.builder().key("RECRUIT_NAME").label("모집명").inputType(VariableInputType.TEXT).required(true).build()))
                .build();
        ReflectionTestUtils.setField(scenario, "id", 1L);

        Member receiver1 = Member.builder().id(10L).email("a@ject.kr").semesterId(5L).build();
        Member receiver2 = Member.builder().id(11L).email("b@ject.kr").semesterId(5L).build();

        MailDispatchRequest request = new MailDispatchRequest(
                1L,
                List.of(10L, 11L),
                Map.of("RECRUIT_NAME", "메이커스")
        );

        given(mailScenarioRepository.findById(1L)).willReturn(Optional.of(scenario));
        given(memberRepository.findAllById(List.of(10L, 11L))).willReturn(List.of(receiver1, receiver2));
        given(map2JsonSerializer.serializeAsString(request.commonVariables())).willReturn("{\"RECRUIT_NAME\":\"메이커스\"}");
        given(mailDispatchJobRepository.save(any(MailDispatchJob.class))).willAnswer(invocation -> {
            MailDispatchJob job = invocation.getArgument(0);
            ReflectionTestUtils.setField(job, "id", 100L);
            return job;
        });

        MailDispatchResponse response = mailDispatchService.dispatch(request);

        assertThat(response.dispatchJobId()).isEqualTo(100L);
        assertThat(response.mailScenarioId()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo("REQUESTED");
        assertThat(response.receiverCount()).isEqualTo(2);

        ArgumentCaptor<List<MailDispatchTarget>> targetCaptor = ArgumentCaptor.forClass(List.class);
        then(mailDispatchTargetRepository).should().saveAll(targetCaptor.capture());
        assertThat(targetCaptor.getValue()).hasSize(2);
    }

    @Test
    @DisplayName("dispatch 대상 수신자 중 일부라도 없으면 예외가 발생한다")
    void dispatch_ReceiverNotFound() {
        MailScenario scenario = MailScenario.builder()
                .name("불합격")
                .category(MailScenarioCategory.CLUB_MEMBER)
                .type(MailScenarioType.REJECT)
                .scenarioCode("MEMBER_REJECT_NOTICE")
                .subjectTemplate("[JECT] ${RECRUIT_NAME}")
                .bodyTemplate("안녕하세요 ${name}님")
                .active(true)
                .customVariables(Set.of(MailScenarioVariable.builder().key("RECRUIT_NAME").label("모집명").inputType(VariableInputType.TEXT).required(true).build()))
                .build();
        ReflectionTestUtils.setField(scenario, "id", 1L);

        Member receiver1 = Member.builder().id(10L).email("a@ject.kr").semesterId(5L).build();

        MailDispatchRequest request = new MailDispatchRequest(
                1L,
                List.of(10L, 11L),
                Map.of("RECRUIT_NAME", "메이커스")
        );

        given(mailScenarioRepository.findById(1L)).willReturn(Optional.of(scenario));
        given(memberRepository.findAllById(List.of(10L, 11L))).willReturn(List.of(receiver1));

        assertThatThrownBy(() -> mailDispatchService.dispatch(request))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.RECEIVER_NOT_FOUND);
    }

    @Test
    @DisplayName("executeDispatch는 작업을 시작하고 PROCESSING 상태를 반환한다")
    void executeDispatch_Success() {
        MailScenario scenario = MailScenario.builder()
                .name("불합격")
                .category(MailScenarioCategory.CLUB_MEMBER)
                .type(MailScenarioType.REJECT)
                .scenarioCode("MEMBER_REJECT_NOTICE")
                .subjectTemplate("[JECT] ${RECRUIT_NAME}")
                .bodyTemplate("안녕하세요 ${name}님")
                .active(true)
                .customVariables(Set.of(MailScenarioVariable.builder().key("RECRUIT_NAME").label("모집명").inputType(VariableInputType.TEXT).required(true).build()))
                .build();
        ReflectionTestUtils.setField(scenario, "id", 1L);

        MailDispatchJob job = MailDispatchJob.builder()
                .scenario(scenario)
                .status(MailDispatchJobStatus.REQUESTED)
                .receiverCount(2)
                .commonVariablesJson("{\"RECRUIT_NAME\":\"메이커스\"}")
                .build();
        ReflectionTestUtils.setField(job, "id", 200L);

        given(mailDispatchJobRepository.findByIdWithScenario(200L)).willReturn(Optional.of(job));

        MailDispatchExecuteResponse response = mailDispatchService.executeDispatch(200L);

        assertThat(response.dispatchJobId()).isEqualTo(200L);
        assertThat(response.status()).isEqualTo("PROCESSING");
        then(mailDispatchAsyncExecutor).should().execute(200L);
    }


    @Test
    @DisplayName("executeDispatch는 REQUESTED 상태가 아니면 실행할 수 없다")
    void executeDispatch_InvalidStatus() {
        MailScenario scenario = MailScenario.builder()
                .name("불합격")
                .category(MailScenarioCategory.CLUB_MEMBER)
                .type(MailScenarioType.REJECT)
                .scenarioCode("MEMBER_REJECT_NOTICE")
                .subjectTemplate("subject")
                .bodyTemplate("body")
                .active(true)
                .customVariables(Set.of())
                .build();

        MailDispatchJob job = MailDispatchJob.builder()
                .scenario(scenario)
                .status(MailDispatchJobStatus.COMPLETED)
                .receiverCount(1)
                .commonVariablesJson("{}")
                .build();
        ReflectionTestUtils.setField(job, "id", 202L);

        given(mailDispatchJobRepository.findByIdWithScenario(202L)).willReturn(Optional.of(job));

        assertThatThrownBy(() -> mailDispatchService.executeDispatch(202L))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.INVALID_DISPATCH_JOB_STATUS);
    }

    @Test
    @DisplayName("발송 작업 이력 목록을 최신순으로 조회한다")
    void getDispatchHistories() {
        MailScenario scenario1 = MailScenario.builder()
                .name("시나리오1")
                .category(MailScenarioCategory.CLUB_MEMBER)
                .type(MailScenarioType.ETC)
                .scenarioCode("SCENARIO_1")
                .subjectTemplate("subject")
                .bodyTemplate("body")
                .active(true)
                .customVariables(Set.of())
                .build();
        ReflectionTestUtils.setField(scenario1, "id", 1L);

        MailScenario scenario2 = MailScenario.builder()
                .name("시나리오2")
                .category(MailScenarioCategory.CLUB_MEMBER)
                .type(MailScenarioType.ETC)
                .scenarioCode("SCENARIO_2")
                .subjectTemplate("subject")
                .bodyTemplate("body")
                .active(true)
                .customVariables(Set.of())
                .build();
        ReflectionTestUtils.setField(scenario2, "id", 2L);

        MailDispatchJob job1 = MailDispatchJob.builder()
                .scenario(scenario1)
                .status(MailDispatchJobStatus.COMPLETED)
                .receiverCount(2)
                .commonVariablesJson("{}")
                .build();
        ReflectionTestUtils.setField(job1, "id", 10L);
        ReflectionTestUtils.setField(job1, "createdAt", LocalDateTime.of(2026, 4, 1, 10, 0));

        MailDispatchJob job2 = MailDispatchJob.builder()
                .scenario(scenario2)
                .status(MailDispatchJobStatus.FAILED)
                .receiverCount(1)
                .commonVariablesJson("{}")
                .build();
        ReflectionTestUtils.setField(job2, "id", 11L);
        ReflectionTestUtils.setField(job2, "createdAt", LocalDateTime.of(2026, 4, 2, 10, 0));

        MailDispatchTarget job1Sent = MailDispatchTarget.pending(job1, 100L, "a@ject.kr");
        job1Sent.markSent();
        MailDispatchTarget job1Failed = MailDispatchTarget.pending(job1, 101L, "b@ject.kr");
        job1Failed.markFailed("ses fail");
        MailDispatchTarget job2Failed = MailDispatchTarget.pending(job2, 102L, "c@ject.kr");
        job2Failed.markFailed("blocked");

        given(mailDispatchJobRepository.findAllWithScenarioOrderByIdDesc()).willReturn(List.of(job2, job1));
        given(mailDispatchTargetRepository.findAllByJobIdIn(List.of(11L, 10L)))
                .willReturn(List.of(job1Sent, job1Failed, job2Failed));

        List<MailDispatchHistoryResponse> responses = mailDispatchService.getDispatchHistories();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).dispatchJobId()).isEqualTo(11L);
        assertThat(responses.get(0).status()).isEqualTo("FAILED");
        assertThat(responses.get(0).sentCount()).isEqualTo(0);
        assertThat(responses.get(0).failedCount()).isEqualTo(1);
        assertThat(responses.get(1).dispatchJobId()).isEqualTo(10L);
        assertThat(responses.get(1).status()).isEqualTo("COMPLETED");
        assertThat(responses.get(1).sentCount()).isEqualTo(1);
        assertThat(responses.get(1).failedCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("발송 작업 상세 조회 시 상태별 카운트와 공통 변수를 반환한다")
    void getDispatchHistory() {
        MailScenario scenario = MailScenario.builder()
                .name("불합격")
                .category(MailScenarioCategory.CLUB_MEMBER)
                .type(MailScenarioType.REJECT)
                .scenarioCode("MEMBER_REJECT_NOTICE")
                .subjectTemplate("subject")
                .bodyTemplate("body")
                .active(true)
                .customVariables(Set.of())
                .build();
        ReflectionTestUtils.setField(scenario, "id", 1L);

        MailDispatchJob job = MailDispatchJob.builder()
                .scenario(scenario)
                .status(MailDispatchJobStatus.PROCESSING)
                .receiverCount(3)
                .commonVariablesJson("{\"RECRUIT_NAME\":\"메이커스\"}")
                .build();
        ReflectionTestUtils.setField(job, "id", 200L);
        ReflectionTestUtils.setField(job, "createdAt", LocalDateTime.of(2026, 4, 3, 10, 0));

        MailDispatchTarget sentTarget = MailDispatchTarget.pending(job, 10L, "a@ject.kr");
        sentTarget.markSent();
        MailDispatchTarget failedTarget = MailDispatchTarget.pending(job, 11L, "b@ject.kr");
        failedTarget.markFailed("ses fail");
        MailDispatchTarget pendingTarget = MailDispatchTarget.pending(job, 12L, "c@ject.kr");

        given(mailDispatchJobRepository.findByIdWithScenario(200L)).willReturn(Optional.of(job));
        given(mailDispatchTargetRepository.findAllByJobIdOrderByIdAsc(200L))
                .willReturn(List.of(sentTarget, failedTarget, pendingTarget));
        given(string2MapSerializer.serializeAsMap("{\"RECRUIT_NAME\":\"메이커스\"}"))
                .willReturn(Map.of("RECRUIT_NAME", "메이커스"));

        MailDispatchDetailResponse response = mailDispatchService.getDispatchHistory(200L);

        assertThat(response.dispatchJobId()).isEqualTo(200L);
        assertThat(response.status()).isEqualTo("PROCESSING");
        assertThat(response.receiverCount()).isEqualTo(3);
        assertThat(response.pendingCount()).isEqualTo(1);
        assertThat(response.sentCount()).isEqualTo(1);
        assertThat(response.failedCount()).isEqualTo(1);
        assertThat(response.commonVariables()).containsEntry("RECRUIT_NAME", "메이커스");
    }

    @Test
    @DisplayName("존재하지 않는 발송 작업 상세 조회 시 DISPATCH_JOB_NOT_FOUND 예외를 반환한다")
    void getDispatchHistory_NotFound() {
        given(mailDispatchJobRepository.findByIdWithScenario(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> mailDispatchService.getDispatchHistory(999L))
                .isInstanceOf(MailException.class)
                .extracting("errorCode")
                .isEqualTo(MailErrorCode.DISPATCH_JOB_NOT_FOUND);
    }

    @Test
    @DisplayName("발송 실패 대상 조회 시 실패한 타겟만 반환한다")
    void getFailedTargets() {
        MailScenario scenario = MailScenario.builder()
                .name("불합격")
                .category(MailScenarioCategory.CLUB_MEMBER)
                .type(MailScenarioType.REJECT)
                .scenarioCode("MEMBER_REJECT_NOTICE")
                .subjectTemplate("subject")
                .bodyTemplate("body")
                .active(true)
                .customVariables(Set.of())
                .build();
        ReflectionTestUtils.setField(scenario, "id", 1L);

        MailDispatchJob job = MailDispatchJob.builder()
                .scenario(scenario)
                .status(MailDispatchJobStatus.FAILED)
                .receiverCount(2)
                .commonVariablesJson("{}")
                .build();
        ReflectionTestUtils.setField(job, "id", 210L);

        MailDispatchTarget failedTarget1 = MailDispatchTarget.pending(job, 10L, "a@ject.kr");
        failedTarget1.markFailed("ses timeout");
        ReflectionTestUtils.setField(failedTarget1, "id", 300L);
        ReflectionTestUtils.setField(failedTarget1, "updatedAt", LocalDateTime.of(2026, 4, 4, 10, 0));

        MailDispatchTarget failedTarget2 = MailDispatchTarget.pending(job, 11L, "b@ject.kr");
        failedTarget2.markFailed("mailbox unavailable");
        ReflectionTestUtils.setField(failedTarget2, "id", 301L);
        ReflectionTestUtils.setField(failedTarget2, "updatedAt", LocalDateTime.of(2026, 4, 4, 10, 1));

        given(mailDispatchJobRepository.findByIdWithScenario(210L)).willReturn(Optional.of(job));
        given(mailDispatchTargetRepository.findAllByJobIdAndStatusOrderByIdAsc(210L, MailDispatchTargetStatus.FAILED))
                .willReturn(List.of(failedTarget1, failedTarget2));

        List<MailDispatchFailedTargetResponse> responses = mailDispatchService.getFailedTargets(210L);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).targetId()).isEqualTo(300L);
        assertThat(responses.get(0).failureReason()).isEqualTo("ses timeout");
        assertThat(responses.get(1).targetId()).isEqualTo(301L);
        assertThat(responses.get(1).failureReason()).isEqualTo("mailbox unavailable");
    }
}
