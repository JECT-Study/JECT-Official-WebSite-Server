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

class MailDispatchRequestFingerprintTest {

    private final MailDispatchRequestFingerprint fingerprint = new MailDispatchRequestFingerprint(
            new Map2JsonSerializer(new ObjectMapper()));

    @Test
    @DisplayName("입력 변수 맵 순서가 달라도 같은 요청 본문으로 식별한다")
    void 입력_변수_맵_순서가_달라도_같은_요청_본문으로_식별한다() {
        // given
        SendMailDispatchRequest first = request(Map.of("B", "두 번째", "A", "첫 번째"));
        Map<String, String> reorderedVariables = new LinkedHashMap<>();
        reorderedVariables.put("A", "첫 번째");
        reorderedVariables.put("B", "두 번째");
        SendMailDispatchRequest second = request(reorderedVariables);

        // when
        String firstFingerprint = fingerprint.generate(first);
        String secondFingerprint = fingerprint.generate(second);

        // then
        assertThat(firstFingerprint).isEqualTo(secondFingerprint);
    }

    private SendMailDispatchRequest request(Map<String, String> inputVariables) {
        return new SendMailDispatchRequest(2L, 1L, List.of(10L, 20L), "제목", inputVariables);
    }
}
