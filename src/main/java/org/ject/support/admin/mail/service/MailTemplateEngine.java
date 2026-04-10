package org.ject.support.admin.mail.service;

import static org.apache.commons.lang3.StringUtils.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 문자열 템플릿의 플레이스홀더를 전달받은 변수 값으로 치환하는 렌더링 컴포넌트입니다.
 */
@Service
public class MailTemplateEngine {

    /**
     * 플레이스홀더를 제공된 데이터로 치환합니다.
     *
     * @param template HTML 템플릿 문자열
     * @param variables 치환할 변수 맵
     * @return 치환된 HTML 문자열
     */
    public String render(String template, Map<String, Object> variables) {
        if (isBlank(template)) {
            return EMPTY;
        }
        return new StringSubstitutor(variables).replace(template);
    }
}
