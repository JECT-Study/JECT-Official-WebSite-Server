package org.ject.support.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CodeGeneratorUtilTest {

    @Test
    @DisplayName("랜덤 인증 코드 생성 테스트")
    void generateAuthCode_length_check() {
        // given
        int length = 6;

        // when
        String code = CodeGeneratorUtil.generateAuthCode(length);

        // then
        assertNotNull(code);
        assertEquals(length, code.length());
    }
}
