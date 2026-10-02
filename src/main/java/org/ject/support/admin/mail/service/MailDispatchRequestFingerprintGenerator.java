package org.ject.support.admin.mail.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import org.ject.support.admin.mail.dto.SendMailDispatchRequest;
import org.ject.support.common.util.Map2JsonSerializer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailDispatchRequestFingerprintGenerator {

    private final Map2JsonSerializer map2JsonSerializer;

    public String generate(SendMailDispatchRequest request) {
        Map<String, Object> payload = new TreeMap<>();
        List<Long> applyIds = request.applyIds() == null
                ? null
                : request.applyIds().stream().sorted(Comparator.nullsFirst(Long::compareTo)).toList();
        payload.put("applyIds", applyIds);
        payload.put("inputVariables", request.inputVariables() == null
                ? Map.of()
                : new TreeMap<>(request.inputVariables()));
        payload.put("recruitId", request.recruitId());
        payload.put("scenarioId", request.scenarioId());
        payload.put("subjectOverride", request.subjectOverride());
        String canonicalJson = map2JsonSerializer.serializeAsString(payload);
        // 요청 크기가 TEXT 한도를 넘더라도 fingerprint는 고정 길이로 유지한다.
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonicalJson.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 fingerprint 생성에 실패했습니다.", exception);
        }
    }
}
