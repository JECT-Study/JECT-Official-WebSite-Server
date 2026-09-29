package org.ject.support.admin.mail.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.ject.support.admin.mail.dto.SendMailDispatchRequest;
import org.ject.support.common.util.Map2JsonSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MailDispatchRequestFingerprintGeneratorTest {

    private final MailDispatchRequestFingerprintGenerator fingerprintGenerator =
            new MailDispatchRequestFingerprintGenerator(
                    new Map2JsonSerializer(new ObjectMapper()));

    @Test
    @DisplayName("요청 필드를 고정된 순서로 직렬화하고 순서 없는 값은 정렬한다")
    void 요청_필드를_고정된_순서로_직렬화하고_순서_없는_값은_정렬한다() {
        // given
        Map<String, String> inputVariables = new LinkedHashMap<>();
        inputVariables.put("B", "두 번째");
        inputVariables.put("A", "첫 번째");
        SendMailDispatchRequest request = new SendMailDispatchRequest(
                2L, 1L, List.of(20L, 10L), "제목", inputVariables);

        // when
        String result = fingerprintGenerator.generate(request);

        // then
        assertThat(result).isEqualTo(
                "{\"applyIds\":[10,20],\"inputVariables\":{\"A\":\"첫 번째\",\"B\":\"두 번째\"},"
                        + "\"recruitId\":2,\"scenarioId\":1,\"subjectOverride\":\"제목\"}");
    }

    @Test
    @DisplayName("지원 ID와 입력 변수 순서가 달라도 같은 fingerprint를 만든다")
    void 지원_ID와_입력_변수_순서가_달라도_같은_fingerprint를_만든다() {
        // given
        SendMailDispatchRequest first = new SendMailDispatchRequest(
                2L, 1L, List.of(10L, 20L), "제목", Map.of("A", "첫 번째", "B", "두 번째"));
        Map<String, String> reorderedVariables = new LinkedHashMap<>();
        reorderedVariables.put("B", "두 번째");
        reorderedVariables.put("A", "첫 번째");
        SendMailDispatchRequest reordered = new SendMailDispatchRequest(
                2L, 1L, List.of(20L, 10L), "제목", reorderedVariables);

        // when
        String firstFingerprint = fingerprintGenerator.generate(first);
        String reorderedFingerprint = fingerprintGenerator.generate(reordered);

        // then
        assertThat(reorderedFingerprint).isEqualTo(firstFingerprint);
    }

    @Test
    @DisplayName("null 입력 변수와 빈 입력 변수는 같은 요청으로 식별한다")
    void null_입력_변수와_빈_입력_변수는_같은_요청으로_식별한다() {
        // given
        SendMailDispatchRequest withoutVariables = new SendMailDispatchRequest(
                2L, 1L, List.of(10L), null, null);
        SendMailDispatchRequest emptyVariables = new SendMailDispatchRequest(
                2L, 1L, List.of(10L), null, Map.of());

        // when
        String nullFingerprint = fingerprintGenerator.generate(withoutVariables);
        String emptyFingerprint = fingerprintGenerator.generate(emptyVariables);

        // then
        assertThat(nullFingerprint).isEqualTo(emptyFingerprint);
    }
}
