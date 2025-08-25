package org.ject.support.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeGeneratorUtilTest {

    @Test
    @DisplayName("길이가 6인 랜덤한 숫자 생성을 요청하면 6자리 숫자 코드가 생성된다")
    void generateDigitCode_length_and_pattern_check() {
        // given
        int length = 6;

        // when
        String code = CodeGeneratorUtil.generateDigitCode(length);

        // then
        assertNotNull(code);
        assertEquals(length, code.length());
        assertTrue(code.matches("[0-9]+"));
    }

    @Test
    @DisplayName("길이가 6인 랜덤한 알파벳과 숫자 생성을 요청하면 6자리 코드가 생성된다")
    void generateAlphaNumCode_length_and_pattern_check() {
        // given
        int length = 6;

        // when
        String code = CodeGeneratorUtil.generateAlphaNumCode(length);

        // then
        assertNotNull(code);
        assertEquals(length, code.length());
        assertTrue(code.matches("[0-9A-Z]+"));
    }
}
