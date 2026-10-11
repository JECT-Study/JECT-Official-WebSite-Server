package org.ject.support.admin.mail.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.ject.support.admin.mail.dto.SendMailDispatchRequest;
import org.ject.support.common.util.Map2JsonSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MailDispatchRequestFingerprintGeneratorTest {

    private final MailDispatchRequestFingerprintGenerator fingerprintGenerator =
            new MailDispatchRequestFingerprintGenerator(
                    new Map2JsonSerializer(new ObjectMapper()));

    @ParameterizedTest(name = "{0} 변경은 다른 요청으로 식별한다")
    @MethodSource("changedRequests")
    void 요청_필드가_달라지면_다른_fingerprint를_만든다(String field, SendMailDispatchRequest changedRequest) {
        // given
        SendMailDispatchRequest original = new SendMailDispatchRequest(
                2L, 1L, List.of(10L, 20L), "제목", Map.of("A", "첫 번째", "B", "두 번째"));

        // when
        String changedFingerprint = fingerprintGenerator.generate(changedRequest);

        // then
        assertThat(changedFingerprint).isNotEqualTo(fingerprintGenerator.generate(original));
    }

    private static Stream<Arguments> changedRequests() {
        Map<String, String> variables = Map.of("A", "첫 번째", "B", "두 번째");
        return Stream.of(
                Arguments.of("recruitId", new SendMailDispatchRequest(
                        3L, 1L, List.of(10L, 20L), "제목", variables)),
                Arguments.of("scenarioId", new SendMailDispatchRequest(
                        2L, 3L, List.of(10L, 20L), "제목", variables)),
                Arguments.of("applyIds", new SendMailDispatchRequest(
                        2L, 1L, List.of(10L, 30L), "제목", variables)),
                Arguments.of("subjectOverride", new SendMailDispatchRequest(
                        2L, 1L, List.of(10L, 20L), "다른 제목", variables)),
                Arguments.of("inputVariables", new SendMailDispatchRequest(
                        2L, 1L, List.of(10L, 20L), "제목", Map.of("A", "다른 값", "B", "두 번째"))));
    }

    @Test
    @DisplayName("정규화된 요청을 고정 길이 SHA-256 fingerprint로 식별한다")
    void 정규화된_요청을_고정_길이_SHA256_fingerprint로_식별한다() {
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
                "8199090e675fc69cd072439b857f3c396382d7a73809a2be689926cf49d55437");
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
